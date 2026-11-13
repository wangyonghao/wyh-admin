/**
 * 表单 Divider 没有字段值，画成分割线。
 * vertical 对应竖线；dashed 用虚线边框盖住套件分割线的实色。
 */

export interface FormDividerProps {
  dashed?: boolean;
  title?: string;
  vertical?: boolean;
}

export function dividerOrientation(
  vertical: unknown,
): 'horizontal' | 'vertical' {
  return vertical === true ? 'vertical' : 'horizontal';
}

export function dividerClass(
  vertical: unknown,
  dashed: unknown,
): string | undefined {
  if (dashed !== true) return undefined;
  if (vertical === true) {
    return 'h-full w-0 border-l border-dashed border-border bg-transparent';
  }
  return 'h-0 w-full border-t border-dashed border-border bg-transparent';
}
