package com.sunboat.adminWeb.business.utils;

//public class ExecutorUtil<T> {
//
//    // 线程池
//    private final ExecutorService executorService;
//    // 每批处理的数据量
//    private final int batchSize;
//
//    private final ThreadPoolExecutor executor;
//
//
//
//
//    public static executBatch(int) {
//
//    }
//
//
//    public ExecutorUtil(ExecutorService executorService, int batchSize, ThreadPoolExecutor executor) {
//        this.executorService = executorService;
//        this.batchSize = batchSize;
//        this.executor = executor;
//    }
//
//    public void exeDemo(int corePoolSize,int maximumPoolSize,ThreadFactory threadFactory,RejectedExecutionHandler handler) {
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//                corePoolSize,
//                maximumPoolSize,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<>(),
//                new ThreadFactory() {
//                    private int counter = 1;
//
//                    @Override
//                    public Thread newThread(Runnable r) {
//                        return new Thread(r, "batch-insert-thread-" + counter++);
//                    }
//                },
//                new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//    }
//}
