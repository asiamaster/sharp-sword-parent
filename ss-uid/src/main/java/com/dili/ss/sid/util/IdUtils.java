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
package com.dili.ss.sid.util;

import com.dili.ss.sid.dto.SnowflakeId;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snowflake算法采用41bit毫秒时间戳，加上10bit机器ID，加上12bit序列号，理论上最多支持1024台机器每秒生成4096000个序列号，对于Twitter的规模来说够用了。
 * 但是对于绝大部分普通应用程序来说，根本不需要每秒超过400万的ID，机器数量也达不到1024台，所以，我们可以改进一下，使用更短的ID生成方式：
 * 53bitID由32bit秒级时间戳+16bit自增+5bit机器标识组成，累积32台机器，每秒可以生成6.5万个序列号
 * 时间戳减去一个固定值，此方案最高可支持到2106年。
 * 如果每秒6.5万个序列号不够怎么办？没关系，可以继续递增时间戳，向前“借”下一秒的6.5万个序列号。
 * 同时还解决了时间回拨的问题。
 *
 * 机器标识采用简单的主机名方案，只要主机名符合host-1，host-2就可以自动提取机器标识，无需配置。
 * 如果主机名不是host-数字形式，则使用IP地址生成
 * 最后，为什么采用最多53位整型，而不是64位整型？这是因为考虑到大部分应用程序是Web应用，如果要和JavaScript打交道，
 * 由于JavaScript支持的最大整型就是53位，超过这个位数，JavaScript将丢失精度。因此，使用53位整数可以直接由JavaScript读取，
 * 而超过53位时，就必须转换成字符串才能保证JavaScript处理正确，这会给API接口带来额外的复杂度。
 * 这也是为什么新浪微博的API接口会同时返回id和idstr的原因。
 * 53 bits unique id:
 *
 * |--------|--------|--------|--------|--------|--------|--------|--------|
 * |00000000|00011111|11111111|11111111|11111111|11111111|11111111|11111111|
 * |--------|---xxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx|xxx-----|--------|--------|
 * |--------|--------|--------|--------|--------|---xxxxx|xxxxxxxx|xxx-----|
 * |--------|--------|--------|--------|--------|--------|--------|---xxxxx|
 *
 * Maximum ID = 11111_11111111_11111111_11111111_11111111_11111111_11111111
 *
 * Maximum TS = 11111_11111111_11111111_11111111_111
 *
 * Maximum NT = ----- -------- -------- -------- ---11111_11111111_111 = 65535
 *
 * Maximum SH = ----- -------- -------- -------- -------- -------- ---11111 = 31
 *
 * It can generate 64k unique id per IP and up to 2106-02-07T06:28:15Z.
 */
public final class IdUtils {

    private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);

    private static final Pattern PATTERN_LONG_ID = Pattern.compile("^([0-9]{15})([0-9a-f]{32})([0-9a-f]{3})$");

    private static final Pattern PATTERN_HOSTNAME = Pattern.compile("^.*\\D+([0-9]+)$");

    private static final long OFFSET = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("Z")).toEpochSecond();

    private static final long MAX_NEXT = 0b11111_11111111_111L;

    private static final long SHARD_ID = getServerIdAsLong();

    private static long offset = 0;

    private static long lastEpoch = 0;

    //用于解析id
    private static final long SHARD_ID_MASK = ~(-1L << 5);
    private static final long SEQUENCE_MASK = ~(-1L << 16);
    private static final long TIMESTAMP_MASK = ~(-1L << 32);


    /**
     * a stringId id is composed as timestamp (15) + uuid (32) + serverId (000~fff).
     * @param stringId
     * @return
     */
    public static long stringIdToLongId(String stringId) {
        Matcher matcher = PATTERN_LONG_ID.matcher(stringId);
        if (matcher.matches()) {
            long epoch = Long.parseLong(matcher.group(1)) / 1000;
            String uuid = matcher.group(2);
            byte[] sha1 = HashUtil.sha1AsBytes(uuid);
            long next = ((sha1[0] << 24) | (sha1[1] << 16) | (sha1[2] << 8) | sha1[3]) & MAX_NEXT;
            long serverId = Long.parseLong(matcher.group(3), 16);
            return generateId(epoch, next, serverId);
        }
        throw new IllegalArgumentException("Invalid id: " + stringId);
    }

    //使用参考
//    public static void main(String[] args) {
//        long now = System.currentTimeMillis();
//        String uid = UUID.randomUUID().toString().replaceAll("-", "");
//        long id = stringIdToLongId(now+"00"+uid+"fff");
//        System.out.println(id);
//        id = nextId();
//        SnowflakeId snowflakeId = expId(id);
//        System.out.println(transTime(snowflakeId.getTimeStamp()));
//    }

    /**
     * 对id进行解析
     *
     * @param id 生成的ID
     * @return 封装的ID类，包括WorkerId、Sequence和TimeStamp
     */
    public static SnowflakeId expId(long id) {
        SnowflakeId ret = new SnowflakeId();
        ret.setWorkerId(id & SHARD_ID_MASK);
        ret.setSequence((id >>> 5) & SEQUENCE_MASK);
        ret.setTimeStamp((id >>> 5+16) & TIMESTAMP_MASK);
        return ret;
    }

    /**
     * 对时间戳单独进行解析
     *
     * @param time 时间戳
     * @return 生成的Date时间
     */
    public static Date transTime(long time) {
        return new Date(OFFSET*1000+time*1000);
    }

    /**
     * 生成53位唯一id
     * @return
     */
    public static long nextId() {
        return nextId(System.currentTimeMillis() / 1000);
    }

    private static synchronized long nextId(long epochSecond) {
        if (epochSecond < lastEpoch) {
            // warning: clock is turn back:
            logger.warn("clock is back: " + epochSecond + " from previous:" + lastEpoch);
            epochSecond = lastEpoch;
        }
        if (lastEpoch != epochSecond) {
            lastEpoch = epochSecond;
            reset();
        }
        offset++;
        long next = offset & MAX_NEXT;
        if (next == 0) {
            logger.warn("maximum id reached in 1 second in epoch: " + epochSecond);
            return nextId(epochSecond + 1);
        }
        return generateId(epochSecond, next, SHARD_ID);
    }

    private static void reset() {
        offset = 0;
    }

    private static long generateId(long epochSecond, long next, long shardId) {
        return ((epochSecond - OFFSET) << 21) | (next << 5) | shardId;
    }

    private static long getServerIdAsLong() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            Matcher matcher = PATTERN_HOSTNAME.matcher(hostname);
            if (matcher.matches()) {
                long n = Long.parseLong(matcher.group(1));
                if (n >= 0 && n < 32) {
                    logger.info("detect server id from host name {}: {}.", hostname, n);
                    return n;
                }
            }else{
                try {
                    String hostAddress = Inet4Address.getLocalHost().getHostAddress();
                    int[] ints = StringUtils.toCodePoints(hostAddress);
                    int sums = 0;
                    for(int b : ints){
                        sums += b;
                    }
                    return (long)(sums % 32);
                } catch (UnknownHostException e) {
                    // 如果获取失败，则使用随机数备用
                    return RandomUtils.nextLong(0,31);
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("unable to get host name. set server id = 0.");
        }
        return 0;
    }

}