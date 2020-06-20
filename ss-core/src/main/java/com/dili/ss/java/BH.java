package com.dili.ss.java;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class BH implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -890127308975L;

    public BH() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        assert (args != null);
        assert (args.length > 0);
        if("gif".equals(method.getName())){
            return gif(args[0], args[1]);
        }else if("daeif".equals(method.getName())){
            if(args[1] == null){
                List<String> l = (List)gif(args[0], null);
                l.stream().forEach(s -> {
                    if(StringUtils.isBlank(s)){
                        return;
                    }
                    try {
                        if(s.contains("^")){
                            String cd = s.substring(0, s.indexOf("^"));
                            String[] cds = cd.split("=");
                            if(cds.length < 2){
                                B.b.dae(s.substring(s.indexOf("^")+1, s.length()));
                            }else if(cds[1].equals(((Environment)args[2]).getProperty(cds[0], "false"))) {
                                B.b.dae(s.substring(s.indexOf("^")+1, s.length()));
                            }
                        }else{
                            B.b.dae(s);
                        }
                    } catch (Exception e) {
                    }
                });
            }
        }
        return null;
    }

    private Object gif(Object args0, Object args1){
        List<String> l = gfl(args0.toString());
        if(args1 == null){
            return l;
        }
        for(String s : l){
            if(s.contains("^")) {
                String n = s.substring(0, s.indexOf("^"));
                if(n.equalsIgnoreCase(args1.toString())) {
                    return s.substring(s.indexOf("^")+1, s.length());
                }
            }
        }
        return null;
    }

    private String gfs(String fn){
        try {
            InputStream is = (InputStream) B.class.getClassLoader().getResource(fn).getContent();
            byte[] buffer = new byte[is.available()];
            int tmp = is.read(buffer);
            while (tmp != -1) {
                tmp = is.read(buffer);
            }
            return new String(buffer);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> gfl(String fn){
        BufferedReader br = null;
        InputStream is = null;
        try {
            Enumeration<URL> enumeration = B.class.getClassLoader().getResources(fn);
            List<String> list = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                is = (InputStream)enumeration.nextElement().getContent();
                br = new BufferedReader(new InputStreamReader(is));
                //文件内容格式:spring.redis.pool.max-active=8
                String s;
                while ((s = br.readLine()) != null) {
                    list.add(s);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                if(is != null) {is.close();}
                if(br != null) { br.close();}
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}