import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api';

import { useAccess } from '@vben/access';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.role.roleName'),
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
      label: $t('system.role.status'),
    },
    {
      component: 'Select',
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
      defaultValue: 5,
      fieldName: 'dataScope',
      label: $t('system.dataScope.name'),
      rules: 'required',
    },
    {
      component: 'Input',
      dependencies: {
        show: (values) => values.dataScope === 2,
        triggerFields: ['dataScope'],
      },
      fieldName: 'dataScopeDeptIds',
      formItemClass: 'items-start',
      label: $t('system.dataScope.customDept'),
      modelPropName: 'modelValue',
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.role.remark'),
    },
    {
      component: 'Input',
      fieldName: 'permissions',
      formItemClass: 'items-start',
      label: $t('system.role.setPermissions'),
      modelPropName: 'modelValue',
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.role.roleName'),
    },
    { component: 'Input', fieldName: 'id', label: $t('system.role.id') },
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
      label: $t('system.role.status'),
    },
    {
      component: 'Input',
      fieldName: 'remark',
      label: $t('system.role.remark'),
    },
    {
      component: 'RangePicker',
      fieldName: 'createTime',
      label: $t('system.role.createTime'),
    },
  ];
}

export function useColumns<T = SystemRoleApi.SystemRole>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
  const { hasAccessByCodes } = useAccess();

  return [
    {
      field: 'name',
      title: $t('system.role.roleName'),
      width: 200,
    },
    {
      field: 'id',
      title: $t('system.role.id'),
      width: 200,
    },
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
      minWidth: 100,
      title: $t('system.role.remark'),
    },
    {
      field: 'createTime',
      title: $t('system.role.createTime'),
      width: 200,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'name',
          nameTitle: $t('system.role.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'edit',
            show: () => hasAccessByCodes(['System:Role:Edit']),
          },
          {
            code: 'delete',
            show: () => hasAccessByCodes(['System:Role:Delete']),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.role.operation'),
      width: 130,
    },
  ];
}
