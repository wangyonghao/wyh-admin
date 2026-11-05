import type { PageQuery, PageResult } from '#/types/api';

import http from '#/utils/request';

export type ScheduleMode = 'CRON' | 'DAILY' | 'INTERVAL' | 'MONTHLY' | 'WEEKLY';

export interface ScheduleSpec {
  mode: ScheduleMode;
  hour?: number;
  minute?: number;
  daysOfWeek?: number[];
  dayOfMonth?: number;
  interval?: number;
  intervalUnit?: 'HOUR' | 'MINUTE';
  cron?: string;
}

export interface JobHandlerOption {
  code: string;
  name: string;
  description: string;
  allowConcurrent: boolean;
}

export interface JobResp {
  id: string;
  name: string;
  handlerCode: string;
  handlerName: string;
  handlerDescription?: string;
  cron: string;
  scheduleMode: ScheduleMode;
  schedule: ScheduleSpec;
  scheduleLabel: string;
  params?: string;
  status: number;
  remark?: string;
  nextFireTime?: string;
  createTime?: string;
  updateTime?: string;
  upcomingTimes?: string[];
}

export interface JobSavePayload {
  name: string;
  handlerCode: string;
  schedule: ScheduleSpec;
  params?: string;
  remark?: string;
  enabled?: boolean;
}

export interface JobQuery {
  name?: string;
  handlerCode?: string;
  status?: number;
}

export interface JobPageQuery extends JobQuery, PageQuery {}

export function listJobHandlers() {
  return http.get<JobHandlerOption[]>('/schedule/job/handlers');
}

export function listJob(query: JobPageQuery) {
  return http.get<PageResult<JobResp[]>>('/schedule/job', { params: query });
}

export function getJob(id: string) {
  return http.get<JobResp>(`/schedule/job/${id}`);
}

export function addJob(data: JobSavePayload) {
  return http.post('/schedule/job', data);
}

export function updateJob(data: JobSavePayload, id: string) {
  return http.put(`/schedule/job/${id}`, data);
}

export function updateJobStatus(status: number, id: string) {
  return http.patch(`/schedule/job/${id}/status`, { status });
}

export function deleteJob(id: string) {
  return http.delete(`/schedule/job/${id}`);
}

export function triggerJob(id: string) {
  return http.post(`/schedule/job/trigger/${id}`);
}
