package com.dili.ss.mongodb.boot;

//升级Spring-Boot 2.3.2后，因类变动，注释掉
/**
 * 多了_class的意义，在下面这个链接有很好的解答：
 http://stackoverflow.com/questions/6810488/spring-data-mongodb-mappingmongoconverter-remove-class/
 ，其实就是说，为了在把document转换成Java对象时能够转换到具体的子类.
 要去掉的方法简单：DefaultMongoTypeMapper类的构造函数的第一个参数是Type在MongoDB中名字. 设置为null的话就不会在保存时自动添加_class属性.所以需要覆写
 MappingMongoConverter
 * Created by asiamastor on 2017/1/3.
 */
//@Configuration
//@ConfigurationProperties(prefix = "spring.data.mongodb", ignoreInvalidFields=true)
//@ConditionalOnExpression("'${mongodb.enable}'=='true'")
//public class SpringMongoConfig extends AbstractMongoConfiguration {
//
//    private String database;
//    private String host;
//    private int port;
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    private String username;
//    private String password;
//
//    @Override
//    protected String getDatabaseName() {
//        return database;
//    }
//
//    @Override
//    public MongoClient mongoClient() {
//        if(StringUtils.isBlank(password) || StringUtils.isBlank(username)){
//            return new MongoClient(host, port);
//        }else {
//            return new MongoClient(new ServerAddress(host, port), Lists.newArrayList(MongoCredential.createCredential(username, database, password.toCharArray())));
//        }
//    }
//
//    @Override
//    @Bean
//    public MongoTemplate mongoTemplate() {
//        // overwrite type mbg to get rid of the _class column
////      get the converter from the base class instead of creating it
////      def converter = new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext())
//        MappingMongoConverter converter = null;
//        try {
//            converter = mappingMongoConverter();
//            converter.setTypeMapper(new DefaultMongoTypeMapper(null));
//            return new MongoTemplate(mongoDbFactory(), converter);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void setDatabase(String database) {
//        this.database = database;
//    }
//
//    public void setHost(String host) {
//        this.host = host;
//    }
//
//    public void setPort(int port) {
//        this.port = port;
//    }
//}



//    public
//    @Bean
//    MongoDbFactory mongoDbFactory() throws Exception {
//        return new SimpleMongoDbFactory(new Mongo("10.28.10.206",27017), "agriez_gateway");
//    }
//
//    public
//    @Bean
//    MongoTemplate mongoTemplate() throws Exception {
//        MappingMongoConverter converter =
//                new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext());
//        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(), converter);
//        return mongoTemplate;
//
//    }

