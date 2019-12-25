package com.dili.http.okhttp.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 编译String为class文件到指定目录
 * Standard compile Java source code as String.
 * 
 * @author asiamaster
 */
public class StandardJavaStringCompiler {
	protected static final Logger logger = LoggerFactory.getLogger(StandardJavaStringCompiler.class);

	/**
	 * Compile a Java source file in memory.
	 * 
	 * @param fileName
	 *            Java file name, e.g. "Test.java"
	 * @param source
	 *            The source code as String.
	 * @return The compiled results as Map that contains class name as key,
	 *         class binary as value.
	 * @throws IOException
	 *             If compile error.
	 */
	public static Class<?> compile(String fileName, String source) throws IOException, ClassNotFoundException {
		//当前编译器
		JavaCompiler cmp = ToolProvider.getSystemJavaCompiler();
		//Java标准文件管理器
		StandardJavaFileManager fm = cmp.getStandardFileManager(null, null, null);
		//Java文件对象
		JavaFileObject jfo = new StringJavaObject(fileName, source);
		//编译参数，类似于javac <options>中的options
		List<String> optionsList = new ArrayList<String>();
		//编译文件的存放地方，注意：此处是为Idea工具特设的
		optionsList.addAll(Arrays.asList("-d", "./target/classes"));
		//要编译的单元
		List<JavaFileObject> jfos = Arrays.asList(jfo);
		//设置编译环境
		JavaCompiler.CompilationTask task = cmp.getTask(null, fm, null, optionsList, null, jfos);
		//编译成功
		Boolean result = task.call();
		if (result == null || !result.booleanValue()) {
			throw new RuntimeException("Compilation failed.");
		}
		return Class.forName(fileName);
	}

//	public static void main(String[] args) throws IOException, ClassNotFoundException {
//		String sourceStr = "public class Hello{    public String sayHello (String name) {return \"Hello,\" + name + \"!\";}}";
//		Class<?> clazz = StandardJavaStringCompiler.compile("Hello", sourceStr);
//		System.out.println(clazz);
//	}


}
