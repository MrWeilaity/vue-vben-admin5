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

  /**
   * 从后端读取指定字典类型的启用项。
   * _force 参数保留给调用方兼容旧缓存语义；当前实现不做前端缓存，每次调用都会请求后端。
   */
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

  /**
   * 根据字典值获取展示标签。
   * 使用字符串比较，兼容后端返回字符串值、业务侧传数字值的场景。
   */
  function getLabel(value: number | string | undefined) {
    const target = options.value.find((item) => `${item.value}` === `${value}`);
    return target?.label ?? value;
  }

  /** 适配 Select 组件的 options 结构。 */
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

/**
 * 批量预取字典。
 * 当前只触发后端读取，不写入前端本地缓存；主要用于提前刷新后端/Redis 侧数据读取路径。
 */
export async function preloadDict(typeCodes: string[]) {
  if (!typeCodes?.length) {
    return;
  }
  await getDictByTypeCodes(typeCodes, true);
}
