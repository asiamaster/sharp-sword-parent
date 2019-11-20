package com.dili.ss.gid.generator;

/**
 * @Author: WangMi
 * 序列号生成器的抽象基类
 * @Date: 2019/10/8 14:24
 * @Description:
 */
public abstract class GeneratorBase {
    /***** 数据中心ID和节点ID共占10位，共支持部署1024个节点 *****/
    /**
     * 数据中心ID位数
     */
    final static long dcIdBits = 2L;
    /**
     * 工作节点ID位数
     */
    final static long workerIdBits = 10L - dcIdBits;

    /**
     * 数据中心ID
     */
    final static long dcId = 0L;

    static long workerId;

    public static long getWorkerIdBits() {
        return workerIdBits;
    }

    public static void setWorkerId(long workerId) {
        GeneratorBase.workerId = workerId;
    }
}
