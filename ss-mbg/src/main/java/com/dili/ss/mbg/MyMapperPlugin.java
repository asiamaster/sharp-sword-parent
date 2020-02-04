package com.dili.ss.mbg;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.mbg.beetl.BeetlTemplateUtil;
import com.dili.ss.util.POJOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import tk.mybatis.mapper.generator.MapperCommentGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by asiam on 2017/4/7 0007.
 */
public class MyMapperPlugin extends PluginAdapter {

	private Set<String> mappers = new HashSet<String>();
	private boolean caseSensitive = false;
	//开始的分隔符，例如mysql为`，sqlserver为[
	private String beginningDelimiter = "";
	//结束的分隔符，例如mysql为`，sqlserver为]
	private String endingDelimiter = "";
	//默认不生成DTO，而是生成JAVABean形式的Model
	private Boolean isDTO = false;
	//数据库模式
	private String schema;
	//注释生成器
	private CommentGeneratorConfiguration commentCfg;

	public MyMapperPlugin() {
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		//设置默认的注释生成器
		commentCfg = new CommentGeneratorConfiguration();
		commentCfg.setConfigurationType(MapperCommentGenerator.class.getCanonicalName());
		context.setCommentGeneratorConfiguration(commentCfg);
		//支持oracle获取注释#114
		context.getJdbcConnectionConfiguration().addProperty("remarksReporting", "true");
	}

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		String mappers = this.properties.getProperty("mappers");
		if (StringUtility.stringHasValue(mappers)) {
			for (String mapper : mappers.split(",")) {
				this.mappers.add(mapper);
			}
		} else {
			throw new RuntimeException("Mapper插件缺少必要的mappers属性!");
		}
		String caseSensitive = this.properties.getProperty("caseSensitive");
		if (StringUtility.stringHasValue(caseSensitive)) {
			this.caseSensitive = "TRUE".equalsIgnoreCase(caseSensitive);
		}
		String isDTO = this.properties.getProperty("isDTO", "false");
		if (StringUtility.stringHasValue(isDTO)) {
			this.isDTO = "TRUE".equalsIgnoreCase(isDTO);
		}
		String beginningDelimiter = this.properties.getProperty("beginningDelimiter");
		if (StringUtility.stringHasValue(beginningDelimiter)) {
			this.beginningDelimiter = beginningDelimiter;
		}
		commentCfg.addProperty("beginningDelimiter", this.beginningDelimiter);
		String endingDelimiter = this.properties.getProperty("endingDelimiter");
		if (StringUtility.stringHasValue(endingDelimiter)) {
			this.endingDelimiter = endingDelimiter;
		}
		commentCfg.addProperty("endingDelimiter", this.endingDelimiter);
		String schema = this.properties.getProperty("schema");
		if (StringUtility.stringHasValue(schema)) {
			this.schema = schema;
		}
	}

	public String getDelimiterName(String name) {
		StringBuilder nameBuilder = new StringBuilder();
		if (StringUtility.stringHasValue(schema)) {
			nameBuilder.append(schema);
			nameBuilder.append(".");
		}
		nameBuilder.append(beginningDelimiter);
		nameBuilder.append(name);
		nameBuilder.append(endingDelimiter);
		return nameBuilder.toString();
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 生成的Mapper接口
	 *
	 * @param interfaze
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		//获取实体类
		FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		//import接口
		for (String mapper : mappers) {
			interfaze.addImportedType(new FullyQualifiedJavaType(mapper));
			interfaze.addSuperInterface(new FullyQualifiedJavaType(mapper + "<" + entityType.getShortName() + ">"));
		}
		//import实体类
		interfaze.addImportedType(entityType);
		return true;
	}

	/**
	 * 处理实体类的包和@Table注解
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 */
	private void processEntityClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		//定义SuperClass类型
		FullyQualifiedJavaType superClassType = new FullyQualifiedJavaType("BaseDomain");
		topLevelClass.setSuperClass(superClassType);
		//引入JPA注解
		topLevelClass.addImportedType("javax.persistence.*");
		topLevelClass.addImportedType("com.dili.ss.domain.BaseDomain");

		String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
		//如果包含空格，或者需要分隔符，需要完善
		if (StringUtility.stringContainsSpace(tableName)) {
			tableName = context.getBeginningDelimiter()
					+ tableName
					+ context.getEndingDelimiter();
		}
		//生成实体类注释
		topLevelClass.addJavaDocLine("/**");
		topLevelClass.addJavaDocLine(" * 由MyBatis Generator工具自动生成");
		topLevelClass.addJavaDocLine(" * " + introspectedTable.getRemarks());
		StringBuilder sb = new StringBuilder();
		sb.append(" * This file was generated on ");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		sb.append('.');
		topLevelClass.addJavaDocLine(sb.toString());
		topLevelClass.addJavaDocLine(" */");
		//是否忽略大小写，对于区分大小写的数据库，会有用
		if (caseSensitive && !topLevelClass.getType().getShortName().equals(tableName)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		} else if (!topLevelClass.getType().getShortName().equalsIgnoreCase(tableName)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		} else if (StringUtility.stringHasValue(schema)
				|| StringUtility.stringHasValue(beginningDelimiter)
				|| StringUtility.stringHasValue(endingDelimiter)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		}
		processEntityGetMethodAnnotation(topLevelClass, introspectedTable);
	}

	/**
	 * 生成实体get方法上的FieldDef和EditMode注解
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 */
	private void processEntityGetMethodAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType("com.dili.ss.metadata.annotation.FieldDef");
		topLevelClass.addImportedType("com.dili.ss.metadata.annotation.EditMode");
		topLevelClass.addImportedType("com.dili.ss.metadata.FieldEditor");
		List<Method> methods = topLevelClass.getMethods();

		for (Method method : methods) {
			if (POJOUtils.isGetMethod(method.getName())) {
				IntrospectedColumn introspectedColumn = introspectedTable.getColumn(POJOUtils.humpToLineFast(POJOUtils.getBeanField(method.getName())));
				String fieldLabel = StringUtils.isBlank(introspectedColumn.getRemarks()) ? introspectedColumn.getJavaProperty() : BeetlTemplateUtil.getFieldName(introspectedColumn.getRemarks());
				if ("VARCHAR".equals(introspectedColumn.getJdbcTypeName())) {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\", maxLength = " + introspectedColumn.getLength() + ")");
				} else {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\")");
				}
				JSONObject jsonObject = BeetlTemplateUtil.getJsonObject(introspectedColumn.getRemarks());
