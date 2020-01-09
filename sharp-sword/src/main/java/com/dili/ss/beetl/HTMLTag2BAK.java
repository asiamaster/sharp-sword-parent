package com.dili.ss.beetl;//package com.dili.ss.beetl;
//
//import org.beetl.core.BodyContent;
//import org.beetl.core.ByteWriter;
//import org.beetl.core.Context;
//import org.beetl.core.Template;
//import org.beetl.core.exception.BeetlException;
//import org.beetl.core.io.ByteWriter_Byte;
//import org.beetl.core.io.ByteWriter_Char;
//import org.beetl.core.resource.ClasspathResource;
//import org.beetl.core.statement.Statement;
//import org.beetl.core.tag.HTMLTagSupportWrapper;
//import org.beetl.core.tag.TagFactory;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.File;
//import java.lang.reflect.Field;
//import java.util.*;
//
///**
// * 自定义Html标签，用于嵌套
// * @author asiamastor
// */
//public class HTMLTag2BAK extends HTMLTagSupportWrapper {
//
//    static int RID = 0;
//    private static String VS = "BEETL_VISIT_STATUS";
//    private static int RUN = 0;
//    private static int VISIT = 1;
//    HTMLTag2BAK parent = null;
//    List<HTMLTag2BAK> children = null;
//    LinkedHashMap<String, Integer> binds = null;
//    HttpServletRequest request;
//    // 0 run ,1 known
//    int status = 0;
//    Style style = null;
//    String id;
//
//    /**
//     * 随机为标签产生一个id
//     *
//     * @return
//     */
//    private synchronized static int randomId() {
//        if (RID < 1000000) {
//            return RID++;
//        } else {
//            RID = 0;
//            return RID;
//        }
//    }
//
//    @Override
//    public void render() {
//        if (status == RUN) {
//            //渲染逻辑交给beetl脚本
//            addThis();
//            runTemplateTag();
//            removeThis();
//        } else {
//            TagChildrenContext tnc = (TagChildrenContext) request.getAttribute("tagChildrenContext");
//            tnc.getChildren().add(this);
//            return;
//        }
//    }
//
//    public void binds(Object[] arrays) {
//        if (binds == null) {
//            throw new UnsupportedOperationException("标签体未申明需要绑定变量");
//        }
//        Iterator<Integer> it = binds.values().iterator();
//        int i = 0;
//        while (it.hasNext()) {
//            int index = it.next();
//            this.ctx.vars[index] = arrays[i++];
//        }
//    }
//
//    public String getBody() {
//        try {
//            return super.getBodyContent().toString();
//        } catch (BeetlException ex) {
//            ex.pushResource(this.ctx.getResource());
//            throw ex;
//        }
//    }
//
//    public BodyContent getExecute() {
//        try {
//            ByteWriter writer = ctx.byteWriter;
//            ByteWriter tempWriter = ctx.byteWriter.getTempWriter(writer);
//            ctx.byteWriter = tempWriter;
//            runTemplateTag();
//            ctx.byteWriter = writer;
//            return tempWriter.getTempConent();
//        } catch (BeetlException ex) {
//            ex.pushResource(this.ctx.getResource());
//            throw ex;
//        }
//    }
//
//    public List<HTMLTag2BAK> getChildren() {
//        if (children == null) {
//            request.setAttribute(VS, this.VISIT);
//            TagChildrenContext tnc = new TagChildrenContext();
//            request.setAttribute("tagChildrenContext", tnc);
//            visitChild();
//            request.removeAttribute(VS);
//            children = tnc.getChildren();
//        }
//        return children;
//    }
//
//    public List<String> getMobile() {
//        String str = (String) get("mobile");
//        if (str == null) return Collections.EMPTY_LIST;
//        return Arrays.asList(str.split(" "));
//    }
//
//    /**
//     * 标签名
//     *
//     * @return
//     */
//    public String getTagName() {
//        return (String) this.args[0];
//    }
//
//    public Map getAttrs() {
//        Map map = (Map) this.args[1];
//        return map;
//    }
//
//    public Object get(String attr) {
//        if (this.args.length == 1) {
//            return null;
//        }
//        Map map = (Map) this.args[1];
//        if (map == null) return null;
//        return map.get(attr);
//    }
//
//    @Override
//    public void init(Context ctx, Object[] args, Statement st) {
//        super.init(ctx, args, st);
//        request = (HttpServletRequest) this.ctx.getGlobal("request");
//        Object temp = request.getAttribute(VS);
//        this.status = temp == null ? RUN : (Integer) temp;
//    }
//
//    protected void setBinds(LinkedHashMap<String, Integer> binds) {
//        this.binds = binds;
//    }
//
//    protected void visitChild() {
//        ByteWriter tempWriter = null;
//        if (gt.getConf().isDirectByteOutput()) {
//            tempWriter = new ByteWriter_Byte(new NoLockEmptyByteArrayOutputStream(), gt.getConf().getCharset(), ctx);
//        } else {
//            tempWriter = new ByteWriter_Char(new NoLockEmptyStringWriter(), gt.getConf().getCharset(), ctx);
//        }
//        ByteWriter realWriter = ctx.byteWriter;
//        ctx.byteWriter = tempWriter;
//        bs.execute(ctx);
//        ctx.byteWriter = realWriter;
//    }
//
//    protected void runTemplateTag() {
//        //初始化
//        String child = (String) args[0];
//
//        // 首先查找 已经注册的Tag
//        TagFactory tagFactory = null;
//        String functionTagName = child.replace(':', '.');
//        tagFactory = this.gt.getTagFactory(functionTagName);
//        if (tagFactory == null)
//        {
//            String path = getHtmlTagResourceId(child);
//            Template t = gt.getHtmlFunctionOrTagTemplate(path, this.ctx.getResourceId());
//            t.binding(ctx.globalVar);
//            t.dynamic(ctx.objectKeys);
//            t.binding("tag", this);
//
//            //绑定templatePath变量
//            if(ctx.getResource() instanceof ClasspathResource) {
//                try {
//                    if(ctx.getGlobal("templatePath")== null ){
//                        String resourceRoot = ctx.template.cf.getResourceMap().get("root");
//                        Field pathField = ClasspathResource.class.getDeclaredField("path");
//                        pathField.setAccessible(true);
//                        String templatePath = (String) pathField.get((ClasspathResource) ctx.getResource());
//                        resourceRoot = resourceRoot != null && "/".equals(resourceRoot) ? "" : resourceRoot;
//                        Integer indexOfLength = 11 + resourceRoot.length();
//                        t.binding("templatePath", templatePath.substring(templatePath.indexOf("/templates/" + resourceRoot) + indexOfLength, templatePath.lastIndexOf(".")));
//                    }
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (NoSuchFieldException e) {
//                    e.printStackTrace();
//                }
//            }
//            t.binding("requestUri",ctx.globalVar.get("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping"));
//            if(this.args.length == 2) {
//                Map bodyContent = (Map)this.args[1];
//                Iterator var4 = bodyContent.entrySet().iterator();
//
//                while(var4.hasNext()) {
//                    Map.Entry entry = (Map.Entry)var4.next();
//                    t.binding((String)entry.getKey(), entry.getValue());
//                }
//            }
//            try {
//                t.renderTo(ctx.byteWriter);
//            } catch (BeetlException ex) {
////			ex.pushResource(path);
//                ex.pushToken(ex.token);
//                throw ex;
//            }
//        }
//        else
//        {
//            callTag(tagFactory);
//        }
//    }
//
//    @Override
//    public String toString() {
//        return this.args[0] + ":" + super.toString();
//    }
//
//    public boolean isMobileEnable() {
//        String mobile = (String) this.get("mobile");
//        return mobile != null;
//    }
//
//    @Override
//    protected String getHtmlTagResourceId(String child) {
//        String path = child.replace(':', File.separatorChar);
//        StringBuilder sb = new StringBuilder("/");
//        sb.append(this.tagRoot).append("/").append(path).append(".").append(this.tagSuffix);
//        //html标签支持子文件夹 前台标签以下划线"_"分隔文件夹
//        String key = sb.toString().replaceAll("_", "/");
////        ClasspathResource cr = ((ClasspathResource)gt.getHtmlFunctionOrTagTemplate(key).program.res);
////        try{
////            cr.openReader();
////        }catch (BeetlException ex) {
////        }
////        HttpServletRequest request = (HttpServletRequest)ctx.getGlobal("request");
//        return key;
//    }
//
//
//    protected void addThis() {
//        TagTree tree = (TagTree) request.getAttribute("tagTreeContext");
//        if (tree == null) {
//            tree = new TagTree();
//            request.setAttribute("tagTreeContext", tree);
//        }
//        tree.addTag(this);
//    }
//
//    protected void removeThis() {
//        TagTree tree = (TagTree) request.getAttribute("tagTreeContext");
//        if (tree == null) return;
//        tree.removeTag(this);
//    }
//
//    public HTMLTag2BAK getParent() {
//        TagTree tree = (TagTree) request.getAttribute("tagTreeContext");
//        if (tree == null) return null;
//        return tree.getParenet(this);
//    }
//
//    public Style getStyle() {
//        if (style != null) return style;
//        style = new Style((String) this.get("style"));
//        return style;
//    }
//
//    public String getRid() {
//        if (this.id != null) return id;
//        String id = (String) this.get("id");
//        if (id != null) {
//            this.id = id;
//            return id;
//        } else {
//            this.id = this.getTagName() + "-" + randomId();
//            return this.id;
//        }
//    }
//}
