package com.dili.ss.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class BU {

	public static Object inv(Class<?> c, String m){
		try {
			return c.getMethod(m).invoke(null);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}
	}

	public static void i(){
	}

	public static BI n(){
		return (BI)Proxy.newProxyInstance(BI.class.getClassLoader(), new Class<?>[] { BI.class }, new BH());
	}
}