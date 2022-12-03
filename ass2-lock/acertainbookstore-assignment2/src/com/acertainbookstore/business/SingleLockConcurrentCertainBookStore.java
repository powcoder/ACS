https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/** {@link SingleLockConcurrentCertainBookStore} implements the {@link BookStore} and
 * {@link StockManager} functionalities.
 * 
 * @see BookStore
 * @see StockManager
 */
public class SingleLockConcurrentCertainBookStore implements BookStore, StockManager {

	/** The mapping of books from ISBN to {@link BookStoreBook}. */
	private Map<Integer, BookStoreBook> bookMap = null;

	/**
	 * Instantiates a new {@link CertainBookStore}.
	 */
	public SingleLockConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<>();
	}

public ReadWriteLock lock = new ReentrantReadWriteLock();
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addBooks(java.util.Set)
	 */
	public void addBooks(Set<StockBook> bookSet) throws BookStoreException {
		this.lock.writeLock().lock();
		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check if all are there
		for (StockBook book : bookSet) {
			int isbn = book.getISBN();
			String bookTitle = book.getTitle();
			String bookAuthor = book.getAuthor();
			int noCopies = book.getNumCopies();
			float bookPrice = book.getPrice();

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isEmpty(bookTitle)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isEmpty(bookAuthor)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isInvalidNoCopies(noCopies)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (bookPrice < 0.0) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (bookMap.containsKey(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.DUPLICATED);
			}
		}

		for (StockBook book : bookSet) {
			int isbn = book.getISBN();
			bookMap.put(isbn, new BookStoreBook(book));
		}
		this.lock.writeLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addCopies(java.util.Set)
	 */
	public void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		int isbn;
		int numCopies;
		this.lock.writeLock().lock();
		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.NUM_COPIES + numCopies + BookStoreConstants.INVALID);
			}
		}

		BookStoreBook book;

		// Update the number of copies
		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			book = bookMap.get(isbn);
			book.addCopies(numCopies);
		}
		this.lock.writeLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooks()
	 */
	public List<StockBook> getBooks() {
		this.lock.readLock().lock();
		List<StockBook> listBooks = new ArrayList<>();
		Collection<BookStoreBook> bookMapValues = bookMap.values();
		
		for (BookStoreBook book : bookMapValues) {
			listBooks.add(book.immutableStockBook());
		}
		this.lock.readLock().unlock();
		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#updateEditorPicks(java.util
	 * .Set)
	 */
	public void updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException {
		// Check that all ISBNs that we add/remove are there first.
		this.lock.writeLock().lock();
		if (editorPicks == null) {
			this.lock.writeLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbnValue;

		for (BookEditorPick editorPickArg : editorPicks) {
			isbnValue = editorPickArg.getISBN();

			if (BookStoreUtility.isInvalidISBN(isbnValue)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbnValue)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (BookEditorPick editorPickArg : editorPicks) {
			bookMap.get(editorPickArg.getISBN()).setEditorPick(editorPickArg.isEditorPick());
		}
		this.lock.writeLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public void buyBooks(Set<BookCopy> bookCopiesToBuy) throws BookStoreException {
		this.lock.writeLock().lock();
		if (bookCopiesToBuy == null) {
			this.lock.writeLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int isbn;
		BookStoreBook book;
		Boolean saleMiss = false;

		Map<Integer, Integer> salesMisses = new HashMap<>();

		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			isbn = bookCopyToBuy.getISBN();

			if (bookCopyToBuy.getNumCopies() < 0) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(
						BookStoreConstants.NUM_COPIES + bookCopyToBuy.getNumCopies() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			book = bookMap.get(isbn);

			if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
				// If we cannot sell the copies of the book, it is a miss.
				salesMisses.put(isbn, bookCopyToBuy.getNumCopies() - book.getNumCopies());
				saleMiss = true;
			}
		}

		// We throw exception now since we want to see how many books in the
		// order incurred misses which is used by books in demand
		if (saleMiss) {
			for (Map.Entry<Integer, Integer> saleMissEntry : salesMisses.entrySet()) {
				book = bookMap.get(saleMissEntry.getKey());
				book.addSaleMiss(saleMissEntry.getValue());
			}
			this.lock.writeLock().unlock();
			throw new BookStoreException(BookStoreConstants.BOOK + BookStoreConstants.NOT_AVAILABLE);
		}

		// Then make the purchase.
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			book = bookMap.get(bookCopyToBuy.getISBN());
			book.buyCopies(bookCopyToBuy.getNumCopies());
		}
		this.lock.writeLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#getBooksByISBN(java.util.
	 * Set)
	 */
	public List<StockBook> getBooksByISBN(Set<Integer> isbnSet) throws BookStoreException {
		this.lock.readLock().lock();
		if (isbnSet == null) {
			this.lock.readLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<StockBook> listBooks = new ArrayList<>();

		for (Integer isbn : isbnSet) {
			this.lock.readLock().unlock();
			listBooks.add(bookMap.get(isbn).immutableStockBook());
		}
		this.lock.readLock().unlock();
		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getBooks(java.util.Set)
	 */
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
		this.lock.readLock().lock();
		if (isbnSet == null) {
			this.lock.readLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we rate are there to start with.
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<Book> listBooks = new ArrayList<>();

		for (Integer isbn : isbnSet) {
			listBooks.add(bookMap.get(isbn).immutableBook());
		}
		this.lock.readLock().unlock();
		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getEditorPicks(int)
	 */
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		this.lock.readLock().lock();
		if (numBooks < 0) {
			this.lock.readLock().unlock();
			throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
		}

		List<BookStoreBook> listAllEditorPicks = new ArrayList<>();
		List<Book> listEditorPicks = new ArrayList<>();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
		BookStoreBook book;

		// Get all books that are editor picks.
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = it.next();
			book = pair.getValue();

			if (book.isEditorPick()) {
				listAllEditorPicks.add(book);
			}
		}

		// Find numBooks random indices of books that will be picked.
		Random rand = new Random();
		Set<Integer> tobePicked = new HashSet<>();
		int rangePicks = listAllEditorPicks.size();

		if (rangePicks <= numBooks) {

			// We need to add all books.
			for (int i = 0; i < listAllEditorPicks.size(); i++) {
				tobePicked.add(i);
			}
		} else {

			// We need to pick randomly the books that need to be returned.
			int randNum;

			while (tobePicked.size() < numBooks) {
				randNum = rand.nextInt(rangePicks);
				tobePicked.add(randNum);
			}
		}

		// Get the numBooks random books.
		for (Integer index : tobePicked) {
			book = listAllEditorPicks.get(index);
			listEditorPicks.add(book.immutableBook());
		}
		this.lock.readLock().unlock();
		return listEditorPicks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getTopRatedBooks(int)
	 */
	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		this.lock.readLock().lock();
		if (numBooks < 0) {
			this.lock.readLock().unlock();
			throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
		}
		List<BookStoreBook> listAllRatedBooks = new ArrayList<>();
		List<Book> topRatedBooks = new ArrayList<>();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
		BookStoreBook book;
		// Get all books.
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = it.next();
			book = pair.getValue();
			listAllRatedBooks.add(book);
		}
		
		Collections.sort(listAllRatedBooks, new Comparator<Object>() {  
            public int compare(Object o1, Object o2) {  
                BookStoreBook b1 = (BookStoreBook) o1;  
            	BookStoreBook b2 = (BookStoreBook) o2;
                if (b1.getAverageRating() > b2.getAverageRating()) {  
                    return 1;  
                }  
                if (b1.getAverageRating() < b2.getAverageRating()) {  
                    return -1;  
                }  
                return 0;  
            }  
        });
//		System.out.println("sort finished");
		int min = numBooks;
		if (numBooks > listAllRatedBooks.size()) {
			min = listAllRatedBooks.size();
		}
//		System.out.println("minimum is: " + min + "~");
		for( int i = 0; i < min; i++) {
			book = listAllRatedBooks.get(i);
//			System.out.println(i);
//			System.out.println(book.getISBN());
//			System.out.println(book.getAverageRating());
			topRatedBooks.add(book.immutableBook());
//			Collections.reverse(topRatedBooks);
		}
//		System.out.println("add book succeed");
		this.lock.readLock().unlock();
		return topRatedBooks;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooksInDemand()
	 */
	@Override
	public List<StockBook> getBooksInDemand() throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#rateBooks(java.util.Set)
	 */
	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		this.lock.writeLock().lock();
		if (bookRating == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int isbn;
		int rating;
		BookStoreBook book;
		boolean invalid_rating;
//		System.out.println("before lock");		
//		System.out.println("after lock");
		for (BookRating bookSingleRating : bookRating) {
			this.lock.writeLock().lock();
			isbn = bookSingleRating.getISBN();
			rating = bookSingleRating.getRating();
//			System.out.println("bgggggggggggggggggggg");
//			System.out.println(isbn);
//			System.out.println(rating);
//			System.out.println("afterrrrrrrrrrrrrrrrrr");
			invalid_rating = BookStoreUtility.isInvalidRating(rating);
			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (invalid_rating) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.RATING + rating + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			book = bookMap.get(isbn);
			book.addRating(rating);
			this.lock.writeLock().unlock();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#removeAllBooks()
	 */
	public void removeAllBooks() throws BookStoreException {
		this.lock.writeLock().lock();
		bookMap.clear();
		this.lock.writeLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#removeBooks(java.util.Set)
	 */
	public void removeBooks(Set<Integer> isbnSet) throws BookStoreException {
		this.lock.writeLock().lock();
		if (isbnSet == null) {
			this.lock.writeLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				this.lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (int isbn : isbnSet) {
			bookMap.remove(isbn);
		}
		this.lock.writeLock().unlock();
	}
}
