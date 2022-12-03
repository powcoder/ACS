https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package com.acertainbookstore.client.workloads;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public static Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		int size= isbns.size();
		if( num > size) {
			return isbns;
		}
		else {
			Set<Integer> isbnSelected = new HashSet<Integer>();
			for(int i=0; i<num; i++) {
				int item= new Random().nextInt(size);
				for(Integer obj: isbns) {
					if(i==item) 
						isbnSelected.add(obj);
				}
								
			}
			return isbnSelected;
				
		}
		
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public static Set<StockBook> nextSetOfStockBooks(int num) {
		Set<StockBook> immutableStockBooks = new HashSet<StockBook>();
		for(int i=0; i<= num; i++) {
			int isbn = new Random().nextInt(9999999);
//			-------------------
			String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	        StringBuilder title = new StringBuilder();
	        StringBuilder author= new StringBuilder();
	        Random rnd = new Random();
	        
	        while (title.length() < 20) { // length of the random string.
	            int index = (int) (rnd.nextFloat() * CHARS.length());
	            title.append(CHARS.charAt(index));
	        }
	        String titles = title.toString();
	        while (author.length() < 10) {
	        	int index = (int) (rnd.nextFloat() * CHARS.length());
	        	author.append(CHARS.charAt(index));
	        }
	        String authors = author.toString();
//			-------------------
	        float price = new Random().nextFloat() * (1000.0f-00.0f)+00.0f;
	        int copies = new Random().nextInt(9999999);
			StockBook book = new ImmutableStockBook(isbn, titles, authors, price, copies, 0, 0,
					0, true);
			immutableStockBooks.add(book);
		}
		return immutableStockBooks;
		
				
	}

}
