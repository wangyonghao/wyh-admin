import type { PageProps } from '../page/types';

import type { SplitSizeUnit } from './split-size';

export type { SplitSizeUnit };

export interface ColPageProps extends PageProps {
  /**
   * 左侧宽度。单位看 `leftSizeUnit`，默认百分比。
   * @default 30
   */
  leftWidth?: number;
  leftMinWidth?: number;
  leftMaxWidth?: number;
  leftCollapsedWidth?: number;
  leftCollapsible?: boolean;
  /**
   * 左侧宽度单位。`px` 时 `leftWidth` 等按像素解释。
   * @default '%'
   */
  leftSizeUnit?: SplitSizeUnit;
  /**
   * 右侧宽度。单位看 `rightSizeUnit`，默认百分比。
   * 另一侧是像素时，这个默认值不参与初始布局，右侧吃掉剩余宽度。
   * @default 70
   */
  rightWidth?: number;
  rightMinWidth?: number;
  rightCollapsedWidth?: number;
  rightMaxWidth?: number;
  rightCollapsible?: boolean;
  /**
   * 右侧宽度单位。
   * @default '%'
   */
  rightSizeUnit?: SplitSizeUnit;

  resizable?: boolean;
  splitLine?: boolean;
  splitHandle?: boolean;
}
