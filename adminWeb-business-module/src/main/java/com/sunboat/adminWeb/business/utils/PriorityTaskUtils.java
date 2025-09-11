package com.sunboat.adminWeb.business.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 优先级任务处理工具类
 * 支持提交不同优先级的任务，并等待所有任务完成
 */
public class PriorityTaskUtils {
    
    // 优先级定义
    public static final int PRIORITY_HIGHEST = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_NORMAL = 3;
    public static final int PRIORITY_LOW = 4;
    public static final int PRIORITY_LOWEST = 5;
    
    // 单例线程池，使用支持优先级的线程池
    private static final ExecutorService priorityExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new PriorityBlockingQueue<>(),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "priority-task-thread-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            }
    );
    
    /**
     * 优先级任务包装类
     */
    @Data
    private static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        private final Runnable task;
        private final int priority;
        private final String description;
        
//        public PriorityTask(Runnable task, int priority, String description) {
//            this.task = task;
//            this.priority = priority;
//            this.description = description;
//        }
        
        @Override
        public void run() {
            task.run();
        }
        
        @Override
        public int compareTo(PriorityTask other) {
            // 优先级数值越小，优先级越高
            return Integer.compare(this.priority, other.priority);
        }
    }
    
    /**
     * 提交单个带优先级的任务
     * @param task 任务
     * @param priority 优先级
     * @param description 任务描述
     * @return Future对象
     */
    public static Future<?> submit(Runnable task, int priority, String description) {
        validatePriority(priority);
        PriorityTask priorityTask = new PriorityTask(task, priority, description);
        return priorityExecutor.submit(priorityTask);
    }
    
    /**
     * 提交单个带返回值且带优先级的任务
     * @param task 任务
     * @param priority 优先级
     * @param description 任务描述
     * @return Future对象
     */
    public static <T> Future<T> submit(Supplier<T> task, int priority, String description) {
        validatePriority(priority);
        return priorityExecutor.submit(() -> task.get());
    }
    
    /**
     * 批量提交任务并等待所有任务完成
     * @param tasks 任务列表
     * @param priority 优先级
     * @return 所有任务的Future
     */
    public static List<Future<?>> submitAllAndWait(List<Runnable> tasks, int priority) 
            throws InterruptedException, ExecutionException {
        return submitAllAndWait(tasks, priority, "批量任务");
    }
    
    /**
     * 批量提交任务并等待所有任务完成
     * @param tasks 任务列表
     * @param priority 优先级
     * @param batchDescription 批量任务描述
     * @return 所有任务的Future
     */
    public static List<Future<?>> submitAllAndWait(List<Runnable> tasks, int priority, String batchDescription) 
            throws InterruptedException, ExecutionException {
        
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        
        validatePriority(priority);
        
        System.out.printf("线程 %s 开始处理 %s，共 %d 个任务%n",
                Thread.currentThread().getName(),
                batchDescription,
                tasks.size());
        
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        
        for (int i = 0; i < tasks.size(); i++) {
            Runnable task = tasks.get(i);
            String taskDescription = String.format("%s - 任务 %d", batchDescription, i + 1);
            
            PriorityTask priorityTask = new PriorityTask(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    throw new RuntimeException("处理任务 [" + taskDescription + "] 出错", e);
                }
            }, priority, taskDescription);
            
            Future<?> future = priorityExecutor.submit(priorityTask);
            futures.add(future);
        }
        
        // 等待所有任务完成
        for (Future<?> future : futures) {
            future.get();
        }
        
        System.out.printf("线程 %s 完成处理 %s%n",
                Thread.currentThread().getName(),
                batchDescription);
        
        return futures;
    }
    
    /**
     * 验证优先级是否有效
     */
    private static void validatePriority(int priority) {
        if (priority < PRIORITY_HIGHEST || priority > PRIORITY_LOWEST) {
            throw new IllegalArgumentException("无效的优先级: " + priority + 
                    ", 必须在" + PRIORITY_HIGHEST + "到" + PRIORITY_LOWEST + "之间");
        }
    }
    
    /**
     * 关闭线程池
     */
    public static void shutdown() {
        priorityExecutor.shutdown();
    }
    
    /**
     * 立即关闭线程池
     */
    public static List<Runnable> shutdownNow() {
        return priorityExecutor.shutdownNow();
    }
}
