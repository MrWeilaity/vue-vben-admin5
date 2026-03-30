import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    /**
     * @zh_CN 验证码 code
     */
    captchaCode: string;
    /**
     * @zh_CN 验证码 key
     */
    captchaKey: string;
    /**
     * @zh_CN 密码
     */
    password: string;
    /**
     * @zh_CN 用户名
     */
    username: string;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    accessToken: string;
    refreshToken: string;
  }

  export interface RefreshTokenParams {
    refreshToken?: string;
  }

  export interface RefreshTokenResult {
    accessToken: string;
    refreshToken: string;
  }
  /**
   * 验证码接口返回值
   */
  export interface CaptchaResponse {
    /** 验证码 key */
    captchaImage: string;
    /** 验证码图片 Base64 数据 */
    captchaKey: string;
    /** 过期时间（秒） */
    expireSeconds: number;
  }
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>('/auth/login', data, {
    withCredentials: true,
  });
}

/**
 * 刷新accessToken
 */
export async function refreshTokenApi(data?: AuthApi.RefreshTokenParams) {
  return baseRequestClient.post<AuthApi.RefreshTokenResult>(
    '/auth/refresh',
    data ?? null,
    {
      withCredentials: true,
    },
  );
}

/**
 * 退出登录
 */
export async function logoutApi(data?: AuthApi.RefreshTokenParams) {
  return baseRequestClient.post('/auth/logout', data ?? null, {
    withCredentials: true,
  });
}

/**
 * 获取用户权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/auth/codes');
}

/**
 * 获取图片验证码
 */
export function getCaptchaApi() {
  return requestClient.get<AuthApi.CaptchaResponse>('/auth/captcha');
}
