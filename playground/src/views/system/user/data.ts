import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: $t('system.user.nickname'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'deptId',
      label: $t('system.user.dept'),
      rules: 'required',
    },
    { component: 'Input', fieldName: 'email', label: $t('system.user.email') },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: $t('system.user.mobile'),
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: $t('system.user.status'),
      defaultValue: 1,
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
    },
    {
      component: 'InputPassword',
      fieldName: 'password',
      label: $t('authentication.password'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.user.remark'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: $t('system.user.nickname'),
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: $t('system.user.status'),
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
    },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemUserApi.SystemUser>,
): VxeTableGridColumns<SystemUserApi.SystemUser> {
  return [
    { field: 'username', title: $t('system.user.username'), width: 160 },
    { field: 'nickname', title: $t('system.user.nickname'), width: 160 },
    { field: 'email', title: $t('system.user.email'), width: 200 },
    { field: 'mobile', title: $t('system.user.mobile'), width: 160 },
    {
      field: 'status',
      title: $t('system.user.status'),
      width: 100,
      cellRender: { name: 'CellTag' },
    },
    { field: 'createTime', title: $t('system.user.createTime'), width: 180 },
    {
      field: 'operation',
      title: $t('system.user.operation'),
      width: 130,
      fixed: 'right',
      align: 'center',
      cellRender: {
        name: 'CellOperation',
        attrs: {
          nameField: 'username',
          nameTitle: $t('system.user.name'),
          onClick: onActionClick,
        },
      },
    },
  ];
}
