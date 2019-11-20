package com.dili.ss.gid.generator;

import org.springframework.stereotype.Component;

/**
 * @author wangmi.mi
 * Twitter的SnowFlake算法，用来生成全局唯一的ID（GID）。
 * snowflake ID (64位):
 * 0|0000000 00000000 00000000 00000000 00000000 00|000000 0000|0000 00000000
 * 说明：
 * 最高位保留，不用；
 * 第2部分的41位，表示以毫秒为单位的时间戳(从2016-01-01 01:42:54开始)，可以使用69年；
 * 第3部分的10位，表示数据中心ID和工作节点ID，可按需调整数据中心ID和工作节点ID的位数；
 * 第4部分的12位，为一个不断增加的序列号。
 */
@Component
public class GID  extends GeneratorBase{

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

	private final long twepoch = 1288834974657L;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized long next() {
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
			// sequence自增，因为sequence只有12bit，所以和sequenceMask按位与，去掉高位
			sequence = (sequence + 1) & sequenceMask;

			// 判断是否溢出，当为4096时，与sequenceMask按位与后，sequence等于0
			if (sequence == 0) {
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
		return ((curTimestamp - twepoch) << timestampLeftShift) | (dcId << dcIdLeftShift)
				| (workerId << workerIdLeftShift) | sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}

		return timestamp;
	}

}
