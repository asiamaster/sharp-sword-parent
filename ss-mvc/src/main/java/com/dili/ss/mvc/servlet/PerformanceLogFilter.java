package com.dili.ss.mvc.servlet;


import com.alibaba.fastjson.JSONObject;
import com.dili.ss.mvc.util.RequestUtils;
import com.dili.ss.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能统计拦截器
 * 可以通过URL参数r(秒)指定自动刷新时间，默认5秒
 */
@Component
@ConditionalOnExpression("'${performance.enable}'=='true'")
@DependsOn("springUtil")
@WebFilter(filterName="performanceLogFilter",urlPatterns="/*")
public class PerformanceLogFilter implements Filter {
    private final static Logger LOG = LoggerFactory.getLogger(PerformanceLogFilter.class);
    //超时临界值（毫秒）
    private int timeSpentThresholdToLog = 500;
    public static final String VIEW_PERFORMANCE_URL = "/performance.html";
    private static final Map<String, RequestHandleInfo> MAP = new ConcurrentHashMap();
    private static final int defaultRefreshTimeMs = 3000;
    //刷新时长(秒)值URI设置参数
    private static final String REFRESH_KEY = "r";
    //超时临界值(毫秒)URI设置参数
    private static final String TIME_SPENT_THRESHOLD_KEY = "t";
    //清空缓存URI设置参数
    private static final String CLEAR_KEY = "c";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //spring.application.name配置的应用名称
    private String applicationName;
    /**
     * 初始化加载
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) {
        applicationName = SpringUtil.getProperty("spring.application.name", "spring.application.name未配置");
    }

    /**
     * 拦截处理
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String requestURI = httpServletRequest.getRequestURI();
        if(VIEW_PERFORMANCE_URL.equals(requestURI)) {
            setTimeSpentThreshold(request);
            clearPerformanceLog(request);
            show(httpServletRequest, response);
            return;
        }
        long timeSpent = next(request, response, chain);
        saveTimeSpent(request, timeSpent);
    }

    /**
     * 获取性能日志缓存
     * @return
     */
    public static Map<String, RequestHandleInfo> getPerformanceLogMap(){
        return MAP;
    }

    /**
     * 清空性能日志缓存
     */
    private void clearPerformanceLog(ServletRequest request){
        String CLEAR = request.getParameter(CLEAR_KEY);
        if(CLEAR != null && CLEAR.equals("clear")){
            MAP.clear();
        }
    }

    /**
     * 设置超时临界值
     * @param request
     */
    private void setTimeSpentThreshold(ServletRequest request){
        String TIME_SPENT_THRESHOLD = request.getParameter(TIME_SPENT_THRESHOLD_KEY);
        if(TIME_SPENT_THRESHOLD != null){
            this.setTimeSpentThresholdToLog(Integer.parseInt(TIME_SPENT_THRESHOLD));
        }
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
            requestHandleInfo.firstCostSeconds = timeSpent;
            requestHandleInfo.averageCostSeconds = (float)timeSpent;
            requestHandleInfo.lastAccessTime = new Date();
            requestHandleInfo.firstAccessTime = new Date();
            MAP.put(uri, requestHandleInfo);
        }
    }

    /**
     * 显示性能统计
     * @param request
     * @param response
     * @throws IOException
     */
    private void show(HttpServletRequest request, ServletResponse response) throws IOException {
        StringBuilder timeBefore = new StringBuilder();
        timeBefore.append("<!DOCTYPE html>\n");
        timeBefore.append("<html>\n");
        timeBefore.append("<head lang=\"en\">\n");
        timeBefore.append("    <meta charset=\"UTF-8\">\n");
        timeBefore.append("    <title>性能统计</title>\n");
        timeBefore.append("</head>\n");
        timeBefore.append("<body>\n");
        timeBefore.append("<div style=\" margin:0 auto; margin-top: 15px;\" align=\"center\">\n");
        timeBefore.append("<div style=\"font-weight:bold; font-size:30px;\">").append(applicationName).append("性能统计</div>");
        timeBefore.append(RequestUtils.getIpAddress(request)).append(":").append(request.getServerPort()).append("\n");
        timeBefore.append("    <table style=\"text-align: center;border-collapse: collapse; border: 1px #999999 solid;line-height:28px;\" border=\"1\">\n");
        timeBefore.append("        <tr style=\"background-color: lightskyblue;\">\n");
        timeBefore.append("            <td>URI</td>\n");
        timeBefore.append("            <td width=\"150\">超过").append(timeSpentThresholdToLog).append("毫秒次数</td>\n");
        timeBefore.append("            <td width=\"130\">最小耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"130\">最大耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"130\">首次耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"130\">平均耗时(毫秒)</td>\n");
        timeBefore.append("            <td width=\"190\">首次访问时间</td>\n");
        timeBefore.append("            <td width=\"190\">上次访问时间</td>\n");
        timeBefore.append("        </tr>\n");
        List<RequestHandleInfo> infoList = new ArrayList(MAP.values());
        Collections.sort(infoList, (PerformanceLogFilter.RequestHandleInfo r1, PerformanceLogFilter.RequestHandleInfo r2) ->  r1.averageCostSeconds < r2.averageCostSeconds?1:-1);
        Iterator timeAfter = infoList.iterator();
        while(timeAfter.hasNext()) {
            PerformanceLogFilter.RequestHandleInfo info = (PerformanceLogFilter.RequestHandleInfo)timeAfter.next();
            timeBefore.append("    <tr>\n");
            timeBefore.append("        <td>").append(info.uri).append("</td>\n");
            timeBefore.append("        <td>").append(info.accessCount).append("</td>\n");
            timeBefore.append("        <td>").append(info.minCostSeconds).append("</td>\n");
            timeBefore.append("        <td>").append(info.maxCostSeconds).append("</td>\n");
            timeBefore.append("        <td>").append(info.firstCostSeconds).append("</td>\n");
            timeBefore.append("        <td>").append(info.averageCostSeconds).append("</td>\n");
            timeBefore.append("        <td>").append(format.format(info.firstAccessTime)).append("</td>\n");
            timeBefore.append("        <td>").append(format.format(info.lastAccessTime)).append("</td>\n");
            timeBefore.append("    </tr>\n");
        }
        timeBefore.append("    </table>\n");
        timeBefore.append("</div>\n");
        timeBefore.append("</body>\n");
        timeBefore.append("<script >\n");
        timeBefore.append("function getQueryVariable(variable)\n");
        timeBefore.append("{\n");
        timeBefore.append("   var query = window.location.search.substring(1);\n");
        timeBefore.append("   var vars = query.split(\"&\");\n");
        timeBefore.append("   for (var i=0;i<vars.length;i++) {\n");
        timeBefore.append("       var pair = vars[i].split(\"=\");\n");
        timeBefore.append("       if(pair[0] == variable){return pair[1];}\n");
        timeBefore.append("   }\n");
        timeBefore.append("   return null;\n");
        timeBefore.append("}\n");
        timeBefore.append("function refresh()\n");
        timeBefore.append("{\n");
        timeBefore.append("   window.location.reload();\n");
        timeBefore.append("}\n");
        timeBefore.append("let refreshTimes = getQueryVariable(\"").append(REFRESH_KEY).append("\");\n");
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
        //第一次消耗时间
        long firstCostSeconds;
        //上次访问时间
        Date lastAccessTime;
        //首次访问时间
        Date firstAccessTime;

        private RequestHandleInfo() {
        }
    }
}

