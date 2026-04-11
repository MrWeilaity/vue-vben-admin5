import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemDictApi {
  export interface DictType {
    builtIn?: 0 | 1;
    cacheEnabled: 0 | 1;
    code: string;
    createTime?: string;
    id: number;
    name: string;
    remark?: string;
    sortOrder: number;
    status: 0 | 1;
  }

  export interface DictData {
    createTime?: string;
    cssStyle?: string;
    extJson?: Record<string, any>;
    id: number;
    isDefault: 0 | 1;
    label: string;
    remark?: string;
    sortOrder: number;
    status: 0 | 1;
    tagClass?: string;
    tagType?: string;
    typeCode: string;
    typeId: number;
    value: string;
  }
}

/** 查询字典类型分页列表。 */
export async function getDictTypeList(params: Recordable<any> = {}) {
  return requestClient.get('/system/dict/type/list', { params });
}

/** 查询字典类型下拉。 */
export async function getDictTypeOptions() {
  return requestClient.get<Array<SystemDictApi.DictType>>(
    '/system/dict/type/options',
  );
}

/** 新增字典类型。 */
export async function createDictType(data: Omit<SystemDictApi.DictType, 'id'>) {
  return requestClient.post('/system/dict/type', data);
}

/** 更新字典类型。 */
export async function updateDictType(
  id: number,
  data: Omit<SystemDictApi.DictType, 'id'>,
) {
  return requestClient.put(`/system/dict/type/${id}`, data);
}

/** 更新字典类型状态。 */
export async function updateDictTypeStatus(id: number, status: 0 | 1) {
  return requestClient.put(`/system/dict/type/status/${id}`, { status });
}

/** 删除字典类型。 */
export async function deleteDictType(id: number) {
  return requestClient.delete(`/system/dict/type/${id}`);
}

/** 查询字典项分页列表。 */
export async function getDictDataList(params: Recordable<any> = {}) {
  return requestClient.get('/system/dict/data/list', { params });
}

/** 新增字典项。 */
export async function createDictData(data: Omit<SystemDictApi.DictData, 'id'>) {
  return requestClient.post('/system/dict/data', data);
}

/** 更新字典项。 */
export async function updateDictData(
  id: number,
  data: Omit<SystemDictApi.DictData, 'id'>,
) {
  return requestClient.put(`/system/dict/data/${id}`, data);
}

/** 更新字典项状态。 */
export async function updateDictDataStatus(id: number, status: 0 | 1) {
  return requestClient.put(`/system/dict/data/status/${id}`, { status });
}

/** 删除字典项。 */
export async function deleteDictData(id: number) {
  return requestClient.delete(`/system/dict/data/${id}`);
}

/** 刷新字典缓存。 */
export async function refreshDictCache() {
  return requestClient.post('/system/dict/refreshCache');
}

/** 业务消费：读取单个字典。 */
export async function getDictByTypeCode(typeCode: string, onlyEnabled = true) {
  return requestClient.get<Array<SystemDictApi.DictData>>(
    '/system/dict/consume/items',
    {
      params: { onlyEnabled, typeCode },
    },
  );
}

/** 业务消费：批量读取字典。 */
export async function getDictByTypeCodes(
  typeCodes: string[],
  onlyEnabled = true,
) {
  return requestClient.get<Record<string, Array<SystemDictApi.DictData>>>(
    '/system/dict/consume/batch',
    {
      params: {
        onlyEnabled,
        typeCodes: typeCodes.join(','),
      },
    },
  );
}
