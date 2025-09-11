package com.sunboat.adminWeb.business.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 适配MyBatis Mapper的多线程分批插入工具类
 */
public class BatchInsertWithMapper<T> {
    
    // 线程池
    private final ExecutorService executorService;
    // 每批处理的数据量
    private final int batchSize;
    
    /**
     * 构造函数
     * @param batchSize 每批处理的数据量
     */
    public BatchInsertWithMapper(int batchSize) {
        this(batchSize, getRecommendedThreadCount());
    }
    
    /**
     * 构造函数
     * @param batchSize 每批处理的数据量
     * @param threadCount 线程数量
     */
    public BatchInsertWithMapper(int batchSize, int threadCount) {
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
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    /**
     * 获取推荐的线程数量（基于CPU核心数）
     */
    private static int getRecommendedThreadCount() {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        // 数据库操作是IO密集型，推荐CPU核心数 * 5
        return Math.max(cpuCount * 5, 4);
    }
    
    /**
     * 将大List分成多个批次
     */
    private List<List<T>> splitIntoBatches(List<T> dataList) {
        List<List<T>> batches = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<T> batch = dataList.subList(i, endIndex);
            batches.add(new ArrayList<>(batch)); // 转为新的ArrayList，避免subList的视图问题
        }
        
        return batches;
    }
    
    /**
     * 执行分批插入（适配MyBatis Mapper）
     * @param dataList 要插入的全部数据
     * @param mapper 你的Mapper接口实例
     * @param insertAction 调用Mapper的插入方法
     * @return 所有批次的插入结果
     */
    public List<Integer> processBatchInsert(
            List<T> dataList, 
            Object mapper,
            InsertAction<T> insertAction) throws InterruptedException, ExecutionException {
        
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 分成多个批次
        List<List<T>> batches = splitIntoBatches(dataList);
        System.out.printf("将 %d 条数据分成 %d 批处理，每批 %d 条%n", 
                dataList.size(), batches.size(), batchSize);
        
        // 提交所有批次任务
        List<Future<Integer>> futures = batches.stream()
                .map(batch -> executorService.submit(() -> {
                    // 调用传入的Mapper插入方法
                    return insertAction.insert(mapper, batch);
                }))
                .collect(Collectors.toList());
        
        // 等待所有任务完成并收集结果
        List<Integer> results = new ArrayList<>(futures.size());
        for (Future<Integer> future : futures) {
            results.add(future.get()); // 阻塞等待结果，获取插入成功的数量
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
     * 插入动作接口，用于调用Mapper的批量插入方法
     */
    @FunctionalInterface
    public interface InsertAction<T> {
        /**
         * 调用Mapper的批量插入方法
         * @param mapper Mapper接口实例
         * @param batch 一批数据
         * @return 插入成功的数量
         */
        int insert(Object mapper, List<T> batch);
    }
    
    // 使用示例
    public static void main(String[] args) {
        // 假设你有这些实例（实际项目中由Spring注入）
        // UserMapper userMapper = springContext.getBean(UserMapper.class);
        
        // 生成测试数据
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setId(i);
            user.setName("User-" + i);
            userList.add(user);
        }
        
        // 创建处理器，每批插入200条
        BatchInsertWithMapper<User> insertProcessor = new BatchInsertWithMapper<>(200);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 执行批量插入，这里调用你的Mapper方法
            List<Integer> results = insertProcessor.processBatchInsert(
                    userList,
                    null, // 实际使用时传入你的userMapper实例
                    (mapper, batch) -> {
                        // 强制转换为你的Mapper类型，调用批量插入方法
                        UserMapper userMapper = (UserMapper) mapper;
                        return userMapper.batchInsert(batch); // 假设你的Mapper有这个方法
                    }
            );
            
            long endTime = System.currentTimeMillis();
            
            // 统计总插入数量
            int totalInserted = results.stream().mapToInt(Integer::intValue).sum();
            System.out.printf("插入完成，总耗时: %dms, 总插入数量: %d%n",
                    endTime - startTime, totalInserted);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            insertProcessor.shutdown();
        }
    }
    
    // 示例实体类
    static class User {
        private int id;
        private String name;
        
        // getter和setter
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    // 示例Mapper接口（你的项目中应该已经有类似的）
    interface UserMapper {
        // 批量插入方法，返回插入成功的数量
        int batchInsert(List<User> users);
    }
}
    