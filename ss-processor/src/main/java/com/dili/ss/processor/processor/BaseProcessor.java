//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？
package com.dili.ss.processor.processor;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 处理器基类
 * 提供打印功能
 */
public abstract class BaseProcessor extends AbstractProcessor {

    protected Messager messager;
//    protected JavacTrees trees;
    protected TreeMaker treeMaker;
    protected Names names;
    protected Filer filer;
    // 元素操作的辅助类
    protected Elements elementUtils;
    //读取conf/processor.properties的配置信息
    Properties properties = new Properties();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
//        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = BaseProcessor.class.getClassLoader().getResourceAsStream("conf/processor.properties");
        if(null == in){
            return;
        }
        // 使用properties对象加载输入流
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }

//    /**
//     * 获取元素的行号
//     * @param element
//     * @return
//     */
//    protected int getLine(Element element){
//        Object javacSourcePosition = ((JavacElements) elementUtils).getSourcePosition(element);
//        try {
//            Method getLineMethod = javacSourcePosition.getClass().getMethod("getLine");
//            getLineMethod.setAccessible(true);
//            return (int)getLineMethod.invoke(javacSourcePosition);
//        } catch (Exception e) {
//            return 0;
//        }
//    }

    protected void error(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, e, String.format(msg, args));
    }

    protected void warning(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, e, String.format(msg, args));
    }

    protected void info(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.OTHER, e, String.format(msg, args));
    }

    protected void printMessage(Diagnostic.Kind kind, Element e, String msg, Object... args) {
        messager.printMessage(kind, String.format(msg, args), e);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}