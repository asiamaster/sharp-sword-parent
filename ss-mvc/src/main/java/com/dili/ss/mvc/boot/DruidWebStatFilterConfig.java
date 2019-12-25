package com.dili.ss.mvc.boot;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * druid监控,访问地址:http://localhost/druid/index.html
 * 登录名:admin,密码:123456
 * Created by asiamastor on 2017/1/20.
 */
//@Configuration
//@ConditionalOnExpression("'${druid-filter.enable}'=='true'")
//@ConfigurationProperties(prefix="druid-filter", ignoreInvalidFields = true)
//@PropertySource({"classpath:application.properties"})
public class DruidWebStatFilterConfig {

    @Value("${loginUsername:admin}")
    private String loginUsername;

    @Value("${loginPassword:123456}")
    private String loginPassword;

    @Value("${resetEnable:true}")
    private String resetEnable;

    @Value("${allow:}")
    private String allow;

    @Value("${deny:}")
    private String deny;

    //   解决spring boot druid SQL监控无数据
//    @Bean
//    @ConfigurationProperties(prefix="spring.datasource")
//    public DataSource druidDataSource() {
//        return new DruidDataSource();
//    }

    //    Druid 的 StatViewServlet
//    访问:http://gateway.1n4j.com/druid/index.html
    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/druid/*");
        //添加初始化参数：initParams
//        //白名单：没有配置或者为空，则允许所有访问
        if(StringUtils.isNoneBlank(allow)) {
            servletRegistrationBean.addInitParameter("allow", allow);
        }
        if(StringUtils.isNoneBlank(deny)) {
//        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
            servletRegistrationBean.addInitParameter("deny", deny);
        }
//        //登录查看信息的账号密码.
        servletRegistrationBean.addInitParameter("loginUsername",loginUsername);
        servletRegistrationBean.addInitParameter("loginPassword",loginPassword);
//        //是否能够重置数据.
        servletRegistrationBean.addInitParameter("resetEnable",resetEnable);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean druidWebStatFilter() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(new WebStatFilter());
        reg.addUrlPatterns("/*");
        reg.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");
        reg.addInitParameter("sessionStatMaxCount","2000");
        reg.addInitParameter("sessionStatEnable","true");
        reg.addInitParameter("profileEnable","true");
        reg.addInitParameter("principalCookieName","SessionId");
        return reg;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getResetEnable() {
        return resetEnable;
    }

    public void setResetEnable(String resetEnable) {
        this.resetEnable = resetEnable;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getDeny() {
        return deny;
    }

    public void setDeny(String deny) {
        this.deny = deny;
    }
}
