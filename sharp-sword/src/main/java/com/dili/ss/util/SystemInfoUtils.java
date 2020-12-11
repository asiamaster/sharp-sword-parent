package com.dili.ss.util;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统信息工具类
 * @author: WM
 * @time: 2020/12/11 14:23
 */
public class SystemInfoUtils {
    private static final SystemInfo systemInfo = new SystemInfo();

    /**
     * 获取CPU利用率百分比，以1为100%，保留四位小数，未使用返回0
     * @return
     */
    public static String getCpuUseRatio(){
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        if(totalCpu == 0){
            return "0";
        }
        BigDecimal cpuUseRatio = new BigDecimal(totalCpu-idle).divide(BigDecimal.valueOf(totalCpu), 4, RoundingMode.HALF_UP);
        return new DecimalFormat("#.####").format(cpuUseRatio);
    }

    /**
     * 获取逻辑处理器数(4c8t的U返回8)
     * @return
     */
    public static Integer getLogicalProcessorCount(){
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        return processor.getLogicalProcessorCount();
    }

    /**
     * 获取全局内存对象
     * @return
     */
    public static GlobalMemory getMemory(){
        return systemInfo.getHardware().getMemory();
    }

    /**
     * 获取总内存大小，单位(Byte)
     * @return
     */
    public static long getMemoryTotalByte(){
        return getMemory().getTotal();
    }

    /**
     * 获取可用内存大小，单位(Byte)
     * @return
     */
    public static long getMemoryAvailableByte(){
        return getMemory().getAvailable();
    }

    /**
     * 获取内存利用率百分比，以1为100%，保留四位小数
     * @return
     */
    public static String getMemoryUseRatio(){
        GlobalMemory memory = getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();
        BigDecimal memoryUseRatio = new BigDecimal(totalByte-availableByte).divide(BigDecimal.valueOf(totalByte), 4, RoundingMode.HALF_UP);
        return new DecimalFormat("#.####").format(memoryUseRatio);
    }

    /**
     * 获取磁盘信息
     * @return
     */
    public static List<HWDiskStore> getDiskStores(){
        return systemInfo.getHardware().getDiskStores();
    }

    /**
     * Get file stores on this machine
     * @return
     */
    public static List<OSFileStore> getFileStores(){
        return systemInfo.getOperatingSystem().getFileSystem().getFileStores();
    }

    /**
     * 获取所有磁盘利用率百分比，以1为100%，保留四位小数
     * @return
     */
    public static List<String> getDiskUseRatio(){
        List<OSFileStore> fileStores = getFileStores();
        List<String> diskUseRatios = new ArrayList<>(fileStores.size());
        for (OSFileStore fileStore : fileStores) {
            //可用空间
            long usable = fileStore.getUsableSpace();
            long total = fileStore.getTotalSpace();
            BigDecimal diskUseRatio = new BigDecimal(total - usable).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            diskUseRatios.add(new DecimalFormat("#.####").format(diskUseRatio));
        }
        return diskUseRatios;
    }

    /**
     * 获取系统信息
     * @return
     */
    public static SystemInfo getSystemInfo(){
        return systemInfo;
    }

    /**
     * 格式化内存显示
     * @param byteNumber
     * @return
     */
    public static String formatByte(long byteNumber){
        double FORMAT = 1024.0;
        double kbNumber = byteNumber/FORMAT;
        if(kbNumber<FORMAT){
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber/FORMAT;
        if(mbNumber<FORMAT){
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber/FORMAT;
        if(gbNumber<FORMAT){
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber/FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }

}
