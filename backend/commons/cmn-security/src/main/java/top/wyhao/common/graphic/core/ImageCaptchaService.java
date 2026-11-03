
package top.wyhao.common.graphic.core;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import com.wf.captcha.base.Captcha;
import top.wyhao.common.graphic.autoconfigure.GraphicCaptchaProperties;

import java.awt.*;

/**
 * 图形验证码服务接口
 *

 * @since 1.4.0
 */
public class ImageCaptchaService {

    private final GraphicCaptchaProperties properties;

    public ImageCaptchaService(GraphicCaptchaProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取验证码实例
     *
     * @return 验证码实例
     */
    public Captcha createCaptchaImage() {
        Captcha captcha = ReflectUtil.newInstance(properties.getType().getCaptchaImpl(), properties
            .getWidth(), properties.getHeight());
        captcha.setLen(properties.getLength());
        if (CharSequenceUtil.isNotBlank(properties.getFontName())) {
            captcha.setFont(new Font(properties.getFontName(), Font.PLAIN, properties.getFontSize()));
        }
        return captcha;
    }
}
