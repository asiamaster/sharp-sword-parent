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
import com.dili.ss.uid.dao.BizNumberMapper;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.handler.BizNumberHandler;
import com.dili.ss.uid.service.BizNumberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * 业务号生成服务
 *
 * @author asiamaster
 */
@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberServiceImpl extends BaseServiceImpl<BizNumber, Long> implements BizNumberService {

    @Autowired
    private BizNumberHandler bizNumberHandler;

    public BizNumberMapper getActualDao() {
        return (BizNumberMapper)getDao();
    }

    @Override
    public BizNumber selectOne(BizNumber bizNumber){
        return getActualDao().selectOne(bizNumber);
    }
    @Override
    public String getBizNumberByType(BizNumberRule bizNumberRule) {
        if(bizNumberRule == null){
            return null;
        }
//        String range = bizNumberRule.getRange();
//        //默认自增步长为1
//        if(StringUtils.isBlank(range)){
//            range = "1";
//        }else {
//            //验证自增范围
//            String[] ranges = range.split(",");
//            if (ranges.length > 2) {
//                throw new RuntimeException("非法自增步长范围");
//            }
//        }
        Long bizNumber = bizNumberHandler.getBizNumberByType(bizNumberRule.getType(), bizNumberRule.getDateFormat(), bizNumberRule.getLength(), bizNumberRule.getRange());
        if(StringUtils.isBlank(bizNumberRule.getDateFormat())){
            return bizNumberRule.getPrefix() + String.format("%0" + bizNumberRule.getLength() + "d", bizNumber);
        }else {
            return bizNumberRule.getPrefix() + bizNumber;
        }
    }

}