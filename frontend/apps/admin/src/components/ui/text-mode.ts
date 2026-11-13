export interface TextFieldAttrs {
  autosize?: unknown;
  maxLength?: unknown;
  maxlength?: unknown;
  pair?: unknown;
  rows?: unknown;
  showCount?: unknown;
  showPasswordOn?: unknown;
  showWordLimit?: unknown;
  textarea?: unknown;
  type?: unknown;
}

/** 成对输入由 FormTextInput 画成两个原子 Input，不走文本域或密码。 */
export function textUsesPair(attrs: TextFieldAttrs): boolean {
  return attrs.pair != null && attrs.pair !== false;
}

export function textUsesTextarea(attrs: TextFieldAttrs): boolean {
  return (
    attrs.textarea === true ||
    attrs.type === 'textarea' ||
    attrs.autosize != null
  );
}

export function textUsesPassword(attrs: TextFieldAttrs): boolean {
  return attrs.type === 'password' || attrs.showPasswordOn != null;
}

export function textShowsCount(attrs: TextFieldAttrs): boolean {
  return attrs.showCount != null || attrs.showWordLimit != null;
}

export function textMaxLength(attrs: TextFieldAttrs): number | undefined {
  const raw = attrs.maxlength ?? attrs.maxLength;
  if (typeof raw === 'number' && Number.isFinite(raw)) return raw;
  if (typeof raw === 'string' && raw.trim() !== '') {
    const parsed = Number(raw);
    if (Number.isFinite(parsed)) return parsed;
  }
  return undefined;
}

export function textRows(attrs: TextFieldAttrs): number | undefined {
  if (typeof attrs.rows === 'number' && attrs.rows > 0) return attrs.rows;
  if (attrs.autosize && typeof attrs.autosize === 'object') {
    const minRows = (attrs.autosize as { minRows?: unknown }).minRows;
    if (typeof minRows === 'number' && minRows > 0) return minRows;
  }
  return undefined;
}

export function textCountLabel(value: string, max?: number): string {
  return max == null ? String(value.length) : `${value.length}/${max}`;
}
