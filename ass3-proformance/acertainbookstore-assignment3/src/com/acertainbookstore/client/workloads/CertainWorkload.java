https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;  
import org.jfree.chart.axis.DateAxis;  
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;  
import org.jfree.data.time.TimeSeries;  
import org.jfree.data.time.TimeSeriesCollection;  
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import java.lang.Math;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numConcurrentWorkloadThreads = 10;
		String serverAddress = "http://localhost:8081";
		boolean localTest = false;
		List<WorkerRunResult> workerrunResults_local = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults_local = new ArrayList<Future<WorkerRunResult>>();
		List<WorkerRunResult> workerrunResults_rpc = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults_rpc = new ArrayList<Future<WorkerRunResult>>();

		// Initialize the RPC interfaces if its not a localTest, the variable is
		// overriden if the property is set
		String localTestProperty = System
				.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
		localTest = (localTestProperty != null) ? Boolean
				.parseBoolean(localTestProperty) : localTest;

		BookStore bookStore_local = null;
		StockManager stockManager_local = null;
		BookStore bookStore_rpc = null;
		StockManager stockManager_rpc = null;
//		if (localTest) {
//			CertainBookStore store = new CertainBookStore();
//			bookStore = store;
//			stockManager = store;
//		} else {
//			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
//			bookStore = new BookStoreHTTPProxy(serverAddress);
//		}
		CertainBookStore store = new CertainBookStore();
		bookStore_local = store;
		stockManager_local = store;
		stockManager_rpc = new StockManagerHTTPProxy(serverAddress + "/stock");
		bookStore_rpc = new BookStoreHTTPProxy(serverAddress);
		// Generate data in the bookstore before running the workload
		initializeBookStoreData(bookStore_local, stockManager_local);
		initializeBookStoreData(bookStore_rpc, stockManager_rpc);
		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);

		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			WorkloadConfiguration config = new WorkloadConfiguration(bookStore_local,
					stockManager_local);
			Worker workerTask1 = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults_local.add(exec.submit(workerTask1));
		}
		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			WorkloadConfiguration config = new WorkloadConfiguration(bookStore_rpc,
					stockManager_rpc);
			Worker workerTask2 = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults_rpc.add(exec.submit(workerTask2));
		}

		// Get the results from the threads using the futures returned
		for (Future<WorkerRunResult> futureRunResult : runResults_local) {
			WorkerRunResult runResult1 = futureRunResult.get(); // blocking call
			workerrunResults_local.add(runResult1);
		}
		for (Future<WorkerRunResult> futureRunResult : runResults_rpc) {
			WorkerRunResult runResult2 = futureRunResult.get(); // blocking call
			workerrunResults_rpc.add(runResult2);
		}

		exec.shutdownNow(); // shutdown the executor

		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore_rpc).stop();
			((StockManagerHTTPProxy) stockManager_rpc).stop();
		}

		reportMetric(workerrunResults_local, workerrunResults_rpc);
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerrunResults_local
	 */
	private static boolean check(List<WorkerRunResult> workerrunResults_local) {
		long all_custom = 0;
		long all_interaction = 0;
		long success_custom = 0;
		for (int i = 0; i < 10; i ++) {
			all_custom += workerrunResults_local.get(i).getTotalFrequentBookStoreInteractionRuns();
			success_custom += workerrunResults_local.get(i).getSuccessfulFrequentBookStoreInteractionRuns();
			all_interaction += workerrunResults_local.get(i).getTotalRuns();
		}
		float qc = (float)success_custom/all_custom;
		float q = (float)all_custom/all_interaction;
		if(q <= 0.65 && q >= 0.55 && 1 - qc <= 0.01) {
			return true;
		}else {
			return false;
		}
		
	}
	
	public static class draw_chart extends ApplicationFrame {

		   public draw_chart( String applicationTitle , String chartTitle ,DefaultCategoryDataset dataset ) {
		      super(applicationTitle);
		      JFreeChart lineChart = ChartFactory.createLineChart(
		         chartTitle,
		         "Number of threads","Agg lantency",
		         dataset,
		         PlotOrientation.VERTICAL,
		         true,true,false);
		         
		      ChartPanel chartPanel = new ChartPanel( lineChart );
		      chartPanel.setPreferredSize( new java.awt.Dimension( 1120 , 734 ) );
		      setContentPane( chartPanel );
		   }
		}	
	public static void reportMetric(List<WorkerRunResult> workerrunResults_local, List<WorkerRunResult> workerrunResults_rpc) {
		if(check(workerrunResults_local)) {
			System.out.println("LOCAL CHECK PASS! The customer interactions constitute roughly 60% of the total interactions processed and  only a small fraction (say < 1%) of\n" + 
					"the interactions are unsuccessful.");
		}else {
			System.out.println("LOCAL CHECK FAIL! the total throughput and the goodput are not close enough!!");
		}
		if(check(workerrunResults_rpc)) {
			System.out.println("RPC CHECK PASS! The customer interactions constitute roughly 60% of the total interactions processed and  only a small fraction (say < 1%) of\n" + 
					"the interactions are unsuccessful.");
		}else {
			System.out.println("RPC CHECK FAIL! the total throughput and the goodput are not close enough!!");
		}
		long lantency_local = 0;
		long lantency_rpc = 0;
		long success_interraction_local = 0;
		long success_interraction_rpc = 0;
		double container = 0;
		DefaultCategoryDataset lantency_data = new DefaultCategoryDataset( );
		DefaultCategoryDataset throughput_data = new DefaultCategoryDataset( );
		for(int i = 0; i < 10; i ++) {
			lantency_local += workerrunResults_local.get(i).getElapsedTimeInNanoSecs();
			lantency_rpc += workerrunResults_rpc.get(i).getElapsedTimeInNanoSecs();
			success_interraction_local += workerrunResults_local.get(i).getSuccessfulFrequentBookStoreInteractionRuns();
			success_interraction_rpc += workerrunResults_rpc.get(i).getSuccessfulFrequentBookStoreInteractionRuns();
			lantency_data.addValue( Math.log10(lantency_local) , "lantency (local test)" , Integer.toString(i + 1) );
			lantency_data.addValue( Math.log10(lantency_rpc) , "lantency (across different spaces)" , Integer.toString(i + 1) );
			
			container = Math.sqrt((double)success_interraction_local/lantency_local);
			throughput_data.addValue( container , "throughput (local test)" , Integer.toString(i + 1) );
			container = Math.sqrt((double)success_interraction_rpc/lantency_rpc);
			throughput_data.addValue( container , "throughput (across different spaces)" , Integer.toString(i + 1) );			
		}
		draw_chart lantency_chart = new draw_chart(
		         "Proformance" ,
		         "throughput(Sqrt) Vs threads",
		         throughput_data);
//		System.out.println(lantency);
		lantency_chart.pack( );
		RefineryUtilities.centerFrameOnScreen( lantency_chart );
		lantency_chart.setVisible( true );
		draw_chart throughput_chart = new draw_chart(
		         "Proformance" ,
		         "lantency(log) Vs threads",
		         lantency_data);

		throughput_chart.pack( );
		RefineryUtilities.centerFrameOnScreen( throughput_chart );
		throughput_chart.setVisible( true );

		// TODO: You should aggregate metrics and output them for plotting here
	}
	
	

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 */
	public static void initializeBookStoreData(BookStore bookStore,
			StockManager stockManager) throws BookStoreException {
		Set<StockBook> bookSet = BookSetGenerator.nextSetOfStockBooks(100);
		stockManager.addBooks(bookSet);
		Set<Integer> isbns= new HashSet<Integer>();
		Set<BookCopy> bookCopies = new HashSet<BookCopy>();
		for(StockBook obj:bookSet) {
			isbns.add(obj.getISBN());			
		}
		isbns = BookSetGenerator.sampleFromSetOfISBNs(isbns, 50);
		for(Integer isbn:isbns) {
			for(StockBook stockBook : bookSet) {
				if(stockBook.getISBN() == isbn) {
					BookCopy bookCopy = new BookCopy(isbn.intValue(), new Random().nextInt(stockBook.getNumCopies()));
					bookCopies.add(bookCopy);
				}
			}
		}
		bookStore.buyBooks(bookCopies);
		
		// TODO: You should initialize data for your bookstore here

	}
}
