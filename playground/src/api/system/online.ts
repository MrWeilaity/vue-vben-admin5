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

export async function getOnlineUserList() {
  return await requestClient.get<Array<SystemOnlineApi.OnlineUser>>(
    '/system/online/list',
  );
}

export async function offlineSession(sessionId: string) {
  return await requestClient.post(`/system/online/${sessionId}/offline`);
}
