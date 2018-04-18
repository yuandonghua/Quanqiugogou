package com.shopnum1.distributionportal.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTools {

	/**储存正在执行的线程**/
//	private WorkerThread[] wrokerThread;
	private static Runnable runnable;
	/**循环执行线程**/
	private static Thread thread;
	/**当前正在执行的线程**/
	private static Runnable runningRunnable;
	/***获取当前系统cpu数目***/
	private int cpuNums = Runtime.getRuntime().availableProcessors();
	private static ThreadPoolTools threadTools;
	/**线程池对象**/
	private static ExecutorService executorService;
	/**用来控制执行线程的循环**/
	private static boolean runThread = true;
	/**用来判断工作线程是否有效**/
	private static boolean isRunning = true;
	/**线程集合，当每次获取不同线程池时需要清空**/
	private static List<Runnable> taskQueue;

	/***单例对象**/
	public static ThreadPoolTools getInstance(){
		if (threadTools == null) {
			threadTools = new ThreadPoolTools();
		}
		/**清空正在运行的线程**/
		runningRunnable = null;
		/**清空线程列表数据**/
		taskQueue = new LinkedList<Runnable>();
		/**初始化runnable对象**/
		if (runnable == null) {
			runnable = new Runnable() {
				@Override
				public void run() {
					startExecutorThreadPoolTask();
				}
			};
		}
		/**初始化执行操作线程**/
		if (thread == null) {
			thread = new Thread(runnable);
		}
		return threadTools;
	}

	/***私有化构造函数***/
	private ThreadPoolTools() {
		super();
	}
	
	/**
	 * @author Arnold
	 * @param thread_count 线程池容量
	 * @return 返回一个指定容量的线程池
	 * 该线程池是一个可重用固定线程数量的线程池，若所有线程处于活动状态，则新提交的附加任务将在有可用线程之前在队列等待
	 */
	public ExecutorService getFixedThreadPool(int thread_count){
		executorService = Executors.newFixedThreadPool(thread_count);
		taskQueue = new LinkedList<Runnable>();
		return executorService;
	}
	
	/**
	 * @author Arnold
	 * @return 返回一个使用单个worker线程Executor，以顺序执行任务
	 */
	public ExecutorService getSingleThreadExecutor(){
		executorService = Executors.newSingleThreadExecutor();
		taskQueue = new LinkedList<Runnable>();
		return executorService;
	}

	/**
	 * @author Arnold
	 * @return 返回一个缓存型线程池，用于执行生命周期很短的异步任务
	 */
	public ExecutorService getCachedThreadPool(){
		executorService = Executors.newCachedThreadPool();
		taskQueue = new LinkedList<Runnable>();
		return executorService;
	}
	
	/**
	 * @author Arnold
	 * 无参，执行已添加到队列的线程
	 */
	public void startExecutorThread(){
		/** 
		 * 之前代码，不能暂停线程池的执行
		 * wrokerThread = new WorkerThread[taskQueue.size()];
		 * for (int i = 0; i < wrokerThread.length; i++) {
		 *	 synchronized (taskQueue) {
		 *		 wrokerThread[i] = new WorkerThread();
		 *		 executorService.execute(wrokerThread[i]);
		 *	 }
		 * }
		 */
		isRunning = true;
		runThread = true;
		thread.start();
	}
	
	
	/**
	 * @author Arnold
	 * 让工作线程无效，停止执行的线程，并且清空线程列表
	 */
	public void stopWorkerThread(){
		isRunning = false;
		runThread = false;
		taskQueue.clear();
	}
	
	/**
	 * @author Arnold
	 * 暂停工作线程
	 */
	public void pauseWorkerThread(){
		runThread = false;
		isRunning = false;
	}
	
	/**
	 * @author Arnold
	 * 有参，直接执行传递过来的线程
	 */
	public void executorThread(Runnable task){
		executorService.execute(task);
	}
	
	/**
	 * 此方法添加单线程
	 * 给线程池添加线程，调用该方法后可调用无参执行方法，调用后会执行已添加的task
	 * @param task 
	 */
	public void addTask(Runnable task){
		if(task != null)
			taskQueue.add(task);
	}
	
	/**
	 * @author Arnold
	 * 此方法可添加一个线程集合
	 */
	public void addTask(List<Runnable> tasks){
		while (!tasks.isEmpty()) {
			taskQueue.add(tasks.remove(0));
		}
	}
	
	
	/**
	 * 开始执行线程方法 version:2
	 * 此方法缺陷：记录正在执行的线程
	 * 		如果使用FixedThreadPool之类可指定运行数量的线程池时，保存正在执行的线程只存在一个，并且执行是通过异步锁控制，只允许单个线程的执行
	 * 		当使用可指定运行数量线程池时，执行效果与SingleThreadExecutor相同，都是单次执行
	 * 		但运行时由于FixedThreadPool线程池可以同时运行多个线程，所以当前一个线程未执行完成时并且当前执行线程数小于线程池定义的数量时，下一个线程会进入到FixedThreadPool的运行列表中去
	 * 		运行时使用SingleThreadExecutor时，当前线程未执行完时会将后面的线程加入线程执行队列中，等待执行(此线程池是无界队列形线程池)
	 * @problem : 因为是开启了synchronized锁、所以每次添加的线程只会有一个
	 * 			   所以无论是SingleThreadExecutor或是FixedThreadPool线程池、都是单个线程执行的
	 * 			   如果线程执行速度慢、此锁运行一次后该线程还未执行完成，则SingleThreadExecutor与FixedThreadPool有区别
	 * 				区别：如果FixedThreadPool数量大于正在执行的数量，则新加入的线程会立刻开始执行，而SingleThreadExecutor则会将新加入的线程存放在线程队列等待执行
	 * 					如果使用CachedThreadPool，类似于SingleThreadExecutor，如果当前线程未执行完成则会将新加入的线程放入线程队列等待空闲线程，有空闲线程则复用
	 * @author Arnold
	 */
	private static void startExecutorThreadPoolTask(){
		// 定义在外层不用每次重新创建，达到复用效果 
		Runnable r = null;
		while(runThread){/**是否开启线程执行操作**/
			if (isRunning) {/**是否执行当前线程列表**/
				synchronized (runnable) {
					/**
					 * 判断是否允许执行，如果允许执行则判断线程是否为空
					 */
					if(isRunning && taskQueue.isEmpty()){// 如果不使用循环等待新线程可直接使用if判断
//					while (isRunning && taskQueue.isEmpty()) {
						/**
						 * 当线程列表执行完成后，等待新的线程进来，不推荐使用
						 * 此方法会无限循环等待新的线程，会占用较大的内存空间
						 */
					  /*try {
							taskQueue.wait(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						/**当线程列表执行完成后，跳出此方法**/
						return;
					}
					if (!taskQueue.isEmpty()) 
						/**取出第0个线程开始执行**/
						r = taskQueue.remove(0);
				}
				if (r != null) {
					synchronized (r) {/**保持单线程执行**/
						/**执行该线程**/
						executorService.execute(r);
						/**记录当前正在执行的线程**/
						runningRunnable = r;
					}
				}
				// 将此线程置空
				r = null;
			}
		}
	}
	
	/**
	 * @author Arnold
	 * @return 获取当前正在执行的线程
	 */
	public static Runnable getRunningRunnable(){
		return runningRunnable;
	}
	
}