//                注释中有JSON参数，则处理成下拉框
				if (jsonObject != null) {
					method.addAnnotation("@EditMode(editor = FieldEditor.Combo, required = " + !introspectedColumn.isNullable() + ", params=\"" + jsonObject.toJSONString().replaceAll("\"", "\\\\\"") + "\")");
				} else {
					if ("TIMESTAMP".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Datetime, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("DATE".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Date, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("INTEGER".equals(introspectedColumn.getJdbcTypeName()) || "BIGINT".equals(introspectedColumn.getJdbcTypeName()) || "BIT".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Number, required = " + !introspectedColumn.isNullable() + ")");
					} else {
						method.addAnnotation("@EditMode(editor = FieldEditor.Text, required = " + !introspectedColumn.isNullable() + ")");
					}
				}
			}
		}
	}

	/**
	 * 生成dto形式的实体
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(
			IntrospectedTable introspectedTable) {
		if(!isDTO) {
			return null;
		}
		String targetPackage = this.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String targetProject = this.getContext().getJavaModelGeneratorConfiguration().getTargetProject();
		ShellCallback shellCallback = new DefaultShellCallback(false);
		JavaFormatter javaFormatter = context.getJavaFormatter();
		List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
		String shortName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		GeneratedJavaFile dtoJavafile = null;
		String dtoSuperInterface = "com.dili.ss.dto.IBaseDomain";
		if (StringUtility.stringHasValue(targetPackage)) {
			Interface dtoInterface = new Interface(targetPackage + "." + shortName);
			dtoInterface.setVisibility(JavaVisibility.PUBLIC);
			//===============================  添加dto的父接口  ===============================
			FullyQualifiedJavaType dtoSuperType = new FullyQualifiedJavaType(dtoSuperInterface);
			dtoInterface.addImportedType(dtoSuperType);
			dtoInterface.addSuperInterface(dtoSuperType);
			//===============================  引入JPA注解  ===============================
			dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.*"));

			//===============================  生成实体类注释  ===============================
			dtoInterface.addJavaDocLine("/**");
			dtoInterface.addJavaDocLine(" * 由MyBatis Generator工具自动生成");
			dtoInterface.addJavaDocLine(" * " + introspectedTable.getRemarks());
			StringBuilder sb = new StringBuilder();
			sb.append(" * This file was generated on ");
			sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			sb.append('.');
			dtoInterface.addJavaDocLine(sb.toString());
			dtoInterface.addJavaDocLine(" */");

			//===============================  添加@Table注解  ===============================
			String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
			//如果包含空格，或者需要分隔符，需要完善
			if (StringUtility.stringContainsSpace(tableName)) {
				tableName = context.getBeginningDelimiter()
						+ tableName
						+ context.getEndingDelimiter();
			}
			//是否忽略大小写，对于区分大小写的数据库，会有用
			if (caseSensitive && !dtoInterface.getType().getShortName().equals(tableName)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			} else if (!dtoInterface.getType().getShortName().equalsIgnoreCase(tableName)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			} else if (StringUtility.stringHasValue(schema)
					|| StringUtility.stringHasValue(beginningDelimiter)
					|| StringUtility.stringHasValue(endingDelimiter)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			}

			generateMethods(dtoInterface, introspectedTable);


			//最终生成.java文件
			dtoJavafile = new GeneratedJavaFile(dtoInterface, targetProject, javaFormatter);
