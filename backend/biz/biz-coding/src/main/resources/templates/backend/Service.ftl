package ${packageName}.${subPackageName};

import top.wyhao.admin.common.base.service.BaseService;
import ${packageName}.model.entity.${classNamePrefix}DO;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;
import model.top.wyhao.cmn.db.IService;

/**
 * ${businessName}业务接口
 *

 * @since ${datetime}
 */
public interface ${className} extends BaseService<${classNamePrefix}Resp, ${classNamePrefix}DetailResp, ${classNamePrefix}Query, ${classNamePrefix}Req>, IService<${classNamePrefix}DO> {}