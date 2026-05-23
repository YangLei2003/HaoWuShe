package com.hongshu.web.manager.factory;

import com.hongshu.common.constant.Constants;
import com.hongshu.common.utils.LogUtils;
import com.hongshu.common.utils.ServletUtils;
import com.hongshu.common.utils.StringUtils;
import com.hongshu.common.utils.ip.AddressUtils;
import com.hongshu.common.utils.ip.IpUtils;
import com.hongshu.common.utils.spring.SpringUtils;
import com.hongshu.web.domain.entity.WebLoginLog;
import com.hongshu.web.service.ISysLoginInforService;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @Author hongshu
 */
public class AsyncFactory {

    private static final Logger web_user_logger = LoggerFactory.getLogger("web-user");
    public static TimerTask recordLoginInfor(final String uid, final String username, final String status, final String message,
                                             final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                String s = LogUtils.getBlock(ip) +
                        address +
                        LogUtils.getBlock(username) +
                        LogUtils.getBlock(status) +
                        LogUtils.getBlock(message);
                // 打印信息到日志
                web_user_logger.info(s, args);
            }
        };
    }
}