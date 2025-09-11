package com.sunboat.adminWeb.business.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class PriorityBasedFileProcessor {
    // 线程池核心大小，根据CPU核心数调整
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors() * 3;
    
    // 单线程池，使用优先级队列
    private static final ExecutorService executor = new ThreadPoolExecutor(
        POOL_SIZE,
        POOL_SIZE,
        0L, TimeUnit.MILLISECONDS,
        new PriorityBlockingQueue<>(100, new TaskPriorityComparator()),
        new ThreadPoolExecutor.CallerRunsPolicy() // 任务满时让提交者执行，避免任务丢失
    );
    
    // 任务优先级定义：数值越小优先级越高
    public static final int PRIORITY_HIGH = 1;   // 外层文件夹处理任务
    public static final int PRIORITY_LOW = 2;    // 内层文件块处理任务
    
    /**
     * 带优先级的任务包装类
     */
    static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        private final Runnable task;
        private final int priority;
        private final String description; // 任务描述，用于调试
        
        public PriorityTask(Runnable task, int priority, String description) {
            this.task = task;
            this.priority = priority;
            this.description = description;
        }
        
        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e) {
                System.err.printf("任务 [%s] 执行失败: %s%n", description, e.getMessage());
            }
        }
        
        @Override
        public int compareTo(PriorityTask other) {
            // 优先级数值小的先执行
            return Integer.compare(this.priority, other.priority);
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 任务优先级比较器
     */
    static class TaskPriorityComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable o1, Runnable o2) {
            return ((PriorityTask) o1).compareTo((PriorityTask) o2);
        }
    }
    
    /**
     * 处理所有类文件夹下的二进制文件
     */
    public static void processBinaryFiles(String rootDirPath) throws InterruptedException, ExecutionException {
        File rootDir = new File(rootDirPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("根目录不存在或不是目录: " + rootDirPath);
        }
        
        File[] classDirectories = rootDir.listFiles(File::isDirectory);
        if (classDirectories == null || classDirectories.length == 0) {
            System.out.println("没有找到类文件夹");
            return;
        }
        
        List<Future<?>> futures = new ArrayList<>();
        
        // 提交外层高优先级任务
        for (File classDir : classDirectories) {
            String className = classDir.getName();
            File[] binaryFiles = classDir.listFiles(f -> f.isFile() && isBinaryFile(f.getName(), className));
            
            if (binaryFiles != null && binaryFiles.length > 0) {
                // 包装成高优先级任务
                PriorityTask task = new PriorityTask(() -> {
                    try {
                        processClassFiles(className, binaryFiles);
                    } catch (Exception e) {
                        throw new RuntimeException("处理类文件夹 [" + className + "] 出错", e);
                    }
                }, PRIORITY_HIGH, "处理类文件夹: " + className);
                
                Future<?> future = executor.submit(task);
                futures.add(future);
            }
        }
        
        // 等待所有外层任务完成
        for (Future<?> future : futures) {
            future.get();
        }
        
        System.out.println("所有文件处理完成");
    }
    
    /**
     * 处理某个类的所有二进制文件
     */
    private static void processClassFiles(String className, File[] binaryFiles) throws InterruptedException, ExecutionException {
        System.out.printf("线程 %s 开始处理类 %s 的 %d 个文件%n",
                Thread.currentThread().getName(),
                className,
                binaryFiles.length);
        
        List<Future<?>> fileFutures = new ArrayList<>();
        
        for (File file : binaryFiles) {
            // 为每个文件创建处理任务（仍为高优先级，因为是文件夹任务的子任务）
            PriorityTask fileTask = new PriorityTask(() -> {
                try {
                    processSingleBinaryFile(className, file);
                } catch (Exception e) {
                    throw new RuntimeException("处理文件 [" + file.getName() + "] 出错", e);
                }
            }, PRIORITY_HIGH, "处理文件: " + file.getName());
            
            Future<?> future = executor.submit(fileTask);
            fileFutures.add(future);
        }
        
        // 等待当前类的所有文件处理完成
        for (Future<?> future : fileFutures) {
            future.get();
        }
        
        System.out.printf("线程 %s 完成处理类 %s%n",
                Thread.currentThread().getName(),
                className);
    }
    
    /**
     * 处理单个二进制文件（内部提交低优先级子任务）
     */
    private static void processSingleBinaryFile(String className, File file) throws InterruptedException, ExecutionException, IOException {
        System.out.printf("开始处理文件: %s (大小: %d bytes)%n",
                file.getName(), file.length());
        
        // 块大小设置为1MB，可根据实际情况调整
        final int BLOCK_SIZE = 1024 * 1024;
        
        // 小文件直接处理，不拆分
        if (file.length() < BLOCK_SIZE * 2) {
            processFileBlock(className, file, 0, file.length());
            return;
        }
        
        // 大文件拆分成多个块，提交低优先级任务
        List<Future<?>> blockFutures = new ArrayList<>();
        long fileSize = file.length();
        long position = 0;
        
        while (position < fileSize) {
            long blockLength = Math.min(BLOCK_SIZE, fileSize - position);
            final long currentPosition = position;
            final long currentLength = blockLength;
            
            // 提交低优先级的文件块处理任务
            PriorityTask blockTask = new PriorityTask(() -> {
                try {
                    processFileBlock(className, file, currentPosition, currentLength);
                } catch (IOException e) {
                    throw new RuntimeException("处理文件块 [" + file.getName() + ":" + currentPosition + "] 出错", e);
                }
            }, PRIORITY_LOW, "处理文件块: " + file.getName() + "[" + currentPosition + "-" + (currentPosition + currentLength) + "]");
            
            Future<?> future = executor.submit(blockTask);
            blockFutures.add(future);
            position += blockLength;
        }
        
        // 等待所有文件块处理完成
        for (Future<?> future : blockFutures) {
            future.get();
        }
        
        System.out.printf("完成处理文件: %s%n", file.getName());
    }
    
    /**
     * 处理文件的一个块
     */
    private static void processFileBlock(String className, File file, long position, long length) throws IOException {
        try (FileChannel channel = new FileInputStream(file).getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate((int) length);
            int bytesRead = channel.read(buffer, position);
            
            if (bytesRead > 0) {
                buffer.flip();
                
                // 这里是二进制数据的具体处理逻辑
                // 示例：简单统计特定字节出现次数
                int targetByte = 0x0A; // 换行符，仅作示例
                int count = 0;
                while (buffer.hasRemaining()) {
                    if (buffer.get() == targetByte) {
                        count++;
                    }
                }
                
                System.out.printf("线程 %s 处理 %s 的块 [%d-%d], 0x%02X 出现次数: %d%n",
                        Thread.currentThread().getName(),
                        file.getName(),
                        position, position + length - 1,
                        targetByte, count);
            }
        }
    }
    
    /**
     * 检查文件名是否符合"类名+随机数字"的格式
     */
    private static boolean isBinaryFile(String fileName, String className) {
        return fileName.matches("^" + Pattern.quote(className) + "\\d+$");
    }
    
    public static void main(String[] args) {
        String extractDir = "path/to/extracted/files"; // 替换为实际解压目录
        
        try {
            System.out.println("开始处理二进制文件...");
            long startTime = System.currentTimeMillis();
            
            processBinaryFiles(extractDir);
            
            long processTime = System.currentTimeMillis() - startTime;
            System.out.printf("所有文件处理完成，总耗时: %d ms%n", processTime);
            
        } catch (Exception e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 优雅关闭线程池
            executor.shutdown();
            try {
                // 等待现有任务完成
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    // 强制关闭
                    executor.shutdownNow();
                    // 等待强制关闭完成
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
    