<script setup lang="ts">
/**
 * ============================
 * 图片验证码组件（ImageCaptcha）
 * ============================
 *
 * 功能：
 * 1. 自动加载验证码图片
 * 2. 点击图片刷新验证码
 * 3. 输入验证码
 * 4. v-model 绑定验证码 + key
 *
 * 使用场景：
 * 替代 SliderCaptcha（滑块验证）
 */

import { computed, onMounted, reactive, watch } from 'vue';

import { $t } from '@vben/locales';

import { cn } from '@vben-core/shared/utils';

/**
 * ============================
 * v-model 绑定的数据结构
 * ============================
 *
 * 表单最终会拿到：
 * {
 *   captcha: {
 *     captchaCode: '1234',
 *     captchaKey: 'uuid'
 *   }
 * }
 */
export interface ImageCaptchaValue {
  captchaCode: string; // 用户输入的验证码
  captchaKey: string; // 后端返回的唯一标识（用于校验）
}

/**
 * ============================
 * 后端接口返回的数据结构
 * ============================
 */
export interface ImageCaptchaResponse {
  captchaImageBase64: string; // base64 图片
  captchaKey: string; // 唯一 key
  expireSeconds?: number; // 过期时间（可选）
}

/**
 * ============================
 * 组件 Props
 * ============================
 */
interface ImageCaptchaProps {
  requestCaptcha: () => Promise<ImageCaptchaResponse>; // 获取验证码接口

  // UI相关
  class?: string;
  wrapperClass?: string;
  inputClass?: string;
  imageClass?: string;

  placeholder?: string;
  disabled?: boolean;
}

/**
 * 默认值
 */
const props = withDefaults(defineProps<ImageCaptchaProps>(), {
  class: '',
  placeholder: '',
  disabled: false,
  inputClass: '',
  imageClass: '',
  wrapperClass: '',
});

/**
 * ============================
 * 组件事件
 * ============================
 */
const emit = defineEmits<{
  change: [ImageCaptchaValue]; // 输入变化
  loadError: [unknown]; // 加载失败
  loadSuccess: [ImageCaptchaResponse]; // 加载成功
}>();

/**
 * ============================
 * v-model（核心）
 * ============================
 *
 * 双向绑定：
 * - captchaCode（用户输入）
 * - captchaKey（后端返回）
 */
const modelValue = defineModel<ImageCaptchaValue>({
  default: () => ({
    captchaCode: '',
    captchaKey: '',
  }),
});

/**
 * ============================
 * 内部状态
 * ============================
 */
const state = reactive({
  loading: false, // 是否正在加载验证码
  captchaImageBase64: '', // 图片 base64
  expireSeconds: 0, // 过期时间
});

/**
 * ============================
 * 计算图片 src
 * ============================
 *
 * 兼容：
 * - 后端返回完整 data:image
 * - 或只返回 base64
 */
const imageSrc = computed(() => {
  const value = state.captchaImageBase64?.trim();
  if (!value) return '';

  return value.startsWith('data:image')
    ? value
    : `data:image/png;base64,${value}`;
});

/**
 * ============================
 * 加载验证码（核心方法）
 * ============================
 */
async function loadCaptcha() {
  // 禁用 or 正在加载时，不重复请求
  if (props.disabled || state.loading) return;

  state.loading = true;

  try {
    // 调用外部接口
    const res = await props.requestCaptcha();

    // 设置图片
    state.captchaImageBase64 = res.captchaImageBase64;
    state.expireSeconds = res.expireSeconds ?? 0;

    /**
     * 重置验证码输入 + 更新 key
     *
     * 注意：
     * 每次刷新验证码，key 必须更新
     */
    modelValue.value = {
      captchaCode: '',
      captchaKey: res.captchaKey,
    };

    emit('loadSuccess', res);
    emit('change', modelValue.value);
  } catch (error) {
    /**
     * 加载失败处理
     */
    state.captchaImageBase64 = '';
    modelValue.value = {
      captchaCode: '',
      captchaKey: '',
    };

    emit('loadError', error);
  } finally {
    state.loading = false;
  }
}

/**
 * ============================
 * 输入处理
 * ============================
 */
function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;

  modelValue.value = {
    captchaCode: target.value,
    captchaKey: modelValue.value?.captchaKey ?? '',
  };

  emit('change', modelValue.value);
}

/**
 * ============================
 * 对外暴露方法
 * ============================
 *
 * 可以通过 ref 调用：
 * captchaRef.refresh()
 */
defineExpose({
  refresh: loadCaptcha,
});

/**
 * ============================
 * 监听 disabled
 * ============================
 *
 * 如果从 disabled → 启用
 * 自动重新加载验证码
 */
watch(
  () => props.disabled,
  (disabled) => {
    if (!disabled && !state.captchaImageBase64) {
      loadCaptcha();
    }
  },
);

/**
 * ============================
 * 组件挂载自动加载
 * ============================
 */
onMounted(() => {
  loadCaptcha();
});
</script>

<template>
  <!-- 外层容器 -->
  <div :class="cn('flex w-full items-center gap-2', wrapperClass, props.class)">
    <!-- 输入框 -->
    <input
      :value="modelValue?.captchaCode ?? ''"
      :disabled="disabled"
      :placeholder="
        placeholder || $t('authentication.captchaPlaceholder') || '请输入验证码'
      "
      :class="
        cn(
          'h-10 min-w-0 flex-1 rounded-md border border-border bg-background px-3 text-sm outline-none transition focus:ring-2 focus:ring-primary/20 disabled:cursor-not-allowed disabled:opacity-60',
          inputClass,
        )
      "
      autocomplete="off"
      @input="handleInput"
    />

    <!-- 验证码图片区域 -->
    <button
      type="button"
      :disabled="disabled || state.loading"
      :class="
        cn(
          'flex h-10 w-[120px] shrink-0 items-center justify-center overflow-hidden rounded-md border border-border bg-muted transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60',
          imageClass,
        )
      "
      @click="loadCaptcha"
    >
      <!-- 加载中 -->
      <span v-if="state.loading" class="text-xs text-muted-foreground">
        {{ $t('common.loading') || '加载中...' }}
      </span>

      <!-- 验证码图片 -->
      <img
        v-else-if="imageSrc"
        :src="imageSrc"
        alt="captcha"
        class="h-full w-full object-cover"
      />

      <!-- 空状态 -->
      <span v-else class="text-xs text-muted-foreground">
        {{ $t('authentication.refreshCaptcha') || '点击刷新' }}
      </span>
    </button>
  </div>
</template>
