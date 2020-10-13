package com.dili.ss.uid.handler;

public interface BizNumberManager {

    /**
     * 清除缓存
     * @param type
     */
    void clear(String type);

    /**
     * setter
     * @param bizNumberComponent
     */
    void setBizNumberComponent(BizNumberComponent bizNumberComponent);

    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param step
     * @param increment
     * @return
     */
    Long getBizNumberByType(String type, String dateFormat, int length, long step, int increment);

    /**
     * 根据业务类型获取增量为1的业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param step
     * @return
     */
    Long getBizNumberByType(String type, String dateFormat, int length, long step);

}
