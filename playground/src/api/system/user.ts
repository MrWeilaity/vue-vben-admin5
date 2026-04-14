import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemUserApi {
  export interface SystemUser {
    [key: string]: any;
    createTime?: string;
    dataScope: 1 | 2 | 3 | 4 | 5;
    dataScopeDeptIds?: string[];
    deptId: string;
    email?: string;
    id: number;
    mobile?: string;
    nickname: string;
    postIds?: string[];
    remark?: string;
    roleIds: string[];
    status: 0 | 1;
    username: string;
  }
}

async function getUserList(params: Recordable<any>) {
  return requestClient.get<Array<SystemUserApi.SystemUser>>(
    '/system/user/list',
    {
      params,
    },
  );
}

async function createUser(data: Omit<SystemUserApi.SystemUser, 'id'>) {
  return requestClient.post('/system/user', data);
}

async function updateUser(
  id: number,
  data: Omit<SystemUserApi.SystemUser, 'id'>,
) {
  return requestClient.put(`/system/user/${id}`, data);
}

async function deleteUser(id: number) {
  return requestClient.delete(`/system/user/${id}`);
}

async function resetUserPassword(userId: number, newPassword: string) {
  return requestClient.post(`/system/user/reset-password/${userId}`, {
    newPassword,
  });
}

export { createUser, deleteUser, getUserList, resetUserPassword, updateUser };
