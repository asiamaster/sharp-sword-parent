package com.dili.ss.java;

import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class B {

    public static final BSUI b;
    public static final BI bi;

    static {
        bi = BU.n();
        b = b();
    }

    public static void i(){
    }

    public static void daeif(String s, String k, Environment e){
        bi.daeif(s, k, e);
    }

    private static final BSUI b() {
        if(b != null){
            return b;
        }
        try {
            Map<String, byte[]> map2 = new HashMap<>(1);
            map2.put(BSUI.class.getPackage().getName()+".BSU", gfs("script/water").getBytes("ISO-8859-1"));
            Class<?> clazz= CompileUtil.compile(map2, BSUI.class.getPackage().getName()+".BSU");
            return (com.dili.ss.java.BSUI)clazz.getMethod("me").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static final String gfs(String fn){
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

    private static String d(String src) {
        String temp = "";
        for (int i = 0; i < src.length() / 2; i++) {
            temp = temp + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return temp;
    }

}