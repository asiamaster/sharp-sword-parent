package com.dili.ss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 */
public class RegularUtils {

    //匹配花括号内的内容
    private static Pattern BRACE_PATTERN = Pattern.compile ("\\{([^}]*)\\}");

    /**
     * 获取花括号内字符串
     * @param str
     * @return
     */
    public static List<String> listBraceParam(String str) {
        ArrayList<String> strings = new ArrayList<>();
        Matcher matcher = BRACE_PATTERN.matcher(str);
        while (matcher.find()) {
            strings.add(matcher.group(1));
        }
        return strings;
    }
}
