import type { BadgeVariants } from './badge';

export type BadgeVariant = NonNullable<BadgeVariants['variant']>;

/**
 * Naive `NTag` 的 type / 字典 `extra.color` 里能对上徽标的名字。
 * `primary` 和 `info` 没有单独的徽标色，用 default（强调色）。
 * 对不上的名字返回 null，调用方不要猜一个颜色。
 */
const TAG_VARIANT: Record<string, BadgeVariant> = {
  danger: 'destructive',
  default: 'secondary',
  error: 'destructive',
  info: 'default',
  primary: 'default',
  success: 'success',
  warning: 'warning',
};

export function badgeVariantForTag(type: unknown): BadgeVariant | null {
  if (typeof type !== 'string' || type === '') return null;
  return TAG_VARIANT[type] ?? null;
}

function colorFromExtra(extra: unknown): string | undefined {
  if (typeof extra === 'string' && extra !== '') {
    try {
      return colorFromExtra(JSON.parse(extra));
    } catch {
      return undefined;
    }
  }
  if (!extra || typeof extra !== 'object') return undefined;
  const color = (extra as { color?: unknown }).color;
  return typeof color === 'string' ? color : undefined;
}

/** 字典项上的颜色：先看 tagType，再看 extra.color。 */
export function dictTagColor(item: unknown): string | undefined {
  if (!item || typeof item !== 'object') return undefined;
  const record = item as { extra?: unknown; tagType?: unknown };
  if (typeof record.tagType === 'string' && record.tagType !== '') {
    return record.tagType;
  }
  return colorFromExtra(record.extra);
}

export function badgeVariantForDictItem(item: unknown): BadgeVariant {
  return badgeVariantForTag(dictTagColor(item)) ?? 'secondary';
}
