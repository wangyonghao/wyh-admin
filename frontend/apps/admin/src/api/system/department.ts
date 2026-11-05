import http, { download } from '#/utils/request';

/* ==================== API 定义 ==================== */
export const departmentApi = {
  /** 查询部门列表 */
  tree: (query: DepartmentQuery) => {
    return http.get<DepartmentResult[]>(`/system/department/tree`, { params: query });
  },
  /** 查询部门详情 */
  get: (id: string) => {
    return http.get<DepartmentResult>(`/system/department/${id}`);
  },
  /** 新增部门 */
  create: (data: any) => {
    return http.post<boolean>(`/system/department`, data);
  },
  /** 修改部门 */
  update: (data: any, id: string) => {
    return http.patch(`/system/department/${id}`, data);
  },
  /** 删除部门 */
  delete: (id: string) => {
    return http.delete(`/system/department`, { data: { ids: [id] } });
  },
  /** 导出部门 */
  export: (query: DepartmentQuery) => {
    return download(`/system/department/export`, { params: query });
  },
  /** 查询部门字典树 */
  option: (query: { keyword: string | unknown }) => {
    return http.get<DepartmentResult[]>(`/system/department/dict/tree`, { params: query });
  },
};

/* ==================== Schema 定义 ==================== */
/** 部门类型 */
export interface DepartmentResult {
  id: string;
  parentId: string;
  name: string;
  code: string;
  type: number;
  sort: number;
  status: 1 | 2;
  isBuiltin: boolean;
  description: string;
  createUserString: string;
  createTime: string;
  updateUserString: string;
  updateTime: string;
  children: DepartmentResult[];
}
export interface DepartmentQuery {
  keyword?: string;
  status?: number;
}
