package com.dili.ss.uid.handler;

import com.dili.ss.uid.domain.SequenceNo;
import com.dili.ss.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BizNumberManagerImpl implements BizNumberManager{

    /**
     * 固定步长值，默认为50
     */
    private int fixedStep;

    /**
     * 范围步长值，默认为最大范围的20倍
     */
    private int rangeStep;

    private BizNumberComponent bizNumberComponent;

    protected ConcurrentHashMap<String, SequenceNo> bizNumberMap = new ConcurrentHashMap<>();

    //获取失败后的重试次数
    protected static final int RETRY = 3;

    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param range
     * @return
     */
    @Override
    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param range
     * @return
     */
    public Long getBizNumberByType(String type, String dateFormat, int length, String range) {
        String dateStr = DateUtils.format(dateFormat);
        Long orderId = getNextSequenceId(type, null, dateFormat, length, range);
        //如果不是同天，重新获取从1开始的编号
        if (StringUtils.isNotBlank(dateStr) && !dateStr.equals(StringUtils.substring(String.valueOf(orderId), 0, 8))) {
            orderId = getNextSequenceId(type, getInitBizNumber(dateStr, length), dateFormat, length, range);
        }
        return orderId;
    }

    /**
     * 根据日期格式和长度，获取下一个编号, 失败后重试五次
     * @param type  编码类型
     * @param startSeq  从指定SEQ开始， 一般为空或从当天第1号开始
     * @param dateFormat    日期格式(可以为空)
     * @param length    编码长度
     * @param range 增长范围
     * @return
     */
    private Long getNextSequenceId(String type, Long startSeq, String dateFormat, int length, String range) {
        Long seqId = getNextSeqId(type, startSeq, dateFormat, length, range);
        for (int i = 0; (seqId < 0 && i < RETRY); i++) {// 失败后，最大重复3次获取
            bizNumberMap.remove(type);
            seqId = getNextSeqId(type, startSeq, dateFormat, length, range);
        }
        return seqId;
    }

    /**
     * 根据日期格式和长度，获取下一个编号
     * @param type
     * @param startSeq
     * @param dateFormat
     * @param length
     * @param range
     * @return
     */
    private Long getNextSeqId(String type, Long startSeq, String dateFormat, int length, String range) {
        SequenceNo idSequence = bizNumberMap.get(type);
        String[] ranges = range.split(",");
        if (idSequence == null) {
            idSequence = new SequenceNo();
            //范围步长值取最大自增值的rangeStep倍
            if (ranges.length == 2){
                idSequence.setStep(Long.parseLong(ranges[1]) * rangeStep);
            }else{
                //固定步长值为固定值的fixedStep倍
                idSequence.setStep(Long.parseLong(ranges[0]) * fixedStep);
            }
            bizNumberMap.putIfAbsent(type, idSequence);
            idSequence = bizNumberMap.get(type);
        }
        if (startSeq != null
                || idSequence.getStartSeq() >= idSequence.getFinishSeq()) {
            idSequence = bizNumberComponent.getSeqNoByNewTransactional(idSequence,
                    type, startSeq, dateFormat, length);
            if(idSequence == null){
                return -1L;
            }
        }

        int increment = ranges.length == 1 ? Integer.parseInt(ranges[0]) : rangeRandom(Integer.parseInt(ranges[0]), Integer.parseInt(ranges[1]));
        return increment == 1 ? idSequence.next() : idSequence.next(increment);
    }

    /**
     * 获取范围随机数
     * random.nextInt(max)表示生成[0,max]之间的随机数，然后对(max-min+1)取模。
     * 以生成[10,20]随机数为例，首先生成0-20的随机数，然后对(20-10+1)取模得到[0-10]之间的随机数，然后加上min=10，最后生成的是10-20的随机数
     * @param min
     * @param max
     * @return
     */
    private int rangeRandom(int min, int max){
        return new Random().nextInt(max)%(max-min+1) + min;
    }

    /**
     * 获取日期加每日计数量的初始化字符串，最低位从1开始
     * @param dateStr
     * @param length 编码位数(不包含日期位数)
     * @return
     */
    private Long getInitBizNumber(String dateStr, int length) {
        return StringUtils.isBlank(dateStr) ? 1 : NumberUtils.toLong(dateStr) * new Double(Math.pow(10, length)).longValue() + 1;
    }

    @Override
    public void setFixedStep(int fixedStep){
        this.fixedStep = fixedStep;
    }

    @Override
    public void setRangeStep(int rangeStep){
        this.rangeStep = rangeStep;
    }

    @Override
    public void setBizNumberComponent(BizNumberComponent bizNumberComponent) {
        this.bizNumberComponent = bizNumberComponent;
    }
}
