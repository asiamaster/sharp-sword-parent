package com.dili.ss.beetl;

import com.dili.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.BodyContent;
import org.beetl.core.tag.Tag;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 为前端js提供系统配置属性获取的标签
 * Created by asiamaster on 2017/7/21 0021.
 */
@Component("config")
public class SystemConfigTag extends Tag {

	//属性名
	private final String NAME_FIELD = "name";
	//默认值名
	private final String DEFAULT_VALUE_FIELD = "defValue";
	//判断属性名是否存在,如果不存在就不渲染body
	private final String HAS_FIELD = "has";

	@Override
	public void render() {
		try {
			Map<String, Object> argsMap = (Map) this.args[1];
			String has = (String) argsMap.get(HAS_FIELD);
			//has属性优化，找到has的属性名则渲染标签体
			if(StringUtils.isNotBlank(has)){
				BodyContent content = getBodyContent(); // 标签体内容，暂存
				if(StringUtils.isNotBlank(SpringUtil.getProperty(has))){
					ctx.byteWriter.writeString(content.getBody());
				}
			}else{//没找到has属性名则直接取name属性的值
				String name = (String) argsMap.get(NAME_FIELD);
				if(null == name) {
					return;
				}
				String defaultValue = (String) argsMap.get(DEFAULT_VALUE_FIELD);
				String value = defaultValue == null ? SpringUtil.getProperty(name) : SpringUtil.getProperty(name, defaultValue);
				ctx.byteWriter.writeString(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
