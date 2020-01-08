package com.dili.ss.beetl;

import org.beetl.core.Context;
import org.beetl.core.statement.Statement;
import org.beetl.core.tag.HTMLTagVarBindingWrapper;
import org.beetl.core.tag.Tag;

import java.util.LinkedHashMap;

/**
 * 封装了带变量绑定的html标签调用的标签
 * @author asiamastor
 */
public class HTMLVarBindingTag extends HTMLTagVarBindingWrapper
{

	Tag tag = new HTMLTag();

	@Override
	public void render()
	{
		tag.render();

	}

//	@Override
//	public Object[] bindVars(){
//		return null;
//	}

	@Override
	public void mapName2Index(LinkedHashMap<String, Integer> map)
	{
		((HTMLTag)tag).setBinds(map);
	}

	@Override
	public void init(Context ctx, Object[] args, Statement st)
	{
		tag.init(ctx, args, st);
	}

}
