package com.dili.ss.glossary;

/**
 * Bean类型
 * Created by asiamaster on 2020/7/27.
 */
public enum BeanType {
	MAP(1,"Map"),
	DTO(2,"DTO接口"),
	DTO_INSTANCE(3,"DTOInstance"),
	JAVA_BEAN(4,"JavaBean");

	private int value;
	private String text;

	BeanType(int value, String text){
		this.value = value;
		this.text = text;
	}

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
