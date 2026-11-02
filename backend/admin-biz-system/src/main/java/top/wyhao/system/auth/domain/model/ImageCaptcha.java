package top.wyhao.system.auth.domain.model;

/**
 * 图形验证码生成结果（domain，不依赖 app DTO）。
 */
public record ImageCaptcha(
        String uuid,
        String img,
        Long expireTime,
        boolean alwaysRequired
) {

    public static ImageCaptcha disabled() {
        return new ImageCaptcha(null, null, null, false);
    }

    public ImageCaptcha withAlwaysRequired(boolean alwaysRequired) {
        return new ImageCaptcha(uuid, img, expireTime, alwaysRequired);
    }
}
