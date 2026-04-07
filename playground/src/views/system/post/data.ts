import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemPostApi } from '#/api';

import { useAccess } from '@vben/access';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.post.postName'),
      rules: 'required',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
        optionType: 'button',
      },
      defaultValue: 1,
      fieldName: 'status',
      label: $t('system.post.status'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.post.remark'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.post.postName'),
    },
    { component: 'Input', fieldName: 'id', label: $t('system.post.id') },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.post.status'),
    },
    {
      component: 'Input',
      fieldName: 'remark',
      label: $t('system.post.remark'),
    },
    {
      component: 'RangePicker',
      fieldName: 'createTime',
      label: $t('system.post.createTime'),
    },
  ];
}

export function useColumns<T = SystemPostApi.SystemPost>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
  const { hasAccessByCodes } = useAccess();

  return [
    {
      field: 'name',
      title: $t('system.post.postName'),
      width: 200,
    },
    {
      field: 'id',
      title: $t('system.post.id'),
      width: 140,
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      title: $t('system.post.status'),
      width: 100,
    },
    {
      field: 'remark',
      minWidth: 180,
      title: $t('system.post.remark'),
    },
    {
      field: 'createTime',
      title: $t('system.post.createTime'),
      width: 200,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'name',
          nameTitle: $t('system.post.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'edit',
            show: () => hasAccessByCodes(['System:Post:Edit']),
          },
          {
            code: 'delete',
            show: () => hasAccessByCodes(['System:Post:Delete']),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.post.operation'),
      width: 130,
    },
  ];
}
