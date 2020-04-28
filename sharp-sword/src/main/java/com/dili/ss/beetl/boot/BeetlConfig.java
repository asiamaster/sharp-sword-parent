package com.dili.ss.beetl.boot;

import com.dili.ss.beetl.CommonTagFactory;
import org.beetl.core.Format;
import org.beetl.core.Function;
import org.beetl.core.GroupTemplate;
import org.beetl.core.VirtualAttributeEval;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.core.tag.Tag;
import org.beetl.core.tag.TagFactory;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.ext.spring.BeetlSpringViewResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * beetl配置
 * Created by asiamastor on 2017/1/20.
 */
@Configuration
@ConditionalOnExpression("'${beetl.enable}'=='true'")
@SuppressWarnings("all")
public class BeetlConfig {

    @Autowired
    private List<VirtualAttributeEval> virtualAttributeEval;
    @Autowired
    private Map<String, Function> functions;
    @Autowired
    private Map<String, Tag> tags;
    @Autowired
    Map<String, Format> formats;

    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${beetl.templatesPath:templates}")
    String templatesPath;

    //beetl字符串模板
    @Bean("StringGroupTemplate")
    public GroupTemplate getStringGroupTemplate() throws IOException {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = buildBeetlGroupUtilConfiguration();
        beetlGroupUtilConfiguration.setResourceLoader(new StringTemplateResourceLoader());
        beetlGroupUtilConfiguration.init();
        return beetlGroupUtilConfiguration.getGroupTemplate();
    }

    @Bean(initMethod = "init", name = "beetlGroupUtilConfiguration")
    public BeetlGroupUtilConfiguration getBeetlGroupUtilConfiguration() {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = buildBeetlGroupUtilConfiguration();
        //配置加载loader
        //获取Spring Boot 的ClassLoader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(loader==null){
            loader = BeetlConfig.class.getClassLoader();
        }
        //解决部署后找不到模板问题,但是要在下面的beetlViewResolver中配置beetlSpringViewResolver.setPrefix("/templates/");
        //并且在beetl.properties中改为RESOURCE.tagRoot =templates/htmltag,不然找不到html标签
        ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader(loader, templatesPath);
        beetlGroupUtilConfiguration.setResourceLoader(classpathResourceLoader);
        beetlGroupUtilConfiguration.init();
        //如果使用了优化编译器，涉及到字节码操作，需要添加ClassLoader
        beetlGroupUtilConfiguration.getGroupTemplate().setClassLoader(loader);
        return beetlGroupUtilConfiguration;
    }

    @Bean(name = "beetlViewResolver")
    public BeetlSpringViewResolver getBeetlSpringViewResolver(@Qualifier("beetlGroupUtilConfiguration") BeetlGroupUtilConfiguration beetlGroupUtilConfiguration) {
        BeetlSpringViewResolver beetlSpringViewResolver = new BeetlSpringViewResolver();
        beetlSpringViewResolver.setContentType("text/html;charset=UTF-8");
        beetlSpringViewResolver.setOrder(0);
        beetlSpringViewResolver.setPrefix("/");
        beetlSpringViewResolver.setSuffix(".html");
        beetlSpringViewResolver.setConfig(beetlGroupUtilConfiguration);
        beetlSpringViewResolver.setRedirectHttp10Compatible(false);
        return beetlSpringViewResolver;
    }

    /**
     * 构建一个初始化的BeetlGroupUtilConfiguration
     * @return
     */
    private BeetlGroupUtilConfiguration buildBeetlGroupUtilConfiguration() {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();
//        try {
//            // WebAppResourceLoader ����root·���ǹؼ�
//            WebAppResourceLoader webAppResourceLoader = new WebAppResourceLoader();
//            beetlGroupUtilConfiguration.setResourceLoader(webAppResourceLoader);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //此方式是闲大赋作者的提供的，但是在部署后，jar包路径要报错
        //因为部署后模板在jar中，所以不能用WebResourceLoader（它是按照文件路径加载的）
//        String root =  patternResolver.getResource("classpath:").getFile().toString();
//        WebAppResourceLoader webAppResourceLoader = new WebAppResourceLoader(root);
//        beetlGroupUtilConfiguration.setResourceLoader(webAppResourceLoader);

        ResourcePatternResolver patternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
        beetlGroupUtilConfiguration.setConfigFileResource(patternResolver.getResource("classpath:conf/beetl.properties"));
        InputStream inputStream = BeetlConfig.class.getResourceAsStream("/conf/beetlSharedVars.properties");
        Properties p = new Properties();
        try {
            if(inputStream != null) {
                //p.load(inputStream);//直接这么写，如果properties文件中有汉子，则汉字会乱码。因为未设置编码格式。
//                p.load(new InputStreamReader(inputStream, "utf-8"));
                p.load(new InputStreamReader(inputStream, "UTF-8"));
                inputStream.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        p.put("contextPath", "${server.servlet.context-path}".equals(contextPath) ?"":contextPath);
        beetlGroupUtilConfiguration.setSharedVars((Map)p);
        beetlGroupUtilConfiguration.setVirtualAttributeEvals(virtualAttributeEval);
        beetlGroupUtilConfiguration.setFunctions(functions);
        beetlGroupUtilConfiguration.setFormats(formats);
        beetlGroupUtilConfiguration.setTagFactorys(getTagFactoryMaps());
        return beetlGroupUtilConfiguration;
    }

    private Map<String, TagFactory> getTagFactoryMaps(){
        Map<String, TagFactory> tagFactoryMap = new HashMap<>(tags.size());
        for(Map.Entry<String, Tag> entry : tags.entrySet()) {
            tagFactoryMap.put(entry.getKey(), new CommonTagFactory(entry.getValue()));
        }
        return tagFactoryMap;
    }



}
