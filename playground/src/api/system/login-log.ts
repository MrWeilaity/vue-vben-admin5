import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemLoginLogApi {
  export interface LoginLog {
    browser?: string;
    id?: number;
    loginAddress?: string;
    loginIp?: string;
    loginTime?: string;
    operationMsg?: string;
    os?: string;
    status?: number;
    username?: string;
  }
}

export async function getLoginLogList(params: Recordable<any> = {}) {
  return await requestClient.get<Array<SystemLoginLogApi.LoginLog>>(
    '/system/log/login/list',
    { params },
  );
}
