import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemLogApi } from '#/api/system/log';

import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'keyword',
      label: $t('system.log.keyword'),
    },
  ];
}

export function useColumns(): VxeTableGridColumns<SystemLogApi.OperationLog> {
  return [
    { field: 'username', title: $t('system.log.username'), width: 140 },
    { field: 'method', title: $t('system.log.method'), width: 100 },
    { field: 'path', title: $t('system.log.path'), minWidth: 220 },
    {
      field: 'result',
      title: $t('system.log.result'),
      width: 110,
      cellRender: { name: 'CellTag' },
    },
    { field: 'durationMs', title: $t('system.log.duration'), width: 110 },
    { field: 'ip', title: 'IP', width: 140 },
    { field: 'operation', title: $t('system.log.operation'), minWidth: 200 },
    { field: 'error', title: $t('system.log.error'), minWidth: 200 },
    { field: 'time', title: $t('system.log.time'), width: 180 },
  ];
}
