<script lang="ts" setup>
import type { SystemDictApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createDictType, updateDictType } from '#/api';
import { $t } from '#/locales';

import { useTypeFormSchema } from '../data';

const emits = defineEmits(['success']);
const formData = ref<SystemDictApi.DictType>();
const id = ref<number>();

const [Form, formApi] = useVbenForm({
  schema: useTypeFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    (id.value
      ? updateDictType(id.value, values as any)
      : createDictType(values as any)
    )
      .then(() => {
        emits('success');
        drawerApi.close();
      })
      .catch(() => drawerApi.unlock());
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = drawerApi.getData<SystemDictApi.DictType>();
    formApi.resetForm();
    id.value = data?.id;
    formData.value = data;
    await nextTick();
    if (data) {
      formApi.setValues(data);
    }
  },
});

const getDrawerTitle = computed(() =>
  formData.value?.id
    ? $t('common.edit', $t('system.dict.name'))
    : $t('common.create', $t('system.dict.name')),
);
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
