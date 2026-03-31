<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { BasicOption, Recordable } from '@vben/types';

import type { AuthApi } from '#/api/core/auth';

import { computed, markRaw, useTemplateRef } from 'vue';

import {
  AuthenticationLogin,
  ImageCaptcha,
  z,
} from '@vben/common-ui';
import { $t } from '@vben/locales';

import { getCaptchaApi } from '#/api/core/auth';
import { useAuthStore } from '#/store';
defineOptions({ name: 'Login' });
const authStore = useAuthStore();
const MOCK_USER_OPTIONS: BasicOption[] = [
  {
    label: 'Super',
    value: 'vben',
  },
  {
    label: 'Admin',
    value: 'admin',
  },
  {
    label: 'User',
    value: 'jack',
  },
];

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenSelect',
      // componentProps(_values, form) {
      //   return {
      //     'onUpdate:modelValue': (value: string) => {
      //       const findItem = MOCK_USER_OPTIONS.find(
      //         (item) => item.value === value,
      //       );
      //       if (findItem) {
      //         form.setValues({
      //           password: '123456',
      //           username: findItem.label,
      //         });
      //       }
      //     },
      //     options: MOCK_USER_OPTIONS,
      //     placeholder: $t('authentication.selectAccount'),
      //   };
      // },
      componentProps: {
        options: MOCK_USER_OPTIONS,
        placeholder: $t('authentication.selectAccount'),
      },
      fieldName: 'selectAccount',
      label: $t('authentication.selectAccount'),
      rules: z
        .string()
        .min(1, { message: $t('authentication.selectAccount') })
        .optional()
        .default('vben'),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.usernameTip'),
      },
      dependencies: {
        trigger(values, form) {
          if (values.selectAccount) {
            const findUser = MOCK_USER_OPTIONS.find(
              (item) => item.value === values.selectAccount,
            );
            if (findUser) {
              form.setValues({
                password: '123456',
                username: findUser.value,
              });
            }
          }
        },
        triggerFields: ['selectAccount'],
      },
      fieldName: 'username',
      label: $t('authentication.username'),
      rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.password'),
      },
      fieldName: 'password',
      label: $t('authentication.password'),
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
    // {
    //   component: markRaw(SliderCaptcha),
    //   fieldName: 'captcha',
    //   rules: z.boolean().refine((value) => value, {
    //     message: $t('authentication.verifyRequiredTip'),
    //   }),
    // },
    {
      component: markRaw(ImageCaptcha),
      fieldName: 'captcha',
      componentProps: {
        requestCaptcha: getCaptchaApi,
      },
      formFieldProps: {
        validateOnBlur: true,
        validateOnChange: false,
        validateOnInput: false,
        validateOnModelUpdate: false,
      },
      rules: z.object({
        captchaCode: z.string().trim().min(1, {
          message: $t('authentication.captchaPlaceholder'),
        }),
        captchaKey: z.string().min(1, {
          message:  $t('authentication.refresh'),
        }),
      }),
    },
  ];
});

const loginRef =
  useTemplateRef<InstanceType<typeof AuthenticationLogin>>('loginRef');

/**
 * @zh_CN 登录请求参数
 */
interface LoginRequest {
  /**
   * @zh_CN 验证码信息
   */
  captcha: Captcha;
  /**
   * @zh_CN 密码
   */
  password: string;
  /**
   * @zh_CN 用户名
   */
  username: string;
}
interface Captcha {
  /**
   * @zh_CN 验证码 code
   */
  captchaCode: string;
  /**
   * @zh_CN 验证码 key
   */
  captchaKey: string;
}
async function onSubmit(params: Recordable<any>) {
  const data = params as LoginRequest;
  const res: AuthApi.LoginParams = {
    username: data.username,
    password: data.password,
    captchaCode: data.captcha?.captchaCode,
    captchaKey: data.captcha?.captchaKey,
  };
  authStore.authLogin(res).catch(() => {
    // 登陆失败，刷新验证码的演示
    const formApi = loginRef.value?.getFormApi();
    // 重置验证码组件的值
    formApi?.setFieldValue(
      'captcha', 
      {
        captchaCode: '',
        captchaKey: '',
      }, 
      false);
    // 使用表单API获取验证码组件实例，并调用其resume方法来重置验证码
    formApi
      ?.getFieldComponentRef<InstanceType<typeof ImageCaptcha>>('captcha')
      ?.refresh();
  });
}
</script>

<template>
  <AuthenticationLogin
    ref="loginRef"
    :form-schema="formSchema"
    :loading="authStore.loginLoading"
    @submit="onSubmit"
  />
</template>
