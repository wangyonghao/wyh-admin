import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore } from '@vben/stores';
import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';

import { $t } from '#/locales';
import { useUserStore } from '#/store/user';
import { toast } from '#/ui-patterns/toast';
import { BusinessError } from '#/utils/business-error';

/** 与后端 `Result` 成功码一致。 */
const SUCCESS_CODE = '200';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000,
});

export function resolveHttpErrorMessage(error: AxiosError<{ message?: string }>): string {
  if (!error.response) {
    if (error.code === 'ECONNABORTED') return '请求超时，请稍后重试';
    if (!navigator.onLine) return '网络已断开，请检查网络';
    return '网络异常，请稍后重试';
  }

  const status = error.response.status;
  const map: Record<number, string> = {
    400: $t('ui.fallback.http.badRequest'),
    401: $t('ui.fallback.http.unauthorized'),
    403: $t('ui.fallback.http.forbidden'),
    404: $t('ui.fallback.http.notFound'),
    408: $t('ui.fallback.http.requestTimeout'),
  };
  if (map[status]) return map[status];
  if (status >= 500) return $t('ui.fallback.http.internalServerError');
  return error.response.data?.message || `请求失败（${status}）`;
}

let redirectingToLogin = false;

/** 登录态失效：清空本地会话并回到登录页。不请求退出接口，避免 401 重入。 */
export async function handleUnauthorized() {
  if (redirectingToLogin) return;
  redirectingToLogin = true;
  try {
    const { default: router } = await import('#/router');
    const current = router.currentRoute.value;
    if (current.path === LOGIN_PATH) return;

    const redirect = encodeURIComponent(current.fullPath);
    resetAllStores();
    useAccessStore().setLoginExpired(true);
    await router.replace({
      path: LOGIN_PATH,
      query: { redirect },
    });
  } finally {
    redirectingToLogin = false;
  }
}

// ---------- 请求拦截 ----------
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    const token = userStore.token ?? '';
    const locale = preferences.app.locale;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    if (locale) config.headers['Accept-Language'] = locale;
    return config;
  },
  (error: unknown) => Promise.reject(error),
);

// ---------- 响应拦截 ----------
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const body = response.data;
    // 非 JSON（文件下载等）直接放行
    if (response.config.responseType === 'blob' || body?.code === undefined) return body;

    return body;
  },
  (error: AxiosError<{ message?: string; code?: string | number }>) => {
    if (axios.isCancel(error)) return Promise.reject(error);
    // 登录状态失效：清空本地会话并回到登录页
    if (error.response?.status === 401) {
      void handleUnauthorized();
    }

    // 业务异常 转换为 Business Error
    if (error.response?.status === 422) {
      return Promise.reject(new BusinessError(error.response?.data?.code, error.response?.data?.message, error.response?.data?.data));
    }

    const backendCode = error.response?.data?.code;
    if (backendCode != null) {
      error.code = String(backendCode);
    }
    // 非业务异常：提示用户
    toast.error(resolveHttpErrorMessage(error), { duration: 5000 });
    return Promise.reject(error);
  },
);

function showError(msg: string) {
  toast.error(msg, { duration: 5000 });
}

/** 触发浏览器保存 */
function saveBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000); // 延迟释放，避免部分浏览器下载未开始就失效
}

/** 下载文件。空内容视为失败，不触发浏览器保存。 */
export async function download(
  url: string,
  config: AxiosRequestConfig = {},
  fallbackName = 'download',
): Promise<void> {
  const response = await http.get(url, { responseType: 'blob', ...config });
  
  const blob = response.data;

  if (blob.type.includes('application/json')) {
    try {
      const errData = JSON.parse(await blob.text());
      showError(errData.msg || errData.message || '文件生成失败');
    } catch {
      showError('下载失败：文件数据为空');
    }
    throw new Error('Empty blob');
  }

  let fileName = fallbackName;
  const disposition = response.headers['content-disposition'];
  if (disposition) {
    const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (match?.[1]) fileName = decodeURIComponent(match[1].replace(/['"]/g, ''));
  }

  saveBlob(blob, fileName);
}

export default http;
