/**
 * 表单 Space 没有字段值。间距用 flex，不包一层 Naive。
 * small / medium / large 对应 8 / 12 / 16。数组第一项是横向间距。
 */

export type SpaceAlign = 'baseline' | 'center' | 'end' | 'start' | 'stretch';
export type SpaceJustify =
  | 'center'
  | 'end'
  | 'space-around'
  | 'space-between'
  | 'space-evenly'
  | 'start';
export type SpaceSize =
  | 'large'
  | 'medium'
  | 'small'
  | number
  | [number, number];

export interface FormSpaceProps {
  align?: SpaceAlign;
  inline?: boolean;
  justify?: SpaceJustify;
  size?: SpaceSize;
  vertical?: boolean;
  wrap?: boolean;
}

const NAMED_GAP: Record<'large' | 'medium' | 'small', number> = {
  small: 8,
  medium: 12,
  large: 16,
};

const ALIGN: Record<SpaceAlign, string> = {
  baseline: 'items-baseline',
  center: 'items-center',
  end: 'items-end',
  start: 'items-start',
  stretch: 'items-stretch',
};

const JUSTIFY: Record<SpaceJustify, string> = {
  center: 'justify-center',
  end: 'justify-end',
  'space-around': 'justify-around',
  'space-between': 'justify-between',
  'space-evenly': 'justify-evenly',
  start: 'justify-start',
};

export function spaceClass(props: FormSpaceProps = {}): string {
  const classes = [
    props.inline ? 'inline-flex' : 'flex',
    props.vertical ? 'flex-col' : 'flex-row',
    props.wrap === false ? 'flex-nowrap' : 'flex-wrap',
  ];
  if (props.align && ALIGN[props.align]) classes.push(ALIGN[props.align]);
  if (props.justify && JUSTIFY[props.justify])
    classes.push(JUSTIFY[props.justify]);
  return classes.join(' ');
}

export function spaceStyle(size: unknown): {
  columnGap: string;
  rowGap: string;
} {
  const gap = resolveGap(size);
  return {
    columnGap: `${gap.horizontal}px`,
    rowGap: `${gap.vertical}px`,
  };
}

function resolveGap(size: unknown): { horizontal: number; vertical: number } {
  if (Array.isArray(size)) {
    return {
      horizontal: positive(size[0], NAMED_GAP.medium),
      vertical: positive(size[1], NAMED_GAP.medium),
    };
  }
  if (typeof size === 'number') {
    const gap = positive(size, NAMED_GAP.medium);
    return { horizontal: gap, vertical: gap };
  }
  if (size === 'small' || size === 'large' || size === 'medium') {
    const gap = NAMED_GAP[size];
    return { horizontal: gap, vertical: gap };
  }
  return { horizontal: NAMED_GAP.medium, vertical: NAMED_GAP.medium };
}

function positive(value: unknown, fallback: number): number {
  return typeof value === 'number' && Number.isFinite(value) && value >= 0
    ? value
    : fallback;
}
