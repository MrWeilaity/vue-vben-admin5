<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemLogApi } from '#/api/system/log';

import { computed, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Descriptions, Modal, Tag, TypographyParagraph } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOperationLogList } from '#/api/system/log';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';

const detailVisible = ref(false);
const currentLog = ref<null | SystemLogApi.OperationLog>(null);

const requestParamsText = computed(() =>
  formatValue(currentLog.value?.requestParams),
);
const responseText = computed(() =>
  formatValue(currentLog.value?.extData?.response),
);

function formatValue(value: unknown) {
  if (value === null || value === '') {
    return '-';
  }
  if (typeof value === 'string') {
    return value;
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function getStatusColor(success?: number) {
  return success === 1 ? 'success' : 'error';
}

function getStatusText(success?: number) {
  return success === 1 ? $t('system.log.success') : $t('system.log.fail');
}

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemLogApi.OperationLog>) {
  if (code === 'detail') {
    currentLog.value = row;
    detailVisible.value = true;
  }
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['time', ['startTime', 'endTime'], 'YYYY-MM-DD']],
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getOperationLogList({
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
    <Grid :table-title="$t('system.log.list')" />
    <Modal
      v-model:open="detailVisible"
      :body-style="{ maxHeight: '70vh', overflowY: 'auto' }"
      :title="$t('system.log.detail')"
      :footer="null"
      :style="{ top: '5vh' }"
      width="960px"
    >
      <Descriptions bordered :column="2" size="small">
        <Descriptions.Item :label="$t('system.log.username')">
          {{ currentLog?.operatorUsername || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.module')">
          {{ currentLog?.module || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.status')">
          <Tag :color="getStatusColor(currentLog?.success)">
            {{ getStatusText(currentLog?.success) }}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.time')">
          {{ currentLog?.occurTime || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.method')">
          {{ currentLog?.requestMethod || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.path')">
          {{ currentLog?.requestUrl || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.ip')">
          {{ currentLog?.clientIp || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.address')">
          {{ currentLog?.clientAddress || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.operation')" :span="2">
          {{ currentLog?.operationDesc || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.duration')">
          {{ currentLog?.durationMs ?? '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.actionType')">
          {{ currentLog?.actionType || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.httpStatus')">
          {{ currentLog?.httpStatusCode ?? '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.bizStatus')">
          {{ currentLog?.bizStatusCode ?? '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.operatorDept')">
          {{ currentLog?.operatorDept || '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.id')">
          {{ currentLog?.id ?? '-' }}
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.error')" :span="2">
          <TypographyParagraph :ellipsis="false" style="white-space: pre-wrap">
            {{ formatValue(currentLog?.errorMessage) }}
          </TypographyParagraph>
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.requestParams')" :span="2">
          <TypographyParagraph :ellipsis="false" style="white-space: pre-wrap">
            {{ requestParamsText }}
          </TypographyParagraph>
        </Descriptions.Item>
        <Descriptions.Item :label="$t('system.log.response')" :span="2">
          <TypographyParagraph :ellipsis="false" style="white-space: pre-wrap">
            {{ responseText }}
          </TypographyParagraph>
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  </Page>
</template>
