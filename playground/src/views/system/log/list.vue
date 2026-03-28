<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOperationLogList } from '#/api/system/log';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async (_, formValues) => {
          const rows = await getOperationLogList({
            limit: 200,
            ...formValues,
          });
          return rows.map((item, index) => ({
            ...item,
            rowKey: `${item.time ?? ''}-${item.username ?? ''}-${item.path ?? ''}-${index}`,
          }));
        },
      },
    },
    rowConfig: { keyField: 'rowKey' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions,
});
</script>

<template>
  <Page auto-content-height>
    <Grid :table-title="$t('system.log.list')" />
  </Page>
</template>
