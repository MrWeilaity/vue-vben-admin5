<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { h, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, InputPassword, message, Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteUser,
  getUserList,
  resetUserPassword,
  updateUser,
} from '#/api/system/user';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});
const resetPasswordValue = ref('');
const SYSTEM_ADMIN_USER_ID = 1;

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemUserApi.SystemUser>) {
  switch (code) {
    case 'delete': {
      if (row.id === SYSTEM_ADMIN_USER_ID) {
        message.warning('系统管理员账号不允许删除');
        return;
      }
      const hide = message.loading({
        content: $t('ui.actionMessage.deleting', [row.username]),
        duration: 0,
        key: 'action_process_msg',
      });
      deleteUser(row.id)
        .then(() => {
          message.success({
            content: $t('ui.actionMessage.deleteSuccess', [row.username]),
            key: 'action_process_msg',
          });
          onRefresh();
        })
        .catch(() => hide());

      break;
    }
    case 'edit': {
      formDrawerApi.setData(row).open();

      break;
    }
    case 'reset-password': {
      onResetPassword(row);

      break;
    }
    // No default
  }
}

function confirm(content: string, title: string) {
  return new Promise((reslove, reject) => {
    Modal.confirm({
      content,
      onCancel() {
        reject(new Error('已取消'));
      },
      onOk() {
        reslove(true);
      },
      title,
    });
  });
}
/**
 * 状态开关即将改变
 * @param newStatus 期望改变的状态值
 * @param row 行数据
 * @returns 返回false则中止改变，返回其他值（undefined、true）则允许改变
 */
async function onStatusChange(
  newStatus: number,
  row: SystemUserApi.SystemUser,
) {
  const status: Recordable<string> = {
    0: '禁用',
    1: '启用',
  };
  try {
    await confirm(
      `你要将${row.username}的状态切换为 【${status[newStatus.toString()]}】 吗？`,
      `切换状态`,
    );
    await updateUser(row.id, { status: newStatus });
    return true;
  } catch {
    return false;
  }
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({ status: 1 }).open();
}

function onResetPassword(row: SystemUserApi.SystemUser) {
  resetPasswordValue.value = '';
  Modal.confirm({
    title: `重置 ${row.username} 的密码`,
    content: h(InputPassword, {
      autofocus: true,
      placeholder: '请输入新密码',
      value: resetPasswordValue.value,
      'onUpdate:value': (value) => {
        resetPasswordValue.value = value;
      },
    }),
    async onOk() {
      if (!resetPasswordValue.value) {
        message.warning('请输入新密码');
        throw new Error('请输入新密码');
      }
      const hide = message.loading({
        content: '正在重置密码...',
        duration: 0,
        key: 'reset_password_msg',
      });
      try {
        await resetUserPassword(row.id, resetPasswordValue.value);
        message.success('重置密码成功');
        hide();
      } catch {
        hide();
        message.error('重置密码失败');
      }
    },
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema(), submitOnChange: true },
  gridOptions: {
    columns: useColumns(onActionClick, onStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserList({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions,
});
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.user.list')">
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>
