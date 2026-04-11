import { computed, ref } from 'vue';

import { getDictByTypeCode, getDictByTypeCodes } from '#/api';

/**
 * 字典前端消费能力。
 * - 统一从后端字典中心读取
 * - 每次读取都实时请求后端，避免前端本地缓存造成数据不一致
 */
export function useDict(typeCode: string) {
  const loading = ref(false);
  const options = ref<any[]>([]);

  async function load(_force = false) {
    loading.value = true;
    try {
      const data = await getDictByTypeCode(typeCode, true);
      options.value = data;
      return data;
    } finally {
      loading.value = false;
    }
  }

  function getLabel(value: number | string | undefined) {
    const target = options.value.find((item) => `${item.value}` === `${value}`);
    return target?.label ?? value;
  }

  const selectOptions = computed(() =>
    options.value.map((item) => ({ label: item.label, value: item.value })),
  );

  return {
    getLabel,
    load,
    loading,
    options,
    selectOptions,
  };
}

/** 批量预取字典（仅发起请求，不落前端缓存）。 */
export async function preloadDict(typeCodes: string[]) {
  if (!typeCodes?.length) {
    return;
  }
  await getDictByTypeCodes(typeCodes, true);
}

/** 清理前端字典内存缓存（兼容保留，无实际缓存可清理）。 */
export function clearDictCache(_typeCode?: string) {
  // no-op: 前端不再维护字典内存缓存
}
