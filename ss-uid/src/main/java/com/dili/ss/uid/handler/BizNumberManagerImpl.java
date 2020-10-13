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

import com.dili.ss.uid.domain.SequenceNo;
import com.dili.ss.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BizNumberManagerImpl implements BizNumberManager{

    public static ReentrantLock rangeIdLock = new ReentrantLock(false);

    public static ReentrantLock lock = new ReentrantLock(false);

    private BizNumberComponent bizNumberComponent;

    protected ConcurrentHashMap<String, SequenceNo> bizNumberMap = new ConcurrentHashMap<>();

    //获取失败后的重试次数
    protected static final int RETRY = 5;

    @Override
    public void clear(String type){
        if(bizNumberMap.containsKey(type)) {
            bizNumberMap.remove(type);
        }
    }

    /**
     * 根据业务类型获取业务号
     * @param type
     * @param dateFormat
     * @param length
     * @param step
     * @return
     */
    @Override
    public Long getBizNumberByType(String type, String dateFormat, int length, long step) {
        String dateStr = dateFormat == null ? null : DateUtils.format(dateFormat);
        Long orderId = getNextSequenceId(type, null, dateFormat, length, step);
        //如果不是同天，重新获取从1开始的编号
        if (StringUtils.isNotBlank(dateStr) && !dateStr.equals(StringUtils.substring(String.valueOf(orderId), 0, 8))) {
            orderId = getNextSequenceId(type, getInitBizNumber(dateStr, length), dateFormat, length, step);
        }
        return orderId;
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
    @Override
    public Long getBizNumberByType(String type, String dateFormat, int length, long step, int increment) {
        String dateStr = DateUtils.format(dateFormat);
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
        for (int i = 0; (seqId < 0 && i < RETRY); i++) {// 失败后，最大重复3次获取
            bizNumberMap.remove(type);
            seqId = getNextSeqId(type, startSeq, dateFormat, length, step, increment);
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
        SequenceNo idSequence = bizNumberMap.get(type);
        if (idSequence == null) {
            idSequence = new SequenceNo(step);
            bizNumberMap.putIfAbsent(type, idSequence);
            idSequence = bizNumberMap.get(type);
        }
        //如果是新的一天，startSeq不为空，而是计算的initNumber
        //如果bizNumberMap.get(type)为空，StartSeq >= FinishSeq
        if (startSeq != null || idSequence.getStartSeq() >= idSequence.getFinishSeq()) {
            idSequence = bizNumberComponent.getSeqNoByNewTransactional(idSequence, type, startSeq, dateFormat, length);
            if (idSequence == null) {
                return -1L;
            }
        }
        try {
            return increment == 1 ? idSequence.next() : idSequence.next(increment);
        }finally {
            rangeIdLock.unlock();
        }
    }

    /**
     * 根据日期格式和长度，获取下一个编号, 失败后重试五次
     * 增量为1
     * @param type  编码类型
     * @param startSeq  从指定SEQ开始， 一般为空或从当天第1号开始
     * @param dateFormat    日期格式(可以为空)
     * @param length    编码长度
     * @param step 步长
     * @return
     */
    private Long getNextSequenceId(String type, Long startSeq, String dateFormat, int length, long step) {
        Long seqId = getNextSeqId(type, startSeq, dateFormat, length, step);
        for (int i = 0; (seqId < 0 && i < RETRY); i++) {// 失败后，最大重复3次获取
            bizNumberMap.remove(type);
            seqId = getNextSeqId(type, startSeq, dateFormat, length, step);
        }
        return seqId;
    }

    /**
     * 根据日期格式和长度，获取下一个编号, 增量为1
     * @param type
     * @param startSeq
     * @param dateFormat
     * @param length
     * @param step
     * @return
     */
    private Long getNextSeqId(String type, Long startSeq, String dateFormat, int length, long step) {
        lock.lock();
        SequenceNo idSequence = bizNumberMap.get(type);
        if (idSequence == null) {
            //固定步长值为fixedStep
            idSequence = new SequenceNo(step);
            bizNumberMap.putIfAbsent(type, idSequence);
            idSequence = bizNumberMap.get(type);
        }
        //如果是新的一天，startSeq不为空，而是计算的initNumber
        //如果bizNumberMap.get(type)为空，StartSeq >= FinishSeq
        if (startSeq != null || idSequence.getStartSeq() >= idSequence.getFinishSeq()) {
            idSequence = bizNumberComponent.getSeqNoByNewTransactional(idSequence, type, startSeq, dateFormat, length);
            if (idSequence == null) {
                lock.unlock();
                return -1L;
            }
        }
        try {
            return idSequence.next();
        }finally {
            lock.unlock();
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

    @Override
    public void setBizNumberComponent(BizNumberComponent bizNumberComponent) {
        this.bizNumberComponent = bizNumberComponent;
    }


//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ExecutorService executor = new ExportThreadPoolExecutor().getCustomThreadPoolExecutor();
//        Callable thread = new Callable(){
//            @Override
//            public Object call() {
//                return test();
//            }
//        };
//        int COUNT = 12100;
//        List<Future<Long>> futures = new ArrayList(COUNT);
//        for(int i=0; i<COUNT; i++) {
//            futures.add(executor.submit(thread));
//        }
//        List<Long> ids = new ArrayList<>(COUNT);
//        for(int i=0; i<COUNT; i++) {
//            ids.add(futures.get(i).get());
//        }
//        System.out.println("获取数据:" + ids.size() +"条");
//        List<Long> duplicates = listDuplicates(ids);
//        System.out.println("重复数据:");
//        for(Long du : duplicates){
//            System.out.println(du);
//        }
//        System.out.println("完成");
//    }
//
//    public static List listDuplicates(List list){
//        List listTemp = new ArrayList();
//        List duplicates = new ArrayList();
//        int size = list.size();
//        for(int i=0;i<size;i++){
//            if(!listTemp.contains(list.get(i))){
//                listTemp.add(list.get(i));
//            }else{
//                duplicates.add(list.get(i));
//            }
//        }
//        return duplicates;
//    }
//
//    static ConcurrentHashMap<String, SequenceNo> bizNumberMap1 = new ConcurrentHashMap<>();
//    private static Long test(){
//        String type = "order";
//        SequenceNo idSequence = bizNumberMap1.get(type);
//        if (idSequence == null) {
//            //固定步长值为fixedStep
//            idSequence = new SequenceNo(50L);
//            bizNumberMap1.putIfAbsent(type, idSequence);
//            idSequence = bizNumberMap1.get(type);
//        }
//        return idSequence.next();
//    }
}