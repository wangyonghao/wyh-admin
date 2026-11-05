import type { ClassValue } from 'clsx';

import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/** Same class merger the shadcn atoms use. */
function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export { cn };
