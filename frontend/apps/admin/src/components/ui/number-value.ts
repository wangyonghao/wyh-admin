/**
 * 数字框的空值仍是 null。0 是合法数字，不能当成清空。
 * 表单重置写成 null 时，控件显示为空。
 */

export function displayFormNumber(value: unknown): number | undefined {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : undefined;
  }
  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : undefined;
  }
  return undefined;
}

export function commitFormNumber(value: unknown): null | number {
  const shown = displayFormNumber(value);
  return shown == null ? null : shown;
}

/** 没传 step 时，用 precision 推出步长。两者都没有就交给控件默认。 */
export function numberStep(
  step: unknown,
  precision: unknown,
): number | undefined {
  if (typeof step === 'number' && Number.isFinite(step)) return step;
  if (
    typeof precision === 'number' &&
    Number.isInteger(precision) &&
    precision >= 0 &&
    precision <= 20
  ) {
    return 10 ** -precision;
  }
  return undefined;
}

export function numberBound(value: unknown): number | undefined {
  if (typeof value === 'number' && Number.isFinite(value)) return value;
  return undefined;
}
