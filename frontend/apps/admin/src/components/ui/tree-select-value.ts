/**
 * useVbenForm 的空值是 null（见 adapter/form.ts）。
 * 树选择把 null 画成未选；清除后回写 null。0 是合法键（顶级菜单）。
 * 选项键优先用 keyField，没有再退回 value / key，避免 Api 转成 value 后对不上。
 * VbenTree 会在选项还没到时把对不上的值写成 undefined，下拉不用它做选中态。
 */

export interface FormTreeSelectProps {
  childrenField?: string;
  clearable?: boolean;
  defaultExpandAll?: boolean;
  disabled?: boolean;
  filterable?: boolean;
  keyField?: string;
  labelField?: string;
  multiple?: boolean;
  options?: unknown[];
  placeholder?: string;
}

export interface TreeNode {
  children: TreeNode[];
  disabled: boolean;
  key: string | number;
  label: string;
}

export interface VisibleTreeRow {
  depth: number;
  disabled: boolean;
  hasChildren: boolean;
  key: string | number;
  label: string;
}

export function isTreeKey(value: unknown): value is string | number {
  if (typeof value === 'number') return Number.isFinite(value);
  return typeof value === 'string' && value !== '';
}

export function treeKeyId(key: string | number): string {
  return `${typeof key}:${key}`;
}

export function isTreeSelectEmpty(value: unknown): boolean {
  if (value == null) return true;
  if (Array.isArray(value)) return !value.some((item) => isTreeKey(item));
  return !isTreeKey(value);
}

function readKey(
  record: Record<string, unknown>,
  keyField: string,
): null | string | number {
  if (isTreeKey(record[keyField])) return record[keyField];
  if (keyField !== 'value' && isTreeKey(record.value)) return record.value;
  if (keyField !== 'key' && isTreeKey(record.key)) return record.key;
  return null;
}

function readLabel(
  record: Record<string, unknown>,
  labelField: string,
  key: string | number,
): string {
  const named = record[labelField];
  if (typeof named === 'string' || typeof named === 'number') return String(named);
  if (labelField !== 'label') {
    const fallback = record.label;
    if (typeof fallback === 'string' || typeof fallback === 'number') {
      return String(fallback);
    }
  }
  return String(key);
}

export function normalizeTreeOptions(
  raw: unknown,
  keyField = 'key',
  labelField = 'label',
  childrenField = 'children',
): TreeNode[] {
  if (!Array.isArray(raw)) return [];
  const nodes: TreeNode[] = [];
  for (const item of raw) {
    if (item == null || typeof item !== 'object') continue;
    const record = item as Record<string, unknown>;
    const key = readKey(record, keyField);
    if (key == null) continue;
    const childrenRaw = Array.isArray(record[childrenField])
      ? record[childrenField]
      : record.children;
    nodes.push({
      children: normalizeTreeOptions(
        childrenRaw,
        keyField,
        labelField,
        childrenField,
      ),
      disabled: record.disabled === true,
      key,
      label: readLabel(record, labelField, key),
    });
  }
  return nodes;
}

export function findTreeLabel(
  nodes: readonly TreeNode[],
  value: unknown,
): string | undefined {
  if (!isTreeKey(value)) return undefined;
  for (const node of nodes) {
    if (node.key === value) return node.label;
    const nested = findTreeLabel(node.children, value);
    if (nested != null) return nested;
  }
  return undefined;
}

export function containsTreeKey(
  nodes: readonly TreeNode[],
  value: unknown,
): boolean {
  return findTreeLabel(nodes, value) != null;
}

/** 只留下命中的节点和它们的上级。没命中的子节点不展开进来。 */
export function filterTreeNodes(
  nodes: readonly TreeNode[],
  query: string,
): TreeNode[] {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return nodes.map((node) => ({ ...node, children: node.children }));
  const result: TreeNode[] = [];
  for (const node of nodes) {
    const children = filterTreeNodes(node.children, query);
    const self =
      node.label.toLowerCase().includes(keyword) ||
      String(node.key).toLowerCase().includes(keyword);
    if (self || children.length > 0) {
      result.push({ ...node, children });
    }
  }
  return result;
}

export function parentKeyIds(nodes: readonly TreeNode[]): string[] {
  const ids: string[] = [];
  for (const node of nodes) {
    if (node.children.length > 0) {
      ids.push(treeKeyId(node.key), ...parentKeyIds(node.children));
    }
  }
  return ids;
}

export function visibleTreeRows(
  nodes: readonly TreeNode[],
  expanded: ReadonlySet<string>,
  depth = 0,
): VisibleTreeRow[] {
  const rows: VisibleTreeRow[] = [];
  for (const node of nodes) {
    const hasChildren = node.children.length > 0;
    rows.push({
      depth,
      disabled: node.disabled,
      hasChildren,
      key: node.key,
      label: node.label,
    });
    if (hasChildren && expanded.has(treeKeyId(node.key))) {
      rows.push(...visibleTreeRows(node.children, expanded, depth + 1));
    }
  }
  return rows;
}

export function commitTreeValue(value: unknown): null | string | number {
  return isTreeKey(value) ? value : null;
}

/** 多选清空后也是 null，和单选清除、表单重置一致。 */
export function toggleTreeValue(
  current: unknown,
  key: string | number,
  checked: boolean,
): null | Array<string | number> {
  const list = Array.isArray(current) ? current.filter(isTreeKey) : [];
  const without = list.filter((item) => item !== key);
  const next = checked ? [...without, key] : without;
  return next.length === 0 ? null : next;
}

export function treeSummary(
  value: unknown,
  nodes: readonly TreeNode[],
): string {
  if (!Array.isArray(value)) return '';
  return value
    .filter(isTreeKey)
    .map((item) => findTreeLabel(nodes, item) ?? String(item))
    .join('、');
}

export function filterRawTree<T extends Record<string, any>>(
  nodes: readonly T[],
  query: string,
  labelField: keyof T & string,
  childrenField: keyof T & string,
): T[] {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return [...nodes];
  const result: T[] = [];
  for (const node of nodes) {
    const rawChildren = node[childrenField];
    const children = Array.isArray(rawChildren)
      ? filterRawTree(rawChildren, query, labelField, childrenField)
      : [];
    const label = node[labelField];
    const self =
      String(label ?? '')
        .toLowerCase()
        .includes(keyword);
    if (self || children.length > 0) {
      result.push({ ...node, [childrenField]: children });
    }
  }
  return result;
}
