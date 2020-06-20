package com.dili.http.okhttp.java;

import com.dili.ss.util.SpringUtil;
import com.dili.ss.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ResourceUtils;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * In-memory compile Java source code as String.
 * 
 * @author asiamaster
 */
public class JavaStringCompiler {
	protected static final Logger logger = LoggerFactory.getLogger(JavaStringCompiler.class);
	JavaCompiler compiler;
	StandardJavaFileManager stdManager;
	//判断是否运行过，已经运行过则不进行解压
	private static boolean isRun = false;

	public JavaStringCompiler() {
		this.compiler = ToolProvider.getSystemJavaCompiler();
		this.stdManager = compiler.getStandardFileManager(null, null, null);
	}

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
	@SuppressWarnings("all")
	public Map<String, byte[]> compile(String fileName, String source) throws IOException {
		try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
			JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
            boolean isJar = isJarRun();
            if( !isRun && isJar ){
                String unzipDirPath = new ApplicationHome(getClass()).getDir().getAbsolutePath();
                String jarFile = new ApplicationHome(getClass()).getSource().getAbsolutePath();
                ZipUtils.unzip(new File(unzipDirPath), new File(jarFile));
                isRun = true;
            }
            if(!isRun){
				B.i();
			}
			String classpath = isJar ? buildClassPath("/BOOT-INF/lib/") : null;
            Iterable options = isJar ? Arrays.asList("-classpath", classpath) : null;
			CompilationTask task = compiler.getTask(null, manager, null, options, null, Arrays.asList(javaFileObject));
			Boolean result = task.call();
			if (result == null || !result.booleanValue()) {
				throw new RuntimeException("Compilation failed.");
			}
            //生成对象
			return manager.getClassBytes();
		}
    }


    public static boolean isJarRun() {
//        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
//        CodeSource codeSource = protectionDomain.getCodeSource();
//        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
//        String path = (location == null ? null : location.getSchemeSpecificPart());
//        File root = new File(path);
//        return root.isDirectory() ? false : true;
		try {
			String classpath = ResourceUtils.getURL("classpath:").getPath();
			return classpath.contains("\\BOOT-INF\\classes!") || classpath.contains("!/BOOT-INF/classes!") ? true : false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//取不到路径默认为包模式
			return true;
		}
	}

	private static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
	}

    private static String buildClassPath(String libRelativePath){
		try {
			String path = ResourceUtils.getURL("classpath:").getPath();
			if(path.startsWith("file:/")){
				path = path.substring(6, path.length());
			}
			if(isLinux() && !path.startsWith("/")){
				path = "/"+path;
			}
			int index = path.lastIndexOf("!/BOOT-INF/classes");
			if(index < 0){
				return null;
			}
			File libDir = new File(path.substring(0, index));
			libDir = new File(libDir.getParentFile().getPath() + libRelativePath);
			File[] jars = libDir.listFiles();
			StringBuilder classpath = new StringBuilder();
			libRelativePath = "."+libRelativePath;
			String separator = isLinux() ? ":" : ";";
			for (File jar : jars) {
				classpath.append(libRelativePath).append(jar.getName()).append(separator);
			}
			return classpath.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Load class from compiled classes.
	 * 
	 * @param name
	 *            Full class name.
	 * @param classBytes
	 *            Compiled results as a Map.
	 * @return The Class instance.
	 * @throws ClassNotFoundException
	 *             If class not found.
	 * @throws IOException
	 *             If load error.
	 */
	public Class<?> loadClass(String name, Map<String, byte[]> classBytes) throws ClassNotFoundException, IOException {
	    Object obj = SpringUtil.getBean("valueProviderUtils");
	    if(obj == null){
            try (MemoryClassLoader classLoader = new MemoryClassLoader(classBytes)) {
                return classLoader.loadClass(name);
            }
        }else {
            try (MemoryClassLoader classLoader = new MemoryClassLoader(classBytes, obj.getClass().getClassLoader())) {
                return classLoader.loadClass(name);
            }
        }
	}
}
