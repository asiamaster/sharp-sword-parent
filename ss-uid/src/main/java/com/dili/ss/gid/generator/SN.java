package com.dili.ss.gid.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangMi
 * 生成指定长度的序号
 */
@Component
public class SN {

	/**
	 * 长度与最大取值的映射
	 * 长度的取值范围: [1, 18]
	 */
	private final Map<Integer, Long> map = new HashMap<>();

	private long sequence = 0L;

    public static void main(String[] args) {
        SN sn = new SN();
        for (int i=0; i<10; i++) {
            System.out.println(sn.nextRsn(2));
        }
    }
	public SN() {
		long maxVal = 1L;
		for (int len = 1; len < 19; ++len) {
			maxVal *= 10;
			map.put(len, maxVal);
		}
	}

	/**
	 * 获取新的依次递增的SN
	 * 
	 * @param len SN长度，长度区间为: [1,18]
	 * @return
	 */
	public synchronized String nextSn(int len) {
		// 不产生序号0
		if (++sequence >= map.get(len)) {
			sequence = 1L;
		}

		return String.format("%0" + len + "d", sequence);
	}

	/**
	 * 获取新的随机SN
	 * 
	 * @param len RSN长度，长度区间为: [1,18]
	 * @return
	 */
	public synchronized String nextRsn(int len) {
		return String.format("%0" + len + "d", RandomUtils.nextLong(1, map.get(len)));
	}

	/**
	 * 获取新的随机码
	 * 
	 * @param len 随机码长度，必须>0
	 * @return
	 */
	public synchronized String nextRc(int len) {
		return RandomStringUtils.randomAlphanumeric(len).replaceAll("O", "0").replaceAll("l", "L");
	}

}
