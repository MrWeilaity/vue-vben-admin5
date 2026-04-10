<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemOnlineApi } from '#/api/system/online';

import { Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOnlineUserList, offlineSession } from '#/api/system/online';
import { $t } from '#/locales';

import { useColumns } from './data';

async function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemOnlineApi.OnlineUser>) {
  if (code === 'offline') {
    await offlineSession(row.sessionId);
    message.success($t('system.online.offlineSuccess'));
    await gridApi.reload();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async () => {
          const items = await getOnlineUserList();
          return { items, total: items.length };
        },
      },
    },
    rowConfig: { keyField: 'sessionId' },
    toolbarConfig: {
      custom: true,
      refresh: true,
      zoom: true,
    },
  } as VxeTableGridOptions,
});
</script>

<template>
  <Page auto-content-height>
    <Grid :table-title="$t('system.online.list')" />
  </Page>
</template>
