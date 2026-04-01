import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemLogApi } from '#/api/system/log';

import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Select',
      fieldName: 'success',
      label: $t('system.log.status'),
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.log.success'), value: 1 },
          { label: $t('system.log.fail'), value: 0 },
        ],
      },
    },
    {
      component: 'RangePicker',
      fieldName: 'time',
      label: $t('system.log.time'),
    },
    {
      component: 'Input',
      fieldName: 'module',
      label: $t('system.log.module'),
    },
  ];
}

export function useColumns<T = SystemLogApi.OperationLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridColumns<T> {
  return [
    {
      field: 'operatorUsername',
      title: $t('system.log.username'),
      width: 140,
    },
    { field: 'module', title: $t('system.log.module'), width: 180 },
    { field: 'requestMethod', title: $t('system.log.method'), width: 100 },
    { field: 'requestUrl', title: $t('system.log.path'), minWidth: 220 },
    {
      field: 'success',
      title: $t('system.log.status'),
      width: 110,
      cellRender: {
        name: 'CellTag',
        options: [
          { color: 'success', label: $t('system.log.success'), value: 1 },
          { color: 'error', label: $t('system.log.fail'), value: 0 },
        ],
      },
    },
    { field: 'durationMs', title: $t('system.log.duration'), width: 110 },
    { field: 'clientIp', title: 'IP', width: 140 },
    { field: 'clientAddress', title: $t('system.log.address'), minWidth: 200 },
    {
      field: 'operationDesc',
      title: $t('system.log.operation'),
      minWidth: 200,
    },
    { field: 'errorMessage', title: $t('system.log.error'), minWidth: 200 },
    { field: 'occurTime', title: $t('system.log.time'), width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'operatorUsername',
          nameTitle: $t('system.log.title'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [{ code: 'detail', text: $t('system.log.detail') }],
      },
      field: 'actions',
      fixed: 'right',
      title: $t('system.log.actions'),
      width: 100,
    },
  ];
}
