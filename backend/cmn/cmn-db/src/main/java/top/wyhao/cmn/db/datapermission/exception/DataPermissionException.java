
package top.wyhao.cmn.db.datapermission.exception;

/**
 * 数据权限异常
 *

 * @since 2.13.2
 */
public class DataPermissionException extends RuntimeException {

    public DataPermissionException(String message) {
        super(message);
    }

    public static DataPermissionException unsupportedDataScope(String dataScope) {
        return new DataPermissionException("Unsupported data scope: " + dataScope);
    }

    public static DataPermissionException unsupportedDatabase(String database) {
        return new DataPermissionException("Unsupported database for data permission: " + database);
    }

    public static DataPermissionException invalidUserData(String message) {
        return new DataPermissionException("Invalid user data: " + message);
    }

    public static DataPermissionException methodNotFound(String mappedStatementId) {
        return new DataPermissionException("Method not found for data permission: " + mappedStatementId);
    }
}