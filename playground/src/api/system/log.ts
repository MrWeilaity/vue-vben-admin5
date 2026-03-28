import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemLogApi {
  export interface OperationLog {
    durationMs: number;
    error?: string;
    ip?: string;
    method?: string;
    operation?: string;
    path?: string;
    result?: string;
    time?: string;
    username?: string;
  }
}

/**
 * 获取操作日志列表
 */
async function getOperationLogList(params: Recordable<any> = {}) {
  return requestClient.get<Array<SystemLogApi.OperationLog>>(
    '/system/log/operation/list',
    { params },
  );
}

export { getOperationLogList };
