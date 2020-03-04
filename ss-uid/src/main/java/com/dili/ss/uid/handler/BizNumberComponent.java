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

import com.dili.ss.dto.DTOUtils;
import com.dili.ss.uid.dao.BizNumberMapper;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.SequenceNo;
import com.dili.ss.uid.util.BizNumberUtils;
import com.dili.ss.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberComponent {

    @Autowired
    public BizNumberMapper bizNumberMapper;

    /**
     * 根据bizNumberType从数据库获取包含当前日期的当前编码值,并更新biz_number表的value值为finishSeq
     * @param idSequence
     * @param type
     * @param startSeq
     * @param dateFormat 日期格式，必填
     * @param length 编码位数(不包含日期位数)
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
    public SequenceNo getSeqNoByNewTransactional(SequenceNo idSequence, String type, Long startSeq, String dateFormat, int length){
        BizNumber bizNumber = this.getBizNumberByType(type);
        if(bizNumber == null){
            throw new RuntimeException("业务类型不存在");
        }
        //每天最大分配号数
        int max = new Double(Math.pow(10, length)).intValue();
        String dateStr = dateFormat == null ? null : DateUtils.format(dateFormat);
        Long initBizNumber = BizNumberUtils.getInitBizNumber(dateStr, length);
        //先申请占位
        Long tempStartSeq = 0L;
        if(startSeq != null){
            //新的一天,新日期的值大于数据库中的值，则采用新算的值
            if(startSeq > bizNumber.getValue()){
                tempStartSeq = startSeq;
            }
            //解决分布式环境下第二天的第二台服务器第一笔单日期未更新问题
            //判断新日期计息的值比数据库中的值小，则采用数据库中的value
//            else if(startSeq <= bizNumber.getValue()){
            else{
                tempStartSeq = bizNumber.getValue();
            }
            if(tempStartSeq > initBizNumber + max - 1){
                throw new RuntimeException("当天业务编码分配数超过" + max + ",无法分配!");
            }
        }else{
            tempStartSeq = bizNumber.getValue();
        }
        bizNumber.setValue(tempStartSeq + idSequence.getStep());
        try {
            //当更新失败后，返回空，外层进行重试
            int count = bizNumberMapper.updateByPrimaryKey(bizNumber);
            if (count < 1) {
                System.out.println("乐观锁更新失败后，返回空，外层进行重试!");
                return null;
            }
        }catch (RuntimeException e){
            System.out.println("当更新失败后，返回空，外层进行重试:"+e.getMessage());
            return null;
        }
        //设置idSequence
        idSequence.setStartSeq(tempStartSeq);
        idSequence.setFinishSeq(tempStartSeq + idSequence.getStep());
        return idSequence;
    }

    /**
     * 根据业务类型查询BizNumber对象， 无值返回null
     * @param type
     * @return
     */
    private BizNumber getBizNumberByType(String type){
        BizNumber bizNumber = DTOUtils.newDTO(BizNumber.class);
        bizNumber.setType(type);
        List<BizNumber> list = bizNumberMapper.select(bizNumber);
        if(list == null || list.isEmpty()){
            return null;
        }
        if(list.size() > 1){
            StringBuilder sb = new StringBuilder();
            sb.append("重复的类型:");
            sb.append(type);
            sb.append(",无法确定使用哪一个");
            throw new RuntimeException(sb.toString());
        }
        return list.get(0);
    }


}
