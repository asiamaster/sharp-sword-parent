package com.dili.ss.beetl;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.metadata.FieldEditor;
import org.beetl.core.tag.Tag;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 为前端js提供FieldEditor的值对json, key为FieldEditor.name()，value为对应界面easyui控件的名称
 * Created by asiamaster on 2017/7/21 0021.
 */
@Component("fieldEditor")
public class FieldEditorTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";

	//标签自定义属性
	private final String VAR_NAME_FIELD = "varName";

	@Override
	public void render() {
		try {
//			BodyContent content = getBodyContent(); // 标签体内容，暂存
			Map<String, Object> argsMap = (Map) this.args[1];
			String varName = (String) argsMap.get(VAR_NAME_FIELD);
			varName = varName == null ? "fieldEditor" : varName;
			JSONObject jsonObject = new JSONObject();
			for(FieldEditor fieldEditor : FieldEditor.values()){
				jsonObject.put(fieldEditor.name(), fieldEditor.getEditor());
			}
			ctx.byteWriter.writeString("var " + varName + "=" + jsonObject.toJSONString() + ";" + LINE_SEPARATOR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
