package com.dili.ss.gid.generator;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author WangMi
 * 流水号生成算法，用来生成全局唯一的流水号。
 * 流水号格式：
 * YYYYMMDDhhmmssSSS(8+6+3 Bytes)|workerId(3Bytes)|sn(4Bytes)
 */
@Component
public class GSN extends GeneratorBase {

	/**
	 * 最大序列号数
	 */
	private final long maxSequence = 9999L;

	/**
	 * 日期格式化输出
	 */
	private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized String next() {
		// 获取当前毫秒数
		long curTimestamp = System.currentTimeMillis();

		// 如果服务器时间有问题(时钟后退)，报错
		if (lastTimestamp > curTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - curTimestamp));
		}

		// 如果上次生成时间和当前时间相同，在同一毫秒内
		if (lastTimestamp == curTimestamp) {
			// 判断是否溢出，当sequence超过允许的最大值后，重置为0
			if (++sequence > maxSequence) {
				sequence = 0L;

				// 自旋等待到下一毫秒
				curTimestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			// 如果和上次生成时间不同，重置sequence
			// 即从下一毫秒开始，sequence计数重新从0开始累加
			sequence = 0L;
		}

		lastTimestamp = curTimestamp;

		// 按规则生成snowflake ID
		return df.format(new Date(curTimestamp)) + String.format("%03d%04d", workerId, sequence);
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}

		return timestamp;
	}

}
