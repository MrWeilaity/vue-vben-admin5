import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemOnlineApi {
  export interface OnlineUser {
    browser?: string;
    current?: boolean;
    deptName?: string;
    deviceType?: string;
    expiresAt?: string;
    lastAccessTime?: string;
    loginAddress?: string;
    loginIp?: string;
    loginTime?: string;
    os?: string;
    sessionId: string;
    username?: string;
  }
}

export async function getOnlineUserList(params: Recordable<any> = {}) {
  return await requestClient.get<Array<SystemOnlineApi.OnlineUser>>(
    '/system/online/list',
    { params },
  );
}

export async function offlineSession(sessionId: string) {
  return await requestClient.post(`/system/online/${sessionId}/offline`);
}
