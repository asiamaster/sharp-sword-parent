package com.dili.ss.java;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asiamaster on 2017/9/30 0030.
 */
public class CompileUtil {
	private final static JavaStringCompiler compiler;
	public final static Map<String, Class<?>> classes = new HashMap<>();
	static {
		compiler = new JavaStringCompiler();
	}

	/**
	 * 编译字节码
	 * @param fileName
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static Map<String, byte[]> compileFile(String fileName, String source) throws IOException {
		return compiler.compile(fileName, source);
	}

	/**
	 * 编译类
	 * @param classContent 字符串类内容
	 * @param classFullname	类全名: com.xxx.service.XxxService
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	public static Class<?> compile(String classContent, String classFullname)  {
//		System.out.println("========================================================");
//		System.out.println("classFullname:"+classFullname);
//		System.out.println("compile:"+classContent);
//		System.out.println("========================================================");
		try {
			String cn = classFullname.substring(classFullname.lastIndexOf(".")+1);
			Map<String, byte[]> results = compileFile(cn+".java", classContent);
			Class<?> clazz = compiler.loadClass(classFullname, results);
			classes.put(clazz.getName(), clazz);
			return clazz;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据字节码编译类
	 * @param results
	 * @param classFullname
	 * @return
	 */
	@SuppressWarnings("all")
	public static Class<?> compile(Map<String, byte[]> results, String classFullname)  {
		try {
			Class<?> clazz = compiler.loadClass(classFullname, results);
			classes.put(clazz.getName(), clazz);
			return clazz;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
