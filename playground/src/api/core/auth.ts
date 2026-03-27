import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    accessToken: string;
    expiresIn: number;
    refreshToken: string;
  }

  export type RefreshTokenResult = LoginResult;
}

let cachedRefreshToken = '';

export function getCachedRefreshToken() {
  return cachedRefreshToken;
}

export function setCachedRefreshToken(refreshToken: string) {
  cachedRefreshToken = refreshToken;
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  const result = await requestClient.post<AuthApi.LoginResult>(
    '/auth/login',
    data,
    {
      withCredentials: true,
    },
  );
  if (result?.refreshToken) {
    setCachedRefreshToken(result.refreshToken);
  }
  return result;
}

/**
 * 刷新accessToken
 */
export async function refreshTokenApi() {
  const response = await baseRequestClient.post<{
    code: number;
    data: AuthApi.RefreshTokenResult;
    message: string;
  }>(
    '/auth/refresh',
    { refreshToken: getCachedRefreshToken() },
    {
      withCredentials: true,
    },
  );
  const refreshToken = response?.data?.data?.refreshToken;
  if (refreshToken) {
    setCachedRefreshToken(refreshToken);
  }
  return response;
}

/**
 * 退出登录
 */
export async function logoutApi() {
  return baseRequestClient.post(
    '/auth/logout',
    { refreshToken: getCachedRefreshToken() },
    {
      withCredentials: true,
    },
  );
}

export function clearCachedRefreshToken() {
  cachedRefreshToken = '';
}

/**
 * 获取用户权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/auth/codes');
}
