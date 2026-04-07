import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { getDeptList } from '#/api/system/dept';
import { getPostAllList } from '#/api/system/post';
import { getRoleAllList } from '#/api/system/role';
import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        show: false,
        triggerFields: ['username'],
      },
    },
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
      rules: 'required',
      dependencies: {
        show(values) {
          return !values.id;
        },
        triggerFields: ['id'],
      },
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: $t('system.user.nickname'),
      rules: 'required',
    },
    {
      component: 'ApiTreeSelect',
      fieldName: 'deptId',
      label: $t('system.dept.name'),
      rules: 'required',
      componentProps: {
        allowClear: true,
        api: getDeptList,
        class: 'w-full',
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
      },
    },
    {
      component: 'ApiSelect',
      fieldName: 'roleIds',
      label: $t('system.role.name'),
      componentProps: {
        api: getRoleAllList,
        class: 'w-full',
        mode: 'multiple',
        afterFetch: (data: { id: number; name: string }[]) =>
          data.map((item) => ({
            label: item.name,
            value: item.id,
          })),
      },
    },
    {
      component: 'ApiSelect',
      fieldName: 'postIds',
      label: '岗位',
      componentProps: {
        api: getPostAllList,
        class: 'w-full',
        mode: 'multiple',
        afterFetch: (data: { id: number; name: string }[]) =>
          data.map((item) => ({
            label: item.name,
            value: item.id,
          })),
      },
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
      rules: 'required',
      dependencies: {
        show(values) {
          return !values.id;
        },
        triggerFields: ['id'],
      },
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

export function useColumns<T = SystemUserApi.SystemUser>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns<T> {
  return [
    { field: 'username', title: $t('system.user.username'), width: 200 },
    { field: 'nickname', title: $t('system.user.nickname'), width: 200 },
    { field: 'email', title: $t('system.user.email'), width: 200 },
    { field: 'mobile', title: $t('system.user.mobile'), width: 200 },

    {
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      title: $t('system.role.status'),
      width: 100,
    },
    {
      width: 160,
      field: 'dept',
      title: $t('system.user.dept'),
    },
    {
      field: 'remark',
      title: $t('system.user.remark'),
      minWidth: 100,
    },
    { field: 'createTime', title: $t('system.user.createTime'), width: 180 },
    {
      field: 'operation',
      title: $t('system.user.operation'),
      width: 200,
      fixed: 'right',
      align: 'center',
      cellRender: {
        name: 'CellOperation',
        options: [
          'edit',
          { code: 'reset-password', text: $t('system.user.resetPassword') },
          'delete',
        ],
        attrs: {
          nameField: 'username',
          nameTitle: $t('system.user.name'),
          onClick: onActionClick,
        },
      },
    },
  ];
}
