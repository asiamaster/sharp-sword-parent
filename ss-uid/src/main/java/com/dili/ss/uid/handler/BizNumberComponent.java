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
package com.dili.ss.uid.handler;

import com.dili.ss.uid.constants.BizNumberConstant;
import com.dili.ss.uid.domain.BizNumberAndRule;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.domain.SequenceNo;
import com.dili.ss.uid.mapper.BizNumberMapper;
import com.dili.ss.uid.mapper.BizNumberRuleMapper;
import com.dili.ss.uid.util.BizNumberUtils;
import com.dili.ss.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberComponent {

    protected static final Logger log = LoggerFactory.getLogger(BizNumberComponent.class);
    @Autowired
    BizNumberMapper bizNumberMapper;
    @Autowired
    BizNumberRuleMapper bizNumberRuleMapper;


    /**
     * 根据bizNumberType从数据库获取包含当前日期的当前编码值,并更新biz_number表的value值为finishSeq
     * 因多实例部署，dateFormat, length和step三个参数暂时停用，在步长用完后，从数据库更新。后期可以考虑使用mq通知
     * @param idSequence
     * @param type
     * @param startSeq
     * @param dateFormat 日期格式，必填
     * @param length 编码位数(不包含日期位数)
     * @param step 步长
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
    public SequenceNo getSeqNoByNewTransactional(SequenceNo idSequence, String type, Long startSeq, String dateFormat, int length, long step){
//        BizNumber bizNumber = this.getBizNumberByType(type);
//        if(bizNumber == null){
////            throw new RuntimeException("业务类型不存在");
//            log.error("业务类型不存在!");
//            return null;
//        }
//        //多实例场景，这里获取业务规则是为了在当前实例的前一个步长用完后，更新步长等信息
//        BizNumberRuleDomain bizNumberRuleByType = getBizNumberRuleByType(type);
//        if(bizNumberRuleByType == null){
//            log.error("业务类型规则不存在!");
//            return null;
//        }

        BizNumberAndRule bizNumberAndRule = bizNumberMapper.getBizNumberAndRule(type);
        if(bizNumberAndRule == null){
            log.error("业务号类型[{}]不存在!", type);
            return null;
        }
        //每天最大分配号数
        int max = new Double(Math.pow(10, bizNumberAndRule.getLength())).intValue();
        String dateStr = bizNumberAndRule.getDateFormat() == null ? null : DateUtils.format(bizNumberAndRule.getDateFormat());
        Long initBizNumber = BizNumberUtils.getInitBizNumber(dateStr, bizNumberAndRule.getLength());
        //当前编号的日期部分，用于在新的日期变更步长
        Long currentDateValue = dateStr == null ? bizNumberAndRule.getValue() : Long.parseLong(bizNumberAndRule.getValue().toString().substring(0,8));
        //开始序号
        Long tempStartSeq = 0L;
        //如果翻天， startSeq则不为空
        if(startSeq != null){
            Long startSeqDateValue = dateStr == null ? startSeq : Long.parseLong(startSeq.toString().substring(0,8));
            //新的一天,新日期的值大于数据库中的值，则采用新算的值
            if(startSeqDateValue > currentDateValue){
                tempStartSeq = startSeq;
            }
            //解决分布式环境下第二天的第二台服务器第一笔单日期未更新问题
            //判断新日期计息的值比数据库中的值小，则采用数据库中的value
//            else if(startSeq <= bizNumber.getValue()){
            else{
                tempStartSeq = bizNumberAndRule.getValue();
            }
            if(tempStartSeq > initBizNumber + max - 1){
//                throw new RuntimeException("当天业务编码分配数超过" + max + ",无法分配!");
                log.error("[{}]当天业务编码分配数超过{},无法分配!", type, max);
                return null;
            }
        }else{//如果达到步长，则取数据库当前编号
            tempStartSeq = bizNumberAndRule.getValue();
        }
        //这里是当前值加上外层方法根据range和step计算出的最终步长
        //范围步长值取最大自增值的rangeStep倍, 固定步长值为固定值的fixedStep倍
        bizNumberAndRule.setValue(tempStartSeq + step);
        try {
            //当更新失败后，返回空，外层进行重试
            int count = bizNumberMapper.updateByPrimaryKey(bizNumberAndRule);
            if (count < 1) {
                log.info("乐观锁更新失败后，返回空，外层进行重试!");
                return null;
            }
        }catch (RuntimeException e){
            log.error("当更新失败后，返回空，外层进行重试:{}", e.getMessage());
            return null;
        }
        //设置缓存的idSequence
        idSequence.setStartSeq(tempStartSeq);
        idSequence.setStep(bizNumberAndRule.getStep());
        idSequence.setFinishSeq(tempStartSeq + bizNumberAndRule.getStep());
        //更新当前实例缓存的bizNumberRule
        updateCachedBizNumberRule(bizNumberAndRule, type);
        return idSequence;
    }

    /**
     * 更新内存中缓存的业务号规则
     * @param bizNumberAndRule
     */
    private void updateCachedBizNumberRule(BizNumberAndRule bizNumberAndRule, String type){
        BizNumberRule cachedBizNumberRule = BizNumberConstant.bizNumberCache.get(type);
        cachedBizNumberRule.setPrefix(bizNumberAndRule.getPrefix());
        cachedBizNumberRule.setDateFormat(bizNumberAndRule.getDateFormat());
        cachedBizNumberRule.setLength(bizNumberAndRule.getLength());
        cachedBizNumberRule.setStep(bizNumberAndRule.getStep());
        cachedBizNumberRule.setRange(bizNumberAndRule.getRange());
    }

}
