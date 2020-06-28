package com.dili.ss.metadata.provider;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by asiamaster on 2017/5/31 0031.
 */
@Component
public class DateProvider implements ValueProvider {

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) {
            return "";
        }
        if(obj instanceof LocalDate){
            //输出yyyy-MM-dd格式字符串
            return obj.toString();
        }
        if(obj instanceof LocalDateTime){
            //输出yyyy-MM-dd HH:mm:ss格式字符串
            return ((LocalDateTime)obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()));
        }
        if(obj instanceof Date){
            return new SimpleDateFormat("yyyy-MM-dd").format((Date)obj);
        }
        Long time = obj instanceof Long ? (Long)obj : obj instanceof String ? Long.parseLong(obj.toString()) : 0;
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
    }
}
