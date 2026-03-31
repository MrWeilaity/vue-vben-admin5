import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemPostApi {
  export interface SystemPost {
    [key: string]: any;
    id: string;
    name: string;
    remark?: string;
    status: 0 | 1;
  }
}

async function getPostList(params: Recordable<any> = {}) {
  return requestClient.get<Array<SystemPostApi.SystemPost>>(
    '/system/post/list',
    { params },
  );
}

export { getPostList };
