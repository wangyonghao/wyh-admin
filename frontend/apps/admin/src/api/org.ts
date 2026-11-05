import http from '#/utils/request';

export interface OrgResult {
  id: string | number;
  name: string;
  code?: string;
  primary?: boolean;
}

export interface OrgInviteResult {
  id: string | number;
  tenantId: string | number;
  orgName?: string;
  orgCode?: string;
  email: string;
  status: number;
  expireTime?: string;
  token?: string;
}

export interface OrgSessionResult {
  current?: OrgResult | null;
  organizations: OrgResult[];
  needsOrganization: boolean;
  pendingInvites: OrgInviteResult[];
}

export const orgApi = {
  session() {
    return http.get<OrgSessionResult>('/org/session');
  },
  create(data: { name: string; description?: string }) {
    return http.post<OrgResult>('/org', data);
  },
  switchOrg(orgId: string | number) {
    return http.post('/org/switch', { orgId });
  },
  apply(data: { code: string; message?: string }) {
    return http.post('/org/applications', data);
  },
  invite(email: string) {
    return http.post<OrgInviteResult>('/org/invites', { email });
  },
  previewInvite(token: string) {
    return http.get<OrgInviteResult>('/org/invites/preview', { params: { token } });
  },
  acceptInvite(token: string) {
    return http.post('/org/invites/accept', null, { params: { token } });
  },
  rejectInvite(token: string) {
    return http.post('/org/invites/reject', null, { params: { token } });
  },
  reviewApplication(id: string | number, approve: boolean) {
    return http.post(`/org/applications/${id}/review`, { approve });
  },
};
