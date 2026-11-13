/**
 * useVbenForm 的 Upload 绑 fileList，空值仍是 null（见 adapter/form.ts）。
 * null 显示为空列表。删到没有文件、或一次都没选，回写 null，不回写 []。
 * 页面上带自定义请求的上传按钮仍用 Naive，这里只覆盖表单 schema。
 */

export interface FormUploadFile {
  file?: globalThis.File;
  id: string;
  name: string;
  status?: string;
  url?: string;
}

export interface UploadPick {
  file?: globalThis.File;
  name: string;
}

export function displayUploadFiles(value: unknown): FormUploadFile[] {
  if (!Array.isArray(value)) return [];
  return value.flatMap((item) => {
    const file = normalizeUploadFile(item);
    return file ? [file] : [];
  });
}

export function commitUploadFiles(
  files: FormUploadFile[],
): FormUploadFile[] | null {
  return files.length === 0 ? null : files;
}

export function nextUploadList(
  current: unknown,
  incoming: UploadPick[],
  options: { max?: number; multiple?: boolean } = {},
): FormUploadFile[] | null {
  const picked = options.multiple === false ? incoming.slice(0, 1) : incoming;
  const base = options.multiple === false ? [] : displayUploadFiles(current);
  const next = [...base];
  const max =
    typeof options.max === 'number' && options.max > 0
      ? options.max
      : undefined;
  picked.forEach((item, index) => {
    if (max != null && next.length >= max) return;
    const name = item.name.trim();
    if (!name) return;
    next.push({
      file: item.file,
      id: `${Date.now()}-${index}-${name}`,
      name,
      status: 'finished',
    });
  });
  return commitUploadFiles(next);
}

export function removeUploadFile(
  current: unknown,
  id: string,
): FormUploadFile[] | null {
  return commitUploadFiles(
    displayUploadFiles(current).filter((file) => file.id !== id),
  );
}

function normalizeUploadFile(value: unknown): FormUploadFile | null {
  if (value == null || typeof value !== 'object') return null;
  const row = value as Record<string, unknown>;
  if (typeof row.name !== 'string' || row.name.trim() === '') return null;
  const id =
    typeof row.id === 'string' || typeof row.id === 'number'
      ? String(row.id)
      : row.name;
  const file: FormUploadFile = { id, name: row.name };
  if (typeof row.status === 'string') file.status = row.status;
  if (typeof row.url === 'string') file.url = row.url;
  if (
    typeof globalThis.File !== 'undefined' &&
    row.file instanceof globalThis.File
  ) {
    file.file = row.file;
  }
  return file;
}
