import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemLogApi {
  export interface OperationLog {
    actionType?: string;
    bizStatusCode?: number;
    clientAddress?: string;
    clientIp?: string;
    createTime?: string;
    durationMs: number;
    errorMessage?: string;
    extData?: Recordable<any>;
    httpStatusCode?: number;
    id?: number;
    module?: string;
    occurTime?: string;
    operationDesc?: string;
    operatorDept?: string;
    operatorUsername?: string;
    requestMethod?: string;
    requestParams?: string;
    requestUrl?: string;
    success?: number;
  }
}

/**
 * 获取操作日志列表
 */
async function getOperationLogList(params: Recordable<any> = {}) {
  return await requestClient.get<Array<SystemLogApi.OperationLog>>(
    '/system/log/operation/list',
    { params },
  );
}

export { getOperationLogList };
