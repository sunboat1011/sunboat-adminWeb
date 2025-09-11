package com.sunboat.adminWeb.business.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 多线程分批插入处理器
 * 用于将大量数据分成多个批次，通过多线程并行插入
 */
public class BatchInsertProcessor<T> {
    
    // 线程池
    private final ExecutorService executorService;
    // 每批处理的数据量
    private final int batchSize;
    
    /**
     * 构造函数
     * @param batchSize 每批处理的数据量
     */
    public BatchInsertProcessor(int batchSize) {
        this(batchSize, getRecommendedThreadCount());
    }
    
    /**
     * 构造函数
     * @param batchSize 每批处理的数据量
     * @param threadCount 线程数量
     */
    public BatchInsertProcessor(int batchSize, int threadCount) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批次大小必须大于0");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("线程数量必须大于0");
        }
        
        this.batchSize = batchSize;
        // 创建线程池
        this.executorService = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private int counter = 1;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "batch-insert-thread-" + counter++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 当线程池满时，让提交任务的线程执行任务
        );
    }
    
    /**
     * 获取推荐的线程数量（基于CPU核心数）
     */
    private static int getRecommendedThreadCount() {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        // 插入操作通常是IO密集型，推荐CPU核心数 * 5
        return Math.max(cpuCount * 5, 4); // 至少4个线程
    }
    
    /**
     * 将大List分成多个批次
     */
    private List<List<T>> splitIntoBatches(List<T> dataList) {
        List<List<T>> batches = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<T> batch = dataList.subList(i, endIndex);
            batches.add(batch);
        }
        
        return batches;
    }
    
    /**
     * 执行分批插入
     * @param dataList 要插入的全部数据
     * @param insertHandler 实际执行插入的处理器
     * @return 所有批次的插入结果
     * @throws InterruptedException 线程中断异常
     * @throws ExecutionException 执行异常
     */
    public List<Boolean> processBatchInsert(List<T> dataList, InsertHandler<T> insertHandler) 
            throws InterruptedException, ExecutionException {
        
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 分成多个批次
        List<List<T>> batches = splitIntoBatches(dataList);
        System.out.printf("将 %d 条数据分成 %d 批处理，每批 %d 条%n", 
                dataList.size(), batches.size(), batchSize);
        
        // 提交所有批次任务
        List<Future<Boolean>> futures = batches.stream()
                .map(batch -> executorService.submit(() -> insertHandler.insert(batch)))
                .collect(Collectors.toList());
        
        // 等待所有任务完成并收集结果
        List<Boolean> results = new ArrayList<>(futures.size());
        for (Future<Boolean> future : futures) {
            results.add(future.get()); // 阻塞等待结果
        }
        
        return results;
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    
    /**
     * 插入处理器接口，由调用者实现具体的插入逻辑
     */
    @FunctionalInterface
    public interface InsertHandler<T> {
        /**
         * 执行一批数据的插入
         * @param batch 一批数据
         * @return 插入是否成功
         */
        boolean insert(List<T> batch);
    }
    
    // 测试示例
    public static void main(String[] args) {
        // 生成测试数据（10000条）
        List<String> testData = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            testData.add("测试数据-" + i);
        }
        
        // 创建处理器，每批处理100条数据
        BatchInsertProcessor<String> processor = new BatchInsertProcessor<>(100);
        
        try {
            // 执行分批插入
            long startTime = System.currentTimeMillis();
            List<Boolean> results = processor.processBatchInsert(testData, batch -> {
                // 这里实现实际的插入逻辑
                // 例如：插入数据库、写入文件等
                System.out.printf("线程 %s 插入了 %d 条数据%n", 
                        Thread.currentThread().getName(), batch.size());
                
                // 模拟插入操作耗时
                try {
                    Thread.sleep(50); // 模拟IO操作耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                return true;
            });
            
            long endTime = System.currentTimeMillis();
            
            // 统计结果
            long successCount = results.stream().filter(Boolean::booleanValue).count();
            System.out.printf("全部插入完成，总耗时: %dms, 成功批次: %d, 失败批次: %d%n",
                    endTime - startTime,
                    successCount,
                    results.size() - successCount);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.shutdown();
        }
    }
}
    