import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { useAccess } from '@vben/access';

import { $t } from '#/locales';

/** 字典类型编辑表单。 */
export function useTypeFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.dict.typeName'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'code',
      help: $t('system.dict.typeCodeHelp'),
      label: $t('system.dict.typeCode'),
      rules: 'required',
    },
    {
      component: 'InputNumber',
      componentProps: { min: 0 },
      defaultValue: 0,
      fieldName: 'sortOrder',
      label: $t('system.dict.sortOrder'),
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
      fieldName: 'cacheEnabled',
      help: $t('system.dict.cacheEnabledHelp'),
      label: $t('system.dict.cacheEnabled'),
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
      label: $t('system.dict.status'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.dict.remark'),
    },
  ];
}

/** 字典项编辑表单。 */
export function useDataFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'label',
      label: $t('system.dict.label'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'value',
      label: $t('system.dict.value'),
      rules: 'required',
    },
    {
      component: 'InputNumber',
      componentProps: { min: 0 },
      defaultValue: 0,
      fieldName: 'sortOrder',
      label: $t('system.dict.sortOrder'),
      rules: 'required',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
        optionType: 'button',
      },
      defaultValue: 0,
      fieldName: 'isDefault',
      help: $t('system.dict.defaultHelp'),
      label: $t('system.dict.isDefault'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: 'default', value: 'default' },
          { label: 'success', value: 'success' },
          { label: 'error', value: 'error' },
          { label: 'warning', value: 'warning' },
          { label: 'processing', value: 'processing' },
        ],
      },
      fieldName: 'tagType',
      help: $t('system.dict.tagTypeHelp'),
      label: $t('system.dict.tagType'),
    },
    {
      component: 'Input',
      fieldName: 'tagClass',
      label: $t('system.dict.tagClass'),
    },
    {
      component: 'Input',
      fieldName: 'cssStyle',
      label: $t('system.dict.cssStyle'),
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
      label: $t('system.dict.status'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.dict.remark'),
    },
  ];
}

export function useTypeColumns<T = SystemDictApi.DictType>(
  onActionClick: OnActionClickFn<T>,
) {
  const { hasAccessByCodes } = useAccess();
  const columns: VxeTableGridColumns = [
    { field: 'name', title: $t('system.dict.typeName'), minWidth: 180 },
    { field: 'code', title: $t('system.dict.typeCode'), minWidth: 180 },
    { field: 'sortOrder', title: $t('system.dict.sortOrder'), width: 100 },
    {
      field: 'cacheEnabled',
      title: $t('system.dict.cacheEnabled'),
      width: 110,
      cellRender: { name: 'CellTag' },
    },
    {
      field: 'status',
      title: $t('system.dict.status'),
      width: 100,
      cellRender: { name: 'CellTag' },
    },
    { field: 'remark', title: $t('system.dict.remark'), minWidth: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'name',
          nameTitle: $t('system.dict.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'manage-items',
            show: () => hasAccessByCodes(['System:Dict:List']),
            text: $t('system.dict.manageItems'),
          },
          { code: 'edit', show: () => hasAccessByCodes(['System:Dict:Edit']) },
          {
            code: 'delete',
            show: () => hasAccessByCodes(['System:Dict:Delete']),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.dict.operation'),
      width: 130,
    },
  ];
  return columns;
}

export function useDataColumns<T = SystemDictApi.DictData>(
  onActionClick: OnActionClickFn<T>,
) {
  const { hasAccessByCodes } = useAccess();
  const columns: VxeTableGridColumns = [
    { field: 'label', title: $t('system.dict.label'), minWidth: 150 },
    { field: 'value', title: $t('system.dict.value'), minWidth: 120 },
    { field: 'sortOrder', title: $t('system.dict.sortOrder'), width: 90 },
    {
      field: 'isDefault',
      title: $t('system.dict.isDefault'),
      width: 90,
      cellRender: { name: 'CellTag' },
    },
    {
      field: 'status',
      title: $t('system.dict.status'),
      width: 90,
      cellRender: { name: 'CellTag' },
    },
    { field: 'tagType', title: $t('system.dict.tagType'), width: 120 },
    { field: 'remark', title: $t('system.dict.remark'), minWidth: 150 },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'label',
          nameTitle: $t('system.dict.dataName'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          { code: 'edit', show: () => hasAccessByCodes(['System:Dict:Edit']) },
          {
            code: 'delete',
            show: () => hasAccessByCodes(['System:Dict:Delete']),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.dict.operation'),
      width: 130,
    },
  ];
  return columns;
}
