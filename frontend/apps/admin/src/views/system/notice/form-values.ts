import type { NoticeCreateReq } from '#/api/system/notice';

import { asSelectList } from '#/components/ui/select-value';

export interface NoticeFormState {
  content?: string;
  isTiming?: string;
  isTop?: string;
  noticeMethods?: unknown;
  noticeScope?: string;
  noticeUsers?: unknown;
  publishTime?: unknown;
  title?: string;
  type?: string;
}

/** 和原先手写表单一样，用 UTC 的 `yyyy-MM-dd HH:mm:ss`。没选完或不是定时则不提交。 */
export function formatNoticePublishTime(
  isTiming: string | undefined,
  publishTime: unknown,
): string | undefined {
  if (isTiming !== 'true' || typeof publishTime !== 'number') return undefined;
  if (Number.isNaN(publishTime)) return undefined;
  return new Date(publishTime).toISOString().slice(0, 19).replace('T', ' ');
}

export function buildNoticePayload(form: NoticeFormState): NoticeCreateReq {
  const scope = form.noticeScope ?? '1';
  const users = asSelectList(form.noticeUsers).map(String);
  return {
    content: form.content ?? '',
    isTiming: form.isTiming ?? 'false',
    isTop: form.isTop ?? 'false',
    noticeMethods: asSelectList(form.noticeMethods).map(String).join(','),
    noticeScope: scope,
    noticeUsers: scope === '1' || users.length === 0 ? undefined : users.join(','),
    publishTime: formatNoticePublishTime(form.isTiming, form.publishTime),
    title: form.title ?? '',
    type: form.type ?? '',
  };
}
