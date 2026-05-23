
package top.wyhao.cmn.db.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import top.wyhao.starter.core.UserContextHolder;
import top.wyhao.starter.core.model.LoginUser;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 元对象处理器配置（插入或修改时自动填充）
 *

 * @since 2022/12/22 19:52
 */
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 创建人
     */
    private static final String CREATE_USER = "createUser";
    /**
     * 创建时间
     */
    private static final String CREATE_TIME = "createTime";
    /**
     * 修改人
     */
    private static final String UPDATE_USER = "updateUser";
    /**
     * 修改时间
     */
    private static final String UPDATE_TIME = "updateTime";

    /**
     * 插入数据时填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject == null) {
            return;
        }
        LoginUser loginUser = UserContextHolder.getCurrentUser();
        if (metaObject.hasSetter(CREATE_USER) && loginUser != null) {
            this.setFieldValByName(CREATE_USER, loginUser.getUserId(), metaObject);
        }
        if (metaObject.hasSetter(CREATE_TIME)) {
            this.setFieldValByName(CREATE_TIME, LocalDateTime.now(), metaObject);
        }
    }

    /**
     * 修改数据时填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject == null) {
            return;
        }

        LoginUser loginUser = UserContextHolder.getCurrentUser();
        if (metaObject.hasSetter(UPDATE_USER) && loginUser != null) {
            this.setFieldValByName(UPDATE_USER, loginUser.getUserId(), metaObject);
        }
        if (metaObject.hasSetter(UPDATE_TIME)) {
            this.setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
        }
    }
}
