package com.dili.ss.dto;

import javassist.CtClass;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * DTO工厂工具
 */
public class DTOFactoryUtils {

    /**
     * 注册DTO
     * @param importingClassMetadata
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void registerDTOInstance(AnnotationMetadata importingClassMetadata){
        getInstance().registerDTOInstance(importingClassMetadata);
    }
    /**
     * 从指定的包注册DTO实例
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void registerDTOInstanceFromPackages(Set<String> packages, String file){
        getInstance().registerDTOInstanceFromPackages(packages, file);
    }


    /**
     * 创建DTO接口的CtClass
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception {
        return getInstance().createCtClass(clazz);
    }

    private static IDTOFactory getInstance(){
        try {
            return (IDTOFactory)((Class<?>)B.b.g("DTOFactory")).getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

}