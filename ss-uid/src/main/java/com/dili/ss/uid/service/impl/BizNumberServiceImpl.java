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
package com.dili.ss.uid.service.impl;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.exception.AppException;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.domain.SequenceNo;
import com.dili.ss.uid.handler.BizNumberComponent;
import com.dili.ss.uid.mapper.BizNumberMapper;
import com.dili.ss.uid.service.BizNumberService;
import com.dili.ss.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 业务号生成服务
 *
 * @author asiamaster
 */
@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberServiceImpl extends BaseServiceImpl<BizNumber, Long> implements BizNumberService {

    public BizNumberMapper getActualDao() {
        return (BizNumberMapper)getDao();
    }

    public static ReentrantLock rangeIdLock = new ReentrantLock(false);
    @Autowired
    private BizNumberComponent bizNumberComponent;

    protected ConcurrentHashMap<String, SequenceNo> bizNumberMap = new ConcurrentHashMap<>();

    //获取失败后的重试次数
    protected static final int RETRY = 10;

    @Override
    public void clear(String type){
        if(bizNumberMap.containsKey(type)) {
            bizNumberMap.remove(type);
        }
    }

    @Override
    public BizNumber selectOne(BizNumber bizNumber){
        return getActualDao().selectOne(bizNumber);
    }

    @Override
    public String getBizNumberByRule(BizNumberRule bizNumberRule) {
        if(bizNumberRule == null){
            return null;
        }
        Long bizNumber = getBizNumber(bizNumberRule);
        String bizNumberStr = StringUtils.isBlank(bizNumberRule.getDateFormat()) ? String.format("%0" + bizNumberRule.getLength() + "d", bizNumber) : bizNumber.toString();
        String prefix = bizNumberRule.getPrefix();
        return prefix == null ? bizNumberStr : prefix + bizNumberStr;
    }


    /**
     * 根据业务类型获取业务号
     * @param bizNumberRule
     * @return
     */
    private Long getBizNumber(BizNumberRule bizNumberRule) {
        //没有范围，则默认使用1为自增量
        if(StringUtils.isBlank(bizNumberRule.getRange())){
            bizNumberRule.setRange("1");
        }
        String[] ranges = bizNumberRule.getRange().split(",");
        int increment = ranges.length == 1 ? Integer.parseInt(ranges[0]) : rangeRandom(Integer.parseInt(ranges[0].trim()), Integer.parseInt(ranges[1].trim()));
        //计算出来的最终步长
        long finalStep;
        //范围步长值取最大自增值的rangeStep倍
        if (ranges.length == 2) {
            finalStep = Long.parseLong(ranges[1]) * bizNumberRule.getStep();
        } else {
            //固定步长值为固定值的fixedStep倍
            finalStep = Long.parseLong(ranges[0]) * bizNumberRule.getStep();
        }
        return getBizNumberByType(bizNumberRule.getType(), bizNumberRule.getDateFormat(), bizNumberRule.getLength(), finalStep, increment);
    }

    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param step
     * @param increment
     * @return
     */
    private Long getBizNumberByType(String type, String dateFormat, int length, long step, int increment) {
        String dateStr = dateFormat == null ? "" : DateUtils.format(dateFormat);
        Long orderId = getNextSequenceId(type, null, dateFormat, length, step, increment);
        //如果不是同天，重新获取从1开始的编号
        if (StringUtils.isNotBlank(dateStr) && !dateStr.equals(StringUtils.substring(String.valueOf(orderId), 0, 8))) {
            orderId = getNextSequenceId(type, getInitBizNumber(dateStr, length), dateFormat, length, step, increment);
        }
        return orderId;
    }

    /**
     * 根据日期格式和长度，获取下一个编号, 失败后重试五次
     * @param type  编码类型
     * @param startSeq  从指定SEQ开始， 一般为空或从当天第1号开始
     * @param dateFormat    日期格式(可以为空)
     * @param length    编码长度
     * @param step  步长
     * @param increment 增量
     * @return
     */
    private Long getNextSequenceId(String type, Long startSeq, String dateFormat, int length, long step, int increment) {
        Long seqId = getNextSeqId(type, startSeq, dateFormat, length, step, increment);
        int i = 0;
        for (; (seqId < 0 && i < RETRY); i++) {// 失败后，最大重复RETRY次获取
            bizNumberMap.remove(type);
            seqId = getNextSeqId(type, startSeq, dateFormat, length, step, increment);
        }
        if(i >= RETRY){
            throw new AppException("5002", String.format("业务号乐观锁重试%s次失败", RETRY));
        }
        return seqId;
    }

    /**
     * 根据日期格式和长度，获取下一个编号
     * @param type
     * @param startSeq
     * @param dateFormat
     * @param length
     * @param increment
     * @param step
     * @return
     */
    private Long getNextSeqId(String type, Long startSeq, String dateFormat, int length, long step, int increment) {
        rangeIdLock.lock();
        try {
            SequenceNo idSequence = bizNumberMap.get(type);
            if (idSequence == null) {
                idSequence = new SequenceNo(step);
                bizNumberMap.putIfAbsent(type, idSequence);
                idSequence = bizNumberMap.get(type);
            }
            //如果是新的一天，startSeq不为空，而是计算的initNumber
            //如果bizNumberMap.get(type)为空，StartSeq >= FinishSeq
            if (startSeq != null || idSequence.getStartSeq() >= idSequence.getFinishSeq()) {
                idSequence = bizNumberComponent.getSeqNoByNewTransactional(idSequence, type, startSeq, dateFormat, length, step);
                if (idSequence == null) {
                    return -1L;
                }
            }
            return increment == 1 ? idSequence.next() : idSequence.next(increment);
        } finally {
            rangeIdLock.unlock();
        }
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


}