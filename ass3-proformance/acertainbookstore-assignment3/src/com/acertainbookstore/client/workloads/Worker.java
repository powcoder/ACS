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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;





/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
    private WorkloadConfiguration configuration = null;
    private int numSuccessfulFrequentBookStoreInteraction = 0;
    private int numTotalFrequentBookStoreInteraction = 0;

    public Worker(WorkloadConfiguration config) {
	configuration = config;
    }

    /**
     * Run the appropriate interaction while trying to maintain the configured
     * distributions
     * 
     * Updates the counts of total runs and successful runs for customer
     * interaction
     * 
     * @param chooseInteraction
     * @return
     */
    private boolean runInteraction(float chooseInteraction) {
	try {
	    float percentRareStockManagerInteraction = configuration.getPercentRareStockManagerInteraction();
	    float percentFrequentStockManagerInteraction = configuration.getPercentFrequentStockManagerInteraction();

	    if (chooseInteraction < percentRareStockManagerInteraction) {
		runRareStockManagerInteraction();
	    } else if (chooseInteraction < percentRareStockManagerInteraction
		    + percentFrequentStockManagerInteraction) {
		runFrequentStockManagerInteraction();
	    } else {
		numTotalFrequentBookStoreInteraction++;
		runFrequentBookStoreInteraction();
		numSuccessfulFrequentBookStoreInteraction++;
	    }
	} catch (BookStoreException ex) {
	    return false;
	}
	return true;
    }

    /**
     * Run the workloads trying to respect the distributions of the interactions
     * and return result in the end
     */
    public WorkerRunResult call() throws Exception {
	int count = 1;
	long startTimeInNanoSecs = 0;
	long endTimeInNanoSecs = 0;
	int successfulInteractions = 0;
	long timeForRunsInNanoSecs = 0;

	Random rand = new Random();
	float chooseInteraction;

	// Perform the warmup runs
	while (count++ <= configuration.getWarmUpRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    runInteraction(chooseInteraction);
	}

	count = 1;
	numTotalFrequentBookStoreInteraction = 0;
	numSuccessfulFrequentBookStoreInteraction = 0;

	// Perform the actual runs
	startTimeInNanoSecs = System.nanoTime();
	while (count++ <= configuration.getNumActualRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    if (runInteraction(chooseInteraction)) {
		successfulInteractions++;
	    }
	}
	endTimeInNanoSecs = System.nanoTime();
	timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
	return new WorkerRunResult(successfulInteractions, timeForRunsInNanoSecs, configuration.getNumActualRuns(),
		numSuccessfulFrequentBookStoreInteraction, numTotalFrequentBookStoreInteraction);
    }

    /**
     * Runs the new stock acquisition interaction
     * 
     * @throws BookStoreException
     */
    private void runRareStockManagerInteraction() throws BookStoreException {
    	StockManager stockManager = configuration.getStockManager();
    	List<StockBook> books = stockManager.getBooks();
    	Set<StockBook> generatedBooks = BookSetGenerator.nextSetOfStockBooks(configuration.getNumBooksToAdd());
    	Set<StockBook> booksNewAdd = new HashSet<StockBook>();
    	for(StockBook book1:generatedBooks) {
    		int isnb1 = book1.getISBN();
    		for(StockBook book2: books) {
    			int isbn2 = book2.getISBN();
    			if(isnb1 != isbn2) {
    				booksNewAdd.add(book2);
    			}
    		}
    	}
    	stockManager.addBooks(booksNewAdd);
    	
    	// TODO: Add code for New Stock Acquisition Interaction
    }

    /**
     * Runs the stock replenishment interaction
     * 
     * @throws BookStoreException
     */
    private void runFrequentStockManagerInteraction() throws BookStoreException {
    	StockManager stockManager = configuration.getStockManager();
    	List<StockBook> books = stockManager.getBooks();
    	Set<BookCopy> books_tobuy = new HashSet<BookCopy>();
    	
    	int k = configuration.getNumBooksWithLeastCopies();
    	int copy_num = configuration.getNumAddCopies();
		Collections.sort(books, new Comparator<Object>() {  
            public int compare(Object o1, Object o2) {  
            	StockBook b1 = (StockBook) o1;  
            	StockBook b2 = (StockBook) o2;
                if (b1.getNumCopies() > b2.getNumCopies()) {  
                    return 1;  
                }  
                if (b1.getNumCopies() < b2.getNumCopies()) {  
                    return -1;  
                }  
                return 0;  
            }  
        });
		for( int i = 0; i < k; i++) {
			StockBook book = books.get(i);
			int isbn = book.getISBN();
			BookCopy bookCopy = new BookCopy(isbn, copy_num);
			books_tobuy.add(bookCopy);
		}
		stockManager.addCopies(books_tobuy);
    	// TODO: Add code for Stock Replenishment Interaction
    }

    /**
     * Runs the customer interaction
     * 
     * @throws BookStoreException
     */
    
//    private BookStore client ;
    private void runFrequentBookStoreInteraction() throws BookStoreException {
    	BookStore client = configuration.getBookStore();
    	List<Book> bookToPick = client.getEditorPicks(50);
    	Set<Integer> isbnSet = new HashSet<>();
    	Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
    	for(Book book:bookToPick) {
    		isbnSet.add(book.getISBN());
    	}
    	isbnSet = BookSetGenerator.sampleFromSetOfISBNs(isbnSet, 20);
    	for(Integer isbn:isbnSet) {
    		BookCopy book = new BookCopy(isbn, 1);
    		booksToBuy.add(book);
    	}
    	client.buyBooks(booksToBuy);
    	// TODO: Add code for Customer Interaction
    }

}
