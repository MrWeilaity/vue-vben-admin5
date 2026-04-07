import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemPostApi {
  export interface SystemPost {
    [key: string]: any;
    createTime?: string;
    id: number;
    name: string;
    remark?: string;
    status: 0 | 1;
  }
}

/**
 * 获取岗位分页列表。
 */
async function getPostList(params: Recordable<any> = {}) {
  return requestClient.get('/system/post/list', { params });
}

/**
 * 获取全部岗位列表。
 */
async function getPostAllList(params: Recordable<any> = {}) {
  return requestClient.get<Array<SystemPostApi.SystemPost>>(
    '/system/post/allList',
    { params },
  );
}

/**
 * 创建岗位。
 */
async function createPost(data: Omit<SystemPostApi.SystemPost, 'id'>) {
  return requestClient.post('/system/post', data);
}

/**
 * 更新岗位。
 */
async function updatePost(
  id: number,
  data: Omit<SystemPostApi.SystemPost, 'id'>,
) {
  return requestClient.put(`/system/post/${id}`, data);
}

/**
 * 更新岗位状态。
 */
async function updateStatusPost(
  id: number,
  data: Omit<SystemPostApi.SystemPost, 'id'>,
) {
  return requestClient.put(`/system/post/status/${id}`, data);
}

/**
 * 删除岗位。
 */
async function deletePost(id: number) {
  return requestClient.delete(`/system/post/${id}`);
}

export {
  createPost,
  deletePost,
  getPostAllList,
  getPostList,
  updatePost,
  updateStatusPost,
};
