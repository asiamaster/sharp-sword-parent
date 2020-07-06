package com.dili.ss.boot;//package com.dili.ss.boot;
//
//import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
//import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
//import org.jasypt.spring31.properties.EncryptablePropertySourcesPlaceholderConfigurer;
//import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
//import org.springframework.core.io.ClassPathResource;
//
///**
// * 由于spring boot中@Value注解无法获取${xxx}形式的变量，所以只能在Application.java上使用注解:
// * @EnableEncryptableProperties
// * @PropertySource(name="EncryptedProperties", value = "classpath:security.properties")
// * 或直接配置:
// * @EncryptablePropertySource(name = "EncryptedProperties", value = "classpath:security.properties")
// * 并配置application.properties属性jasypt.encryptor.password=security(密码)
// * Created by asiam on 2017/4/6 0006.
// */
////@Configuration
////@ConditionalOnClass({StandardPBEByteEncryptor.class, EncryptablePropertySourcesPlaceholderConfigurer.class})
////@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
//public class EncryptPropertyPlaceholderAutoConfiguration {
//
//    private static final String SECURITY_PROPERTIES_FILE = "security.properties";
//    public static final String PASSWORD = "security";
//
////    @Bean
////    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
//        encryptor.setAlgorithm(StandardPBEByteEncryptor.DEFAULT_ALGORITHM);
//        encryptor.setPassword(PASSWORD);
//        EncryptablePropertySourcesPlaceholderConfigurer
//                configurer = new EncryptablePropertySourcesPlaceholderConfigurer(encryptor);
//        configurer.setLocation(new ClassPathResource(SECURITY_PROPERTIES_FILE));
//        return configurer;
//    }
//
//}