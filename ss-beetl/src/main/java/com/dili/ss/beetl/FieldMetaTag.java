package com.dili.ss.beetl;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.MetadataUtils;
import com.dili.ss.metadata.ObjectMeta;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.tag.Tag;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

/**
 * fieldMeta数据标签<br/>
 * 根据dtoClass获取fieldMeta信息,并转换为key为字段名，value为FieldMeta的JSON<br/><br/>
 * js的对象名取varName变量，默认值为clazz.getSimpleName()首字母小写<br/>
 * 例: var device = {id:{label:"主键", required:true}, name:{label:"名称", required:false}...};
 * Created by asiamaster on 2017/7/11 0011.
 */
@Component("fieldMeta")
public class FieldMetaTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";

	//标签自定义属性
	private final String DTO_CLASS_FIELD = "dtoClass";
	private final String VAR_NAME_FIELD = "varName";

	@Override
	public void render() {
		try {
//			BodyContent content = getBodyContent(); // 标签体内容，暂存
			Map<String, Object> argsMap = (Map)this.args[1];
			String dtoClass = (String) argsMap.get(DTO_CLASS_FIELD);
			String varName = (String) argsMap.get(VAR_NAME_FIELD);
//			dtoClass参数必填
			if(StringUtils.isBlank(dtoClass)) {
				return;
			}
			Class clazz = Class.forName(dtoClass);
			varName = varName == null ? getVarName(clazz.getSimpleName()) : varName;
			ObjectMeta objectMeta = MetadataUtils.getDTOMeta(clazz);
			JSONObject jsonObject = new JSONObject();
			for(FieldMeta fieldMeta : objectMeta){
				jsonObject.put(fieldMeta.getColumn(), JSONObject.toJSON(fieldMeta));
			}
			ctx.byteWriter.writeString("var " + varName + " = " + jsonObject.toJSONString()+";"+LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据类全名获取js变量名称
	 * @param clazzFullName
	 * @return
	 */
	public static String getVarName(String clazzFullName){
		Assert.hasText(clazzFullName, "clazzFullName不能为空");
		return lowerCaseFirstChar(clazzFullName.substring(clazzFullName.lastIndexOf(".")+1))+"Meta";
	}

	/**
	 * 第一个字符小写
	 *
	 * @param value
	 * @return
	 */
	private static String lowerCaseFirstChar(String value){
		return String.valueOf(value.charAt(0)).toLowerCase() + value.substring(1);
	}


}
