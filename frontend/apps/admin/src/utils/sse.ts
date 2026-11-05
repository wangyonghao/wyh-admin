import { preferences } from '@vben/preferences';

import { toast } from '#/ui-patterns/toast';
import { useUserStore } from '#/store/user';

export interface SSEOptions {
  params?: Record<string, any>;
  onOpen?: () => void;
  onMessage?: (data: any, event: MessageEvent) => void;
  onError?: (error: Event) => void;
}

export interface SSEConnection {
  close: () => void;
  readyState: number;
}

function showError(msg: string) {
  console.error(`[HTTP Error]: ${msg}`);
  toast.error(msg, { duration: 5000 });
}

/** 打开一条 SSE 连接。Token 与语言通过查询参数传递（EventSource 不能设置请求头）。 */
export function sse(url: string, options: SSEOptions = {}): SSEConnection {
  const userStore = useUserStore();
  const token = userStore.token ?? '';
  const locale = preferences.app.locale;
  const separator = url.includes('?') ? '&' : '?';
  const params = new URLSearchParams(options.params || {});
  if (token) params.append('Authorization', `Bearer ${token}`);
  if (locale) params.append('Accept-Language', locale);

  const sseUrl = `${url}${separator}${params.toString()}`;
  const eventSource = new EventSource(sseUrl, { withCredentials: true });

  eventSource.onopen = () => options.onOpen?.();
  eventSource.onmessage = (event) => {
    try {
      options.onMessage?.(JSON.parse(event.data), event);
    } catch {
      options.onMessage?.(event.data, event);
    }
  };
  eventSource.onerror = (err) => {
    showError('SSE 连接异常');
    options.onError?.(err);
    eventSource.close();
  };
  return { close: () => eventSource.close(), readyState: eventSource.readyState };
}
