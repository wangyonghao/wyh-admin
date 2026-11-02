
package top.wyhao.system.config.satoken;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.open.exception.OpenApiException;
import top.wyhao.system.open.model.entity.SysApp;
import top.wyhao.system.open.service.AppService;

import java.util.Map;

/**
 * API 参数签名
 *


 * @since 2024/10/17 16:03
 */
@Component
@RequiredArgsConstructor
public class ApiSignTemplate extends SaSignTemplate {

    private final AppService appService;
    public static final String ACCESS_KEY = "accessKey";

    @Override
    public void checkParamMap(Map<String, String> paramMap) {
        // 获取必须的参数
        String timestampValue = paramMap.get(timestamp);
        String nonceValue = paramMap.get(nonce);
        String signValue = paramMap.get(sign);
        String accessKeyValue = paramMap.get(ACCESS_KEY);

        // 校验
        if (CharSequenceUtil.isBlank(timestampValue)) {
            throw OpenApiException.signParamMissing(timestamp);
        }
        if (CharSequenceUtil.isBlank(nonceValue)) {
            throw OpenApiException.signParamMissing(nonce);
        }
        if (CharSequenceUtil.isBlank(signValue)) {
            throw OpenApiException.signParamMissing(sign);
        }
        if (CharSequenceUtil.isBlank(accessKeyValue)) {
            throw OpenApiException.signParamMissing(ACCESS_KEY);
        }
        SysApp app = appService.getByAccessKey(accessKeyValue);
        if (app == null) {
            throw OpenApiException.accessKeyInvalid();
        }
        if (ObjectUtil.equal(StatusEnum.DISABLE, app.getStatus())) {
            throw OpenApiException.appDisabled();
        }
        if (app.isExpired()) {
            throw OpenApiException.appExpired();
        }

        // 依次校验三个参数
        super.checkTimestamp(Long.parseLong(timestampValue));
        super.checkNonce(nonceValue);
        paramMap.put(key, app.getSecretKey());
        super.checkSign(paramMap, signValue);
    }

    @Override
    public String createSign(Map<String, ?> paramMap) {
        if (ObjectUtil.isEmpty(paramMap.get(key))) {
            throw OpenApiException.secretKeyMissing();
        }
        // 移除 sign 参数
        paramMap.remove(sign);
        // 计算签名
        return SaSecureUtil.md5(super.joinParamsDictSort(paramMap));
    }
}
