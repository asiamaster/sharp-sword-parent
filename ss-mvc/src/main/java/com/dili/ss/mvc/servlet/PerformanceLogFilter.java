package com.dili.ss.mvc.servlet;


import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能分析拦截器
 * 可以通过URL参数refresh(秒)指定自动刷新时间，默认5秒
 */
@Component
@ConditionalOnExpression("'${performance.enable}'=='true'")
@WebFilter(filterName="logFilter",urlPatterns="/*")
public class PerformanceLogFilter implements Filter {
    private final static Logger LOG = LoggerFactory.getLogger(PerformanceLogFilter.class);
    //超时临界值（毫秒）
    private int timeSpentThresholdToLog = 500;
    public static final String VIEW_PERFORMANCE_URL = "/performance.html";
    private static final Map<String, RequestHandleInfo> MAP = new ConcurrentHashMap();
    private static final int defaultRefreshTimeMs = 3000;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String requestURI = httpServletRequest.getRequestURI();
        if(requestURI.equals(VIEW_PERFORMANCE_URL)) {
            show(response);
            return;
        }
        long timeSpent = next(request, response, chain);
        saveTimeSpent(request, timeSpent);
    }

    /**
     * 执行业务逻辑
     * @param request
     * @param response
     * @param chain
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private long next(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        long timeBefore1 = System.currentTimeMillis();
        chain.doFilter(request, response);
        long timeAfter1 = System.currentTimeMillis();
        return timeAfter1 - timeBefore1;
    }

    /**
     * 保存执行时间
     * @param request
     * @param timeSpent
     */
    private void saveTimeSpent(ServletRequest request, long timeSpent) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String requestURI = httpServletRequest.getRequestURI();
        if(timeSpent < (long)this.timeSpentThresholdToLog) {
            return;
        }
        LOG.info("{} millisecond to process request:[{}] Param Map:{}", timeSpent, requestURI, JSONObject.toJSONString(request.getParameterMap()));
        String uri;
        //去掉URI参数
        if(requestURI.contains("?")) {
            uri = requestURI.substring(0, requestURI.indexOf("?"));
        } else {
            uri = requestURI;
        }

        RequestHandleInfo requestHandleInfo;
        if(MAP.containsKey(uri)) {
            requestHandleInfo = MAP.get(uri);
            int lastAccessCount = requestHandleInfo.accessCount;
            float lastAverageCostSeconds = requestHandleInfo.averageCostSeconds;
            requestHandleInfo.accessCount = lastAccessCount + 1;
            if(timeSpent < requestHandleInfo.minCostSeconds){
                requestHandleInfo.minCostSeconds = timeSpent;
            }else if(timeSpent > requestHandleInfo.maxCostSeconds){
                requestHandleInfo.maxCostSeconds = timeSpent;
            }
            requestHandleInfo.averageCostSeconds = (lastAverageCostSeconds * (float)lastAccessCount + (float)timeSpent) / (float)(lastAccessCount + 1);
            requestHandleInfo.lastAccessTime = new Date();
        } else {
            requestHandleInfo = new RequestHandleInfo();
            requestHandleInfo.uri = uri;
            requestHandleInfo.accessCount = 1;
            requestHandleInfo.minCostSeconds = timeSpent;
            requestHandleInfo.maxCostSeconds = timeSpent;
            requestHandleInfo.averageCostSeconds = (float)timeSpent;
            requestHandleInfo.lastAccessTime = new Date();
            MAP.put(uri, requestHandleInfo);
        }
    }

    /**
     * 显示性能统计
     * @param response
     * @throws IOException
     */
    private void show(ServletResponse response) throws IOException {
        StringBuilder timeBefore = new StringBuilder();
        timeBefore.append("<!DOCTYPE html>\n");
        timeBefore.append("<html>\n");
        timeBefore.append("<head lang=\"en\">\n");
        timeBefore.append("    <meta charset=\"UTF-8\">\n");
        timeBefore.append("    <title>性能统计</title>\n");
        timeBefore.append("</head>\n");
        timeBefore.append("<body>\n");
        timeBefore.append("<div style=\" margin:0 auto; width:1023px;margin-top: 50px;\">\n");
        timeBefore.append("    <table style=\"text-align: center;border-collapse: collapse; border: 1px #999999 solid;line-height:28px;\" border=\"1\">\n");
        timeBefore.append("        <tr>\n");
        timeBefore.append("            <td>URI</td>\n");
        timeBefore.append("            <td width=\"130\">超过0.5秒次数</td>\n");
        timeBefore.append("            <td width=\"130\">平均耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"130\">最小耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"130\">最大耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"190\">上次访问时间</td>\n");
        timeBefore.append("        </tr>\n");
        List<RequestHandleInfo> infoList = new ArrayList(MAP.values());
        Collections.sort(infoList, new Comparator<RequestHandleInfo>() {
            @Override
            public int compare(PerformanceLogFilter.RequestHandleInfo r1, PerformanceLogFilter.RequestHandleInfo r2) {
                return r1.averageCostSeconds < r2.averageCostSeconds?1:-1;
            }
        });
        Iterator timeAfter = infoList.iterator();

        while(timeAfter.hasNext()) {
            PerformanceLogFilter.RequestHandleInfo info = (PerformanceLogFilter.RequestHandleInfo)timeAfter.next();
            timeBefore.append("        <tr>\n");
            timeBefore.append("            <td>").append(info.uri).append("</td>\n");
            timeBefore.append("            <td>").append(info.accessCount).append("</td>\n");
            timeBefore.append("            <td>").append(info.averageCostSeconds).append("</td>\n");
            timeBefore.append("            <td>").append(info.minCostSeconds).append("</td>\n");
            timeBefore.append("            <td>").append(info.maxCostSeconds).append("</td>\n");
            timeBefore.append("            <td>").append(format.format(info.lastAccessTime)).append("</td>\n");
            timeBefore.append("        </tr>\n");
        }
        timeBefore.append("    </table>\n");
        timeBefore.append("</div>\n");
        timeBefore.append("</body>\n");
        timeBefore.append("<script >\n");
        timeBefore.append("function getQueryVariable(variable)\n");
        timeBefore.append("{\n");
        timeBefore.append("       var query = window.location.search.substring(1);\n");
        timeBefore.append("       var vars = query.split(\"&\");\n");
        timeBefore.append("       for (var i=0;i<vars.length;i++) {\n");
        timeBefore.append("               var pair = vars[i].split(\"=\");\n");
        timeBefore.append("               if(pair[0] == variable){return pair[1];}\n");
        timeBefore.append("       }\n");
        timeBefore.append("       return null;\n");
        timeBefore.append("}\n");
        timeBefore.append("function refresh()\n");
        timeBefore.append("{\n");
        timeBefore.append("       window.location.reload();\n");
        timeBefore.append("}\n");
        timeBefore.append("let refreshTimes = getQueryVariable(\"refresh\");\n");
        timeBefore.append("setTimeout('refresh()', refreshTimes == null ? ").append(defaultRefreshTimeMs).append(" : parseInt(refreshTimes) * 1000);\n");
        timeBefore.append("</script>\n");
        timeBefore.append("</html>\n");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(timeBefore.toString());
    }

    public void setTimeSpentThresholdToLog(int timeSpentThresholdToLog) {
        this.timeSpentThresholdToLog = timeSpentThresholdToLog;
    }

    @Override
    public void destroy() {
    }

    /**
     * 请求处理信息
     */
    private class RequestHandleInfo {
        String uri;
        //访问超时次数
        int accessCount;
        //平均耗时(毫秒)
        float averageCostSeconds;
        //超时的最小消耗时间
        long minCostSeconds;
        //超时的最大消耗时间
        long maxCostSeconds;
        //上次访问时间
        Date lastAccessTime;

        private RequestHandleInfo() {
        }
    }
}

