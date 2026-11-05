import type { ID } from '#/types/api';

import http, { download } from '#/utils/request';

/** 文件业务 ID */
export type FileId = ID;

export function toFileId(fileId?: unknown): FileId | undefined {
  if (fileId == null || fileId === '') {
    return undefined;
  }
  return String(fileId);
}

/** 文件预览地址 */
export function resolveFilePreviewUrl(fileId?: FileId | null): string | undefined {
  const id = toFileId(fileId);
  if (id == null) {
    return undefined;
  }
  return `/api/files/${id}/preview`;
}

export function resolveFileDownloadUrl(fileId: FileId): string {
  return `/api/files/${fileId}/download`;
}

export const fileApi = {
  page: async (params: FileQuery) => {
    const result = await http.get<{
      records: FileResult[];
      total: number;
      page: number;
      pageSize: number;
      pages: number;
    }>('/api/files', { params });
    return {
      ...result,
      records: (result.records ?? []).map((item) => ({ ...item, id: String(item.id) })),
    };
  },

  detail: async (id: FileId) => {
    const result = await http.get<FileResult>(`/api/files/${id}`);
    return { ...result, id: String(result.id) };
  },

  upload: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const result = await http.post<FileUploadResult>('/api/files', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return { ...result, fileId: String(result.fileId) };
  },

  download: (fileId: FileId, fileName: string) => {
    return download(`/api/files/${fileId}/download`, {}, fileName);
  },

  preview: (fileId: FileId) => resolveFilePreviewUrl(fileId)!,

  delete: (id: FileId) => {
    return http.delete(`/api/files/${id}`);
  },

  batchDelete: (ids: FileId[]) => {
    return http.delete('/api/files', { data: { ids: ids.map(String) } });
  },
};

/** 文件分类（与后端 FileCategory 对齐） */
export type FileCategory =
  | 'ALL'
  | 'ARCHIVE'
  | 'DOCUMENT'
  | 'DOCUMENT_TEXT'
  | 'DOCUMENT_SPREADSHEET'
  | 'DOCUMENT_PRESENTATION'
  | 'DOCUMENT_PDF'
  | 'IMAGE'
  | 'MEDIA';

export interface FileQuery {
  fileName?: string;
  category?: FileCategory;
  /** createTime 排序：asc / desc */
  sortOrder?: 'asc' | 'desc';
  page?: number;
  pageSize?: number;
}

export interface FileResult {
  id: FileId;
  fileName: string;
  contentType?: string;
  fileSize: number;
  sha256?: string;
  storageType?: string | { description?: string; value?: number };
  status?: string;
  createTime?: string;
  updateTime?: string;
}

export interface FileUploadResult {
  fileId: FileId;
  fileName: string;
  fileSize: number;
  contentType?: string;
}
