<script lang="ts" setup>
import type { DataNode } from 'ant-design-vue/es/tree';

import type { SystemUserApi } from '#/api/system/user';

import { computed, ref } from 'vue';

import { Tree, useVbenDrawer } from '@vben/common-ui';

import { Spin } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getDeptList } from '#/api/system/dept';
import { createUser, updateUser } from '#/api/system/user';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemUserApi.SystemUser>();
const id = ref<number>();
const dataScopeDepts = ref<DataNode[]>([]);
const loadingDataScopeDepts = ref(false);

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    const payload = {
      ...values,
      dataScopeDeptIds:
        values.dataScope === 2 ? (values.dataScopeDeptIds ?? []) : [],
      postIds: values.postIds ?? [],
      roleIds: values.roleIds ?? [],
    } as any;
    const { password, ...updatePayload } = payload;
    const request = id.value
      ? updateUser(id.value, updatePayload)
      : createUser({ ...payload, password });

    request
      .then(() => {
        emits('success');
        drawerApi.close();
      })
      .catch(() => {
        drawerApi.unlock();
      });
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemUserApi.SystemUser>();
      formApi.resetForm();
      if (data) {
        id.value = data.id;
        formData.value = data;
        formApi.setValues(data);
      } else {
        id.value = undefined;
        formData.value = undefined;
      }
      if (dataScopeDepts.value.length === 0) {
        await loadDataScopeDepts();
      }
    }
  },
});

async function loadDataScopeDepts() {
  loadingDataScopeDepts.value = true;
  try {
    const res = await getDeptList();
    dataScopeDepts.value = res as unknown as DataNode[];
  } finally {
    loadingDataScopeDepts.value = false;
  }
}

const getDrawerTitle = computed(() => {
  return formData.value?.id
    ? $t('common.edit', $t('system.user.name'))
    : $t('common.create', $t('system.user.name'));
});
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form>
      <template #dataScopeDeptIds="slotProps">
        <Spin :spinning="loadingDataScopeDepts" wrapper-class-name="w-full">
          <Tree
            :tree-data="dataScopeDepts"
            multiple
            bordered
            :default-expanded-level="2"
            v-bind="slotProps"
            value-field="id"
            label-field="name"
          />
        </Spin>
      </template>
    </Form>
  </Drawer>
</template>
