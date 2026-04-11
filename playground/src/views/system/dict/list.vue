<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDictType, getDictTypeList, refreshDictCache } from '#/api';
import { clearDictCache } from '#/composables/use-dict';
import { $t } from '#/locales';

import { useTypeColumns } from './data';
import DataManage from './modules/data-manage.vue';
import TypeForm from './modules/type-form.vue';

const { hasAccessByCodes } = useAccess();

const [TypeFormDrawer, typeFormDrawerApi] = useVbenDrawer({
  connectedComponent: TypeForm,
  destroyOnClose: true,
});
const [DataManageDrawer, dataManageDrawerApi] = useVbenDrawer({
  connectedComponent: DataManage,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'name',
        label: $t('system.dict.typeName'),
      },
      {
        component: 'Input',
        fieldName: 'code',
        label: $t('system.dict.typeCode'),
      },
    ],
    submitOnChange: true,
  },
  gridOptions: {
    columns: useTypeColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          await getDictTypeList({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemDictApi.DictType>,
});

function onActionClick(e: OnActionClickParams<SystemDictApi.DictType>) {
  if (e.code === 'manage-items') {
    dataManageDrawerApi.setData(e.row).open();
  }
  if (e.code === 'edit') {
    typeFormDrawerApi.setData(e.row).open();
  }
  if (e.code === 'delete') {
    onDelete(e.row);
  }
}

async function onDelete(row: SystemDictApi.DictType) {
  await deleteDictType(row.id);
  message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
  gridApi.query();
}

function onCreate() {
  typeFormDrawerApi.setData({}).open();
}

async function onRefreshCache() {
  await refreshDictCache();
  clearDictCache();
  message.success($t('system.dict.refreshSuccess'));
}
</script>

<template>
  <Page auto-content-height>
    <TypeFormDrawer @success="gridApi.query" />
    <DataManageDrawer />

    <Grid :table-title="$t('system.dict.typeList')">
      <template #toolbar-tools>
        <Button
          v-access:code="['System:Dict:Create']"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.dict.name')]) }}
        </Button>
        <Button
          v-if="hasAccessByCodes(['System:Dict:Refresh'])"
          class="ml-2"
          @click="onRefreshCache"
        >
          {{ $t('system.dict.refreshCache') }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>
