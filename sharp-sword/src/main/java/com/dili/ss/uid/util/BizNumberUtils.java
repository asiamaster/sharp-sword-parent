package com.dili.ss.uid.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 业务号工具类
 */
public class BizNumberUtils {

    /**
     * 获取日期加每日计数量的初始化字符串，最低位从1开始
     * @param dateStr
     * @param length 编码位数(不包含日期位数)
     * @return
     */
    public static Long getInitBizNumber(String dateStr, int length) {
        return StringUtils.isBlank(dateStr) ? 1 : NumberUtils.toLong(dateStr) * new Double(Math.pow(10, length)).longValue() + 1;
    }
}
