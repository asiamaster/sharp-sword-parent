package com.dili.ss.mvc.boot;

import com.dili.ss.mvc.annotation.Cent2Yuan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 分转元注解(Cent2Yuan)格式化工厂
 */
public class Cent2YuanAnnotationFormatterFactory implements AnnotationFormatterFactory<Cent2Yuan> {
    @Override
    public Set<Class<?>> getFieldTypes() {
        Set<Class<?>> hashSet = new HashSet<>();
        hashSet.add(Long.class);
        return hashSet;
    }

    @Override
    public Printer<?> getPrinter(Cent2Yuan cent2Yuan, Class<?> fieldType) {
        return getFormatter(cent2Yuan);
    }

    @Override
    public Parser<?> getParser(Cent2Yuan cent2Yuan, Class<?> fieldType) {
        return getFormatter(cent2Yuan);
    }

    private Cent2YuanFormatter getFormatter(Cent2Yuan cent2Yuan) {
        Cent2YuanFormatter formatter = new Cent2YuanFormatter();
        formatter.setDefPrintVal(cent2Yuan.defPrintVal());
        return formatter;
    }

    /**
     * 分转元Formatter
     */
    private class Cent2YuanFormatter implements Formatter<Long>, Serializable {
        private static final long serialVersionUID = -818656464607971661L;

        private String defPrintVal;

        public String getDefPrintVal() {
            return defPrintVal;
        }

        public void setDefPrintVal(String defPrintVal) {
            this.defPrintVal = defPrintVal;
        }

        @Override
        public String print(Long value, Locale locale) {
            if(value == null) {
                return getDefPrintVal();
            }
            return new BigDecimal(value).divide(new BigDecimal(100)).toString();
        }


        @Override
        public Long parse(String value, Locale locale)  {
            if(StringUtils.isBlank(value)){
                return null;
            }
            return new BigDecimal(value).multiply(new BigDecimal(100)).longValue();
        }

    }

}
