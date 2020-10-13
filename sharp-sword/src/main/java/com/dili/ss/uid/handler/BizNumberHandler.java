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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberHandler {

    @Autowired
    private BizNumberComponent bizNumberComponent;
    /**
     * 固定步长值，默认为50
     */
    @Value("${uid.fixedStep:50}")
    private int fixedStep;

    /**
     * 范围步长值，默认为最大范围的20倍
     */
    @Value("${uid.rangeStep:20}")
    private int rangeStep;

    private BizNumberManager bizNumberManager;

    @PostConstruct
    public void init() {
        bizNumberManager = new BizNumberManagerImpl();
        bizNumberManager.setBizNumberComponent(bizNumberComponent);
    }

    /**
     * 清除缓存
     * @param type
     */
    public void clear(String type){
        bizNumberManager.clear(type);
    }

    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param range
     * @return
     */
    public Long getBizNumberByType(String type, String dateFormat, int length, String range) {
        //没有范围，则默认使用1为自增量
        if(StringUtils.isBlank(range)){
            range = "1";
        }
        String[] ranges = range.split(",");
        int increment = ranges.length == 1 ? Integer.parseInt(ranges[0]) : rangeRandom(Integer.parseInt(ranges[0].trim()), Integer.parseInt(ranges[1].trim()));
        long step;
        //范围步长值取最大自增值的rangeStep倍
        if (ranges.length == 2) {
            step = Long.parseLong(ranges[1]) * rangeStep;
        } else {
            //固定步长值为固定值的fixedStep倍
            step = Long.parseLong(ranges[0]) * fixedStep;
        }
        if(increment == 1){
            return bizNumberManager.getBizNumberByType(type, dateFormat, length, step);
        }
        return bizNumberManager.getBizNumberByType(type, dateFormat, length, step, increment);
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
