package com.example.business.util;

import com.sunboat.adminWeb.business.utils.PriorityTaskUtils;
import lombok.var;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PriorityTaskUtilsTest {

    // 测试不同优先级任务的执行顺序
    @Test
    public void testTaskPriority() throws InterruptedException, ExecutionException {
        System.out.println("=== 测试任务优先级 ===");
        
        // 提交不同优先级的任务
        PriorityTaskUtils.submit(() -> {
            System.out.println("执行最高优先级任务 (PRIORITY_HIGHEST)");
        }, PriorityTaskUtils.PRIORITY_HIGHEST, "最高优先级任务");
        
        PriorityTaskUtils.submit(() -> {
            System.out.println("执行低优先级任务 (PRIORITY_LOW)");
        }, PriorityTaskUtils.PRIORITY_LOW, "低优先级任务");
        
        PriorityTaskUtils.submit(() -> {
            System.out.println("执行高优先级任务 (PRIORITY_HIGH)");
        }, PriorityTaskUtils.PRIORITY_HIGH, "高优先级任务");
        
        PriorityTaskUtils.submit(() -> {
            System.out.println("执行最低优先级任务 (PRIORITY_LOWEST)");
        }, PriorityTaskUtils.PRIORITY_LOWEST, "最低优先级任务");
        
        PriorityTaskUtils.submit(() -> {
            System.out.println("执行普通优先级任务 (PRIORITY_NORMAL)");
        }, PriorityTaskUtils.PRIORITY_NORMAL, "普通优先级任务");
        
        // 等待所有任务完成（通过提交一个最后执行的任务并等待它）
        PriorityTaskUtils.submit(() -> {
            System.out.println("所有任务执行完毕");
        }, PriorityTaskUtils.PRIORITY_LOWEST, "结束标记任务").get();
    }

    // 测试批量提交任务
    @Test
    public void testBatchSubmit() throws InterruptedException, ExecutionException {
        System.out.println("\n=== 测试批量提交任务 ===");
        
        // 创建10个任务
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int taskNum = i + 1;
            tasks.add(() -> {
                try {
                    // 模拟任务执行时间
                    Thread.sleep(100);
                    System.out.println("批量任务 " + taskNum + " 执行完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("任务被中断", e);
                }
            });
        }
        
        // 批量提交并等待所有完成
        PriorityTaskUtils.submitAllAndWait(tasks, PriorityTaskUtils.PRIORITY_NORMAL, "测试批量任务");
    }

    // 测试带返回值的任务
    @Test
    public void testTaskWithResult() throws InterruptedException, ExecutionException {
        System.out.println("\n=== 测试带返回值的任务 ===");
        
        // 提交带返回值的任务
        var future = PriorityTaskUtils.submit(() -> {
            // 模拟计算过程
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 100 + 200;
        }, PriorityTaskUtils.PRIORITY_HIGH, "加法计算任务");
        
        // 获取并打印结果
        System.out.println("任务返回结果: " + future.get());
    }

    // 测试任务异常处理
    @Test
    public void testTaskException() throws InterruptedException {
        System.out.println("\n=== 测试任务异常处理 ===");
        
        try {
            // 提交一个会抛出异常的任务
            var future = PriorityTaskUtils.submit(() -> {
                throw new RuntimeException("故意抛出的测试异常");
            }, PriorityTaskUtils.PRIORITY_NORMAL, "异常测试任务");
            
            future.get(); // 这里会抛出异常
        } catch (ExecutionException e) {
            System.out.println("捕获到预期异常: " + e.getCause().getMessage());
        }
    }

    // 测试无效优先级
    @Test
    public void testInvalidPriority() {
        System.out.println("\n=== 测试无效优先级 ===");
        
        try {
            // 提交一个优先级无效的任务
            PriorityTaskUtils.submit(() -> {
                System.out.println("这个任务不会被执行");
            }, 0, "无效优先级任务");
        } catch (IllegalArgumentException e) {
            System.out.println("捕获到预期异常: " + e.getMessage());
        }
    }

    // 测试完成后关闭线程池
    @AfterAll
    public static void cleanup() {
        System.out.println("\n=== 关闭线程池 ===");
        PriorityTaskUtils.shutdown();
    }
}
