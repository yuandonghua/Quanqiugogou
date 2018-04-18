package com.shopnum1.distributionportal.config;

/**
 * @description: 配置控制类
 * @author: 袁东华
 * @time: created at 2018/4/18 下午4:54
 */
public class Config {
    //是否输出日志
    private static boolean debug=true;

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }
}
