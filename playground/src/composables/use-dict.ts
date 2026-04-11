import { computed, ref } from 'vue';

import { getDictByTypeCode, getDictByTypeCodes } from '#/api';

const dictCache = new Map<string, any[]>();

/**
 * 字典前端消费能力。
 * - 统一从后端字典中心读取
 * - 支持内存缓存与批量预取
 */
export function useDict(typeCode: string) {
  const loading = ref(false);
  const options = ref<any[]>([]);

  async function load(force = false) {
    if (!force && dictCache.has(typeCode)) {
      options.value = dictCache.get(typeCode) ?? [];
      return options.value;
    }
    loading.value = true;
    try {
      const data = await getDictByTypeCode(typeCode, true);
      dictCache.set(typeCode, data);
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

/** 批量预取字典，减少多次网络请求。 */
export async function preloadDict(typeCodes: string[]) {
  const missTypeCodes = typeCodes.filter((item) => !dictCache.has(item));
  if (missTypeCodes.length === 0) {
    return;
  }
  const map = await getDictByTypeCodes(missTypeCodes, true);
  Object.entries(map).forEach(([key, value]) => {
    dictCache.set(key, value ?? []);
  });
}

/** 清理前端字典内存缓存。 */
export function clearDictCache(typeCode?: string) {
  if (!typeCode) {
    dictCache.clear();
    return;
  }
  dictCache.delete(typeCode);
}