//			try {
//				File mapperDir = shellCallback.getDirectory(targetProject, targetPackage);
//				File mapperFile = new File(mapperDir, dtoJavafile.getFileName());
//				// 文件不存在
//				if (!mapperFile.exists()) {
					mapperJavaFiles.add(dtoJavafile);
//				}
//			} catch (ShellException e) {
//				e.printStackTrace();
//			}
		}
		return mapperJavaFiles;
	}

	/**
	 * 生成getter和setter
	 * @param dtoInterface
	 * @param introspectedTable
	 */
	private void generateMethods(Interface dtoInterface, IntrospectedTable introspectedTable){
		//java type缓存，用于导包
		List<FullyQualifiedJavaType> buffer = new ArrayList<>();
		for(IntrospectedColumn column : introspectedTable.getAllColumns()){
			FullyQualifiedJavaType fullyQualifiedJavaType = column.getFullyQualifiedJavaType();
			//判断如果需要明确导包(非java.lang包)，并且没有缓存的
			if(fullyQualifiedJavaType.isExplicitlyImported() && !buffer.contains(fullyQualifiedJavaType)){
				buffer.add(fullyQualifiedJavaType);
			}
			String property = column.getJavaProperty();
			String upperFirstProperty = Character.toUpperCase(property.charAt(0)) + property.substring(1);
			Method getMethod = new Method("get"+upperFirstProperty);
			getMethod.setReturnType(fullyQualifiedJavaType);
			dtoInterface.addMethod(getMethod);

			Method setMethod = new Method("set"+upperFirstProperty);
			Parameter setParameter = new Parameter(fullyQualifiedJavaType, StringUtils.uncapitalize(property));
			setMethod.addParameter(0, setParameter);
			dtoInterface.addMethod(setMethod);
		}
		//根据字段类型统一导包
		for(FullyQualifiedJavaType fullyQualifiedJavaType : buffer) {
			dtoInterface.addImportedType(fullyQualifiedJavaType);
		}
		//生成DTO get方法上的@Id, @GeneratedValue,@Column,FieldDef和EditMode注解
		processDTOGetMethodAnnotation(dtoInterface, introspectedTable);
	}

	/**
	 * 生成DTO get方法上的注解
	 *
	 * @param dtoInterface
	 * @param introspectedTable
	 */
	private void processDTOGetMethodAnnotation(Interface dtoInterface, IntrospectedTable introspectedTable) {
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.Column"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.GenerationType"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.dili.ss.metadata.FieldEditor"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.dili.ss.metadata.annotation.FieldDef"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.dili.ss.metadata.annotation.EditMode"));

		List<Method> methods = dtoInterface.getMethods();
		for (Method method : methods) {
			if (POJOUtils.isGetMethod(method.getName())) {
				IntrospectedColumn introspectedColumn = introspectedTable.getColumn(POJOUtils.humpToLineFast(POJOUtils.getBeanField(method.getName())));
				if(introspectedColumn == null){
					continue;
				}
				String fieldLabel = StringUtils.isBlank(introspectedColumn.getRemarks()) ? introspectedColumn.getJavaProperty() : BeetlTemplateUtil.getFieldName(introspectedColumn.getRemarks());
				//设置@Id和@GeneratedValue
				if(introspectedColumn.isIdentity()) {
					method.addAnnotation("@Id");
					method.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY)");
				}
				//设置@Column
				method.addAnnotation("@Column(name = \""+beginningDelimiter+introspectedColumn.getActualColumnName()+beginningDelimiter+"\")");
				//设置@FieldDef
				if ("VARCHAR".equals(introspectedColumn.getJdbcTypeName())) {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\", maxLength = " + introspectedColumn.getLength() + ")");
				} else {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\")");
				}
				//设置@EditMode
				JSONObject jsonObject = BeetlTemplateUtil.getJsonObject(introspectedColumn.getRemarks());
