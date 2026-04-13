<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDictData, getDictDataList } from '#/api';
import { $t } from '#/locales';

import { useDataColumns } from '../data';
import DataForm from './data-form.vue';

const typeCode = ref('');
const typeName = ref('');

const [DataFormDrawer, dataFormDrawerApi] = useVbenDrawer({
  connectedComponent: DataForm,
  destroyOnClose: true,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = drawerApi.getData<SystemDictApi.DictType>();
    typeCode.value = data?.code ?? '';
    typeName.value = data?.name ?? '';
    await nextTick();
    gridApi.query();
  },
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'label',
        label: $t('system.dict.label'),
      },
      {
        component: 'Input',
        fieldName: 'value',
        label: $t('system.dict.value'),
      },
    ],
    submitOnChange: true,
  },
  gridOptions: {
    columns: useDataColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!typeCode.value) {
            return { items: [], total: 0 };
          }
          return await getDictDataList({
            page: page.currentPage,
            pageSize: page.pageSize,
            typeCode: typeCode.value,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true },
  } as VxeTableGridOptions<SystemDictApi.DictData>,
});

const drawerTitle = computed(
  () => `${typeName.value || ''} ${$t('system.dict.dataList')}`,
);

function onActionClick(e: OnActionClickParams<SystemDictApi.DictData>) {
  if (e.code === 'edit') {
    dataFormDrawerApi.setData({ item: e.row, typeCode: typeCode.value }).open();
  }
  if (e.code === 'delete') {
    onDelete(e.row);
  }
}

async function onDelete(row: SystemDictApi.DictData) {
  await deleteDictData(row.id);
  message.success($t('ui.actionMessage.deleteSuccess', [row.label]));
  gridApi.query();
}

function onCreate() {
  if (!typeCode.value) {
    message.warning($t('system.dict.selectTypeFirst'));
    return;
  }
  dataFormDrawerApi.setData({ typeCode: typeCode.value }).open();
}
</script>

<template>
  <Drawer :title="drawerTitle" class="w-[40vw]">
    <DataFormDrawer @success="gridApi.query" />
    <Grid :table-title="$t('system.dict.dataList')">
      <template #toolbar-tools>
        <Button
          v-access:code="['System:Dict:Create']"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.dict.dataName')]) }}
        </Button>
      </template>
    </Grid>
  </Drawer>
</template>
