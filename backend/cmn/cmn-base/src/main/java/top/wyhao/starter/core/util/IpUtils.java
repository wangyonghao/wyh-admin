
package top.wyhao.starter.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HtmlUtil;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.dreamlu.mica.ip2region.core.IpInfo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * IP 工具类
 *

 * @since 1.0.0
 */
public class IpUtils {

    private IpUtils() {
    }

    /**
     * 查询 IP 归属地（本地库解析）
     *
     * @param ip IP 地址
     * @return IP 归属地
     */
    public static String getRegion(String ip) {
        if (CharSequenceUtil.isBlank(ip)) {
            return "未知地址";
        }
        if (isInnerIpv4(ip)) {
            return "内网IP";
        }

        Ip2regionSearcher ip2regionSearcher = SpringUtil.getBean(Ip2regionSearcher.class);
        IpInfo ipInfo = ip2regionSearcher.memorySearch(ip);
        if (ipInfo == null) {
            return "未知地址";
        }
        Set<String> regionSet = CollUtil.newLinkedHashSet(ipInfo.getCountry(), ipInfo.getRegion(), ipInfo
            .getProvince(), ipInfo.getCity(), ipInfo.getIsp());
        regionSet.removeIf(Objects::isNull);
        return String.join("|", regionSet);
    }

    /**
     * 是否为内网 IPv4
     *
     * @param ip IP 地址
     * @return 是否为内网 IP
     */
    public static boolean isInnerIpv4(String ip) {
        return NetUtil.isInnerIP("0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip));
    }
}