//                注释中有JSON参数，则处理成下拉框
				if (jsonObject != null) {
					method.addAnnotation("@EditMode(editor = FieldEditor.Combo, required = " + !introspectedColumn.isNullable() + ", params=\"" + jsonObject.toJSONString().replaceAll("\"", "\\\\\"") + "\")");
				} else {
					if ("TIMESTAMP".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Datetime, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("DATE".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Date, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("INTEGER".equals(introspectedColumn.getJdbcTypeName()) || "BIGINT".equals(introspectedColumn.getJdbcTypeName()) || "BIT".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Number, required = " + !introspectedColumn.isNullable() + ")");
					} else {
						method.addAnnotation("@EditMode(editor = FieldEditor.Text, required = " + !introspectedColumn.isNullable() + ")");
					}
				}
			}
		}
	}

	/**
	 * 生成基础实体类
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		//DTO在contextGenerateAdditionalJavaFiles中生成
		if (isDTO) {
			String dtoSuperInterface = "com.dili.ss.dto.IBaseDomain";
			topLevelClass.setVisibility(JavaVisibility.PUBLIC);
			//===============================  添加dto的父接口，在其它插件中用于判断是dto还是JavaBean ===============================
			FullyQualifiedJavaType dtoSuperType = new FullyQualifiedJavaType(dtoSuperInterface);
//			topLevelClass.addImportedType(dtoSuperType);
			topLevelClass.addSuperInterface(dtoSuperType);
			//这里如果return false，在service和controller生成插件中的introspectedTable.getGeneratedJavaFiles()就取不到java模型
			//return true又会生成一个空的普通java bean，所以在生成dto时，只能先生成再覆盖
			return true;
		}
		processEntityClass(topLevelClass, introspectedTable);
		return true;
	}

	/**
	 * 生成实体类注解KEY对象
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//		processEntityClass(topLevelClass, introspectedTable);
		return true;
	}

	/**
	 * 生成带BLOB字段的对象
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//		processEntityClass(topLevelClass, introspectedTable);
		return false;
	}

	//下面所有return false的方法都不生成。这些都是基础的CRUD方法，使用通用Mapper实现
	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		updateAttributeId(element, "selectBy");
		return true;
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		updateAttributeId(element, "updateBy");
		return true;
	}

	//修改id属性的值为updatedId
	private void updateAttributeId(XmlElement element, String updatedId) {
		List<Attribute> attrs = element.getAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			Attribute attr = attrs.get(i);
			if ("id".equals(attr.getName())) {
				attrs.remove(i);
				break;
			}
		}
		element.addAttribute(new Attribute("id", updatedId));
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return true;
	}

	@Override
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	// ====================================  mapper生成 =======================================
	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	// ===============================  model生成  ======================================

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean modelFieldGenerated(Field field,
	                                   TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                   IntrospectedTable introspectedTable,
	                                   ModelClassType modelClassType) {
		return isDTO ? false : true;
	}

	@Override
	public boolean modelGetterMethodGenerated(Method method,
	                                          TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                          IntrospectedTable introspectedTable,
	                                          ModelClassType modelClassType) {
		return isDTO ? false : true;
	}

	@Override
	public boolean modelSetterMethodGenerated(Method method,
	                                          TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                          IntrospectedTable introspectedTable,
	                                          ModelClassType modelClassType) {
		return isDTO ? false : true;
	}
}
