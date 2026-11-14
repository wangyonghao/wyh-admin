/**
 * 从地址栏片段取出一次性注册 token，并标明调用方应清掉片段。
 * 片段不会发给服务器，也不会进入后续请求的 Referer。
 */
export function readRegistrationToken(hash: string): { token: string; shouldClear: boolean } {
  const raw = hash.startsWith('#') ? hash.slice(1) : hash;
  const token = new URLSearchParams(raw).get('t')?.trim() ?? '';
  return { token, shouldClear: token.length > 0 };
}
