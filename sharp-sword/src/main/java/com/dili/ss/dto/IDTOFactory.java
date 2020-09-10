package com.dili.ss.dto;

import javassist.ClassPath;
import javassist.CtClass;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

public interface IDTOFactory {

    /**
     * 注册DTO
     * @param importingClassMetadata
     */
    void registerDTOInstance(AnnotationMetadata importingClassMetadata);

    /**
     * 从指定的包注册DTO实例
     */
    void registerDTOInstanceFromPackages(Set<String> packages, String file);

    /**
     * 创建DTO接口的CtClass
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception;

    /**
     * 插入classPath
     * @param classPath
     */
    void insertClassPath(ClassPath classPath);

    /**
     * 导入默认包
     * @param packageName
     */
    void importPackage(String packageName);
}
