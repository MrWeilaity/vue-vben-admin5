<script lang="ts" setup>
import type { SystemDictApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createDictData, updateDictData } from '#/api';
import { clearDictCache } from '#/composables/use-dict';
import { $t } from '#/locales';

import { useDataFormSchema } from '../data';

const emits = defineEmits(['success']);
const formData = ref<SystemDictApi.DictData>();
const id = ref<number>();
const currentTypeCode = ref<string>('');

const [Form, formApi] = useVbenForm({
  schema: useDataFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    (id.value
      ? updateDictData(id.value, {
          ...values,
          typeCode: currentTypeCode.value,
        } as any)
      : createDictData({ ...values, typeCode: currentTypeCode.value } as any)
    )
      .then(() => {
        clearDictCache(currentTypeCode.value);
        emits('success');
        drawerApi.close();
      })
      .catch(() => drawerApi.unlock());
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = drawerApi.getData<{
      item?: SystemDictApi.DictData;
      typeCode: string;
    }>();
    formApi.resetForm();
    currentTypeCode.value = data?.typeCode ?? '';
    id.value = data?.item?.id;
    formData.value = data?.item;
    await nextTick();
    if (data?.item) {
      formApi.setValues(data.item);
    }
  },
});

const getDrawerTitle = computed(() =>
  formData.value?.id
    ? $t('common.edit', $t('system.dict.dataName'))
    : $t('common.create', $t('system.dict.dataName')),
);
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
