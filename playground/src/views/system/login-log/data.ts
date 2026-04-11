import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { $t } from '#/locales';

export function useGridFormSchema() {
  return [
    {
      component: 'Input',
      componentProps: { allowClear: true, placeholder: $t('system.loginLog.username') },
      fieldName: 'username',
      label: $t('system.loginLog.username'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.log.success'), value: 1 },
          { label: $t('system.log.fail'), value: 0 },
        ],
        placeholder: $t('system.log.status'),
      },
      fieldName: 'status',
      label: $t('system.log.status'),
    },
    {
      component: 'RangePicker',
      fieldName: 'time',
      label: $t('system.log.time'),
    },
  ];
}

export function useColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'username', title: $t('system.loginLog.username'), width: 140 },
    { field: 'loginIp', title: $t('system.loginLog.ip'), width: 150 },
    { field: 'loginAddress', title: $t('system.loginLog.address'), minWidth: 180 },
    { field: 'browser', title: $t('system.loginLog.browser'), width: 140 },
    { field: 'os', title: $t('system.loginLog.os'), width: 140 },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { color: 'success', label: $t('system.log.success'), value: 1 },
          { color: 'error', label: $t('system.log.fail'), value: 0 },
        ],
      },
      field: 'status',
      title: $t('system.log.status'),
      width: 100,
    },
    { field: 'operationMsg', title: $t('system.loginLog.operationMsg'), minWidth: 220 },
    { field: 'loginTime', title: $t('system.loginLog.loginTime'), width: 180 },
  ];
}
