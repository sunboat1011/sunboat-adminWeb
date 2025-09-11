package com.sunboat.adminWeb.business.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池大小计算器工具类
 * 根据CPU核心数和业务类型提供推荐的线程池大小
 */
public class ThreadPoolSizeCalculator {
    
    // 获取CPU核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    
    // 默认倍数
    private static final int CPU_INTENSIVE_MULTIPLIER = 1;  // CPU密集型默认倍数
    private static final int IO_INTENSIVE_MULTIPLIER = 20;   // IO密集型默认倍数
    
    /**
     * 获取CPU核心数
     */
    public static int getCpuCount() {
        return CPU_COUNT;
    }
    
    /**
     * 获取CPU密集型任务推荐的线程数
     * CPU密集型任务：如数据计算、逻辑处理等
     * 推荐公式：CPU核心数 * 1
     */
    public static int getCpuIntensiveThreadCount() {
        return CPU_COUNT * CPU_INTENSIVE_MULTIPLIER;
    }
    
    /**
     * 获取CPU密集型任务推荐的线程数（自定义倍数）
     */
    public static int getCpuIntensiveThreadCount(int multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("倍数必须大于等于1");
        }
        return CPU_COUNT * multiplier;
    }
    
    /**
     * 获取IO密集型任务推荐的线程数
     * IO密集型任务：如网络请求、文件读写、数据库操作等
     * 推荐公式：CPU核心数 * 20（可根据实际IO等待时间调整）
     */
    public static int getIoIntensiveThreadCount() {
        return CPU_COUNT * IO_INTENSIVE_MULTIPLIER;
    }
    
    /**
     * 获取IO密集型任务推荐的线程数（自定义倍数）
     */
    public static int getIoIntensiveThreadCount(int multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("倍数必须大于等于1");
        }
        return CPU_COUNT * multiplier;
    }
    
    /**
     * 根据业务类型创建推荐的线程池
     */
    public static ExecutorService createRecommendedThreadPool(TaskType taskType) {
        int corePoolSize;
        int maximumPoolSize;
        
        if (taskType == TaskType.CPU_INTENSIVE) {
            corePoolSize = getCpuIntensiveThreadCount();
            maximumPoolSize = corePoolSize * 2;
        } else {
            corePoolSize = getIoIntensiveThreadCount();
            maximumPoolSize = corePoolSize * 2;
        }
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(corePoolSize);
        executor.setMaximumPoolSize(maximumPoolSize);
        return executor;
    }
    
    /**
     * 任务类型枚举
     */
    public enum TaskType {
        CPU_INTENSIVE,  // CPU密集型任务
        IO_INTENSIVE    // IO密集型任务
    }
    
    // 测试方法
    public static void main(String[] args) {
        System.out.println("当前CPU核心数: " + getCpuCount());
        System.out.println("推荐CPU密集型线程数: " + getCpuIntensiveThreadCount());
        System.out.println("推荐IO密集型线程数: " + getIoIntensiveThreadCount());
        System.out.println("自定义IO密集型线程数(10倍): " + getIoIntensiveThreadCount(10));
        
        // 创建推荐的线程池示例
        ExecutorService cpuPool = createRecommendedThreadPool(TaskType.CPU_INTENSIVE);
        ExecutorService ioPool = createRecommendedThreadPool(TaskType.IO_INTENSIVE);
        
        System.out.println("CPU密集型线程池核心线程数: " + ((ThreadPoolExecutor) cpuPool).getCorePoolSize());
        System.out.println("IO密集型线程池核心线程数: " + ((ThreadPoolExecutor) ioPool).getCorePoolSize());
        
        cpuPool.shutdown();
        ioPool.shutdown();
    }
}
