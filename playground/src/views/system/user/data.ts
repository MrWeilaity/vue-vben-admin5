import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { useAccess } from '@vben/access';

import { getDeptList } from '#/api/system/dept';
import { getPostAllList } from '#/api/system/post';
import { getRoleAllList } from '#/api/system/role';
import { $t } from '#/locales';

const SYSTEM_ADMIN_USER_ID = 1;

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
      component: 'Select',
      fieldName: 'dataScope',
      label: $t('system.dataScope.name'),
      defaultValue: 5,
      rules: 'required',
      componentProps: {
        class: 'w-full',
        options: [
          { label: $t('system.dataScope.all'), value: 1 },
          { label: $t('system.dataScope.custom'), value: 2 },
          { label: $t('system.dataScope.dept'), value: 3 },
          { label: $t('system.dataScope.deptAndChild'), value: 4 },
          { label: $t('system.dataScope.self'), value: 5 },
        ],
      },
    },
    {
      component: 'Input',
      fieldName: 'dataScopeDeptIds',
      label: $t('system.dataScope.customDept'),
      formItemClass: 'items-start',
      modelPropName: 'modelValue',
      dependencies: {
        show: (values) => values.dataScope === 2,
        triggerFields: ['dataScope'],
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
  const { hasAccessByCodes } = useAccess();

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
      field: 'dataScope',
      formatter: ({ cellValue }) =>
        ({
          1: $t('system.dataScope.all'),
          2: $t('system.dataScope.custom'),
          3: $t('system.dataScope.dept'),
          4: $t('system.dataScope.deptAndChild'),
          5: $t('system.dataScope.self'),
        })[cellValue as 1 | 2 | 3 | 4 | 5] ?? '',
      title: $t('system.dataScope.name'),
      width: 160,
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
          {
            code: 'edit',
            show: () => hasAccessByCodes(['System:User:Edit']),
          },
          {
            code: 'reset-password',
            text: $t('system.user.resetPassword'),
            show: () => hasAccessByCodes(['System:User:ResetPassword']),
          },
          {
            code: 'delete',
            show: (row: T) =>
              hasAccessByCodes(['System:User:Delete']) &&
              (row as SystemUserApi.SystemUser).id !== SYSTEM_ADMIN_USER_ID,
          },
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
