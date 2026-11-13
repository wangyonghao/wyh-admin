export type SplitSizeUnit = '%' | 'px';

export interface SplitPaneSize {
  collapsedSize?: number;
  defaultSize?: number;
  maxSize?: number;
  minSize?: number;
  sizeUnit: SplitSizeUnit;
}

export interface SplitPaneInput {
  collapsedWidth?: number;
  maxWidth?: number;
  minWidth?: number;
  sizeUnit?: SplitSizeUnit;
  width?: number;
}

/**
 * 百分比面板照原样交给 Splitter。
 * 一侧是像素时，另一侧的百分比默认宽度丢掉，让它吃掉剩余空间，避免 200px 和 70% 互相归一。
 * 像素侧的 min / max 仍按像素。
 */
export function resolveSplitPanes(input: {
  left: SplitPaneInput;
  right: SplitPaneInput;
}): { left: SplitPaneSize; right: SplitPaneSize } {
  const leftUnit = input.left.sizeUnit ?? '%';
  const rightUnit = input.right.sizeUnit ?? '%';
  const left = toPaneSize(input.left, leftUnit);
  const right = toPaneSize(input.right, rightUnit);

  if (leftUnit === 'px' && rightUnit === '%') {
    right.defaultSize = undefined;
  } else if (rightUnit === 'px' && leftUnit === '%') {
    left.defaultSize = undefined;
  }

  return { left, right };
}

function toPaneSize(
  input: SplitPaneInput,
  sizeUnit: SplitSizeUnit,
): SplitPaneSize {
  return {
    collapsedSize: input.collapsedWidth,
    defaultSize: input.width,
    maxSize: input.maxWidth,
    minSize: input.minWidth,
    sizeUnit,
  };
}
