package com.dili.ss.gid.generator;

import org.springframework.stereotype.Component;

/**
 * @author wang.mi
 * Twitter的SnowFlake算法，用来生成全局唯一的ID（GID）。
 * new Snowflake ID (64位):
 * 00000000 00|000000 00000000 00000000 00000000 00|000000 0000|0000 00000000
 * 说明：
 * 最高10位保留，不用；
 * 第2部分的32位，表示以秒为单位的时间戳(从2016-01-01 00:00:00开始)，可以使用135年；
 * 第3部分的10位，表示数据中心ID和工作节点ID，可按需调整数据中心ID和工作节点ID的位数；
 * 第4部分的12位，为一个不断增加的序列号。
 */


@Component
public class GIDShort extends GeneratorBase {

	private final double MS_TO_SEC = 0.001;

	/**
	 * 序列号12位
	 */
	private final long sequenceBits = 12L;

	/**
	 * 机器节点左移位数
	 */
	private final long workerIdLeftShift = sequenceBits;
	/**
	 * 数据中心节点左移位数
	 */
	private final long dcIdLeftShift = sequenceBits + workerIdBits;
	/**
	 * 时间毫秒数左移位数
	 */
	private final long timestampLeftShift = sequenceBits + workerIdBits + dcIdBits;

	/**
	 * 2 的 sequenceBits次方
	 */
	private final long sequenceMask = -1L ^ (-1L << sequenceBits);

	/**
	 * 2016-01-01 00:00:00 (秒)
	 */
	private final long twepoch = 1451577600L;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized long next() {
		// 获取当前秒数
		long curTimestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);

		// 如果服务器时间有问题(时钟后退)，报错
		if (lastTimestamp > curTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - curTimestamp));
		}

		// 如果上次生成时间和当前时间相同，在同一秒内
		if (lastTimestamp == curTimestamp) {
			// sequence自增，因为sequence只有12bit，所以和sequenceMask按位与，去掉高位
			sequence = (sequence + 1) & sequenceMask;

			// 判断是否溢出，当为4096时，与sequenceMask按位与后，sequence等于0
			if (sequence == 0) {
				// 自旋等待到下一秒
				curTimestamp = tilNextMillis((long) (lastTimestamp * MS_TO_SEC));
			}
		} else {
			// 如果和上次生成时间不同，重置sequence
			// 即从下一毫秒开始，sequence计数重新从0开始累加
			sequence = 0L;
		}

		lastTimestamp = curTimestamp;

		// 按规则生成snowflake ID
		return ((curTimestamp - twepoch) << timestampLeftShift) | (dcId << dcIdLeftShift)
				| (workerId << workerIdLeftShift) | sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);
		while (timestamp <= lastTimestamp) {
			timestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);
		}

		return timestamp;
	}

}
