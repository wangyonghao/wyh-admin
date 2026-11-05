import type { PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

export interface JobLogResp {
  id: string;
  jobId: string;
  jobName: string;
  handlerCode: string;
  triggerType: string;
  startTime: string;
  endTime?: string;
  durationMs?: number;
  status: number;
  errorMessage?: string;
}

export interface JobLogQuery {
  jobId?: string;
  handlerCode?: string;
  status?: number;
}

export interface JobLogPageQuery extends JobLogQuery, PageQuery {}

export function listJobLog(query: JobLogPageQuery) {
  return http.get<PageResult<JobLogResp[]>>('/schedule/log', { params: query });
}
