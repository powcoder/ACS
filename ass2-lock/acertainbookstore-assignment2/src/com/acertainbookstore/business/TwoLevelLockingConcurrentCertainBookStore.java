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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/** {@link TwoLevelLockingConcurrentCertainBookStore} implements the {@link BookStore} and
 * {@link StockManager} functionalities.
 * 
 * @see BookStore
 * @see StockManager
 */
public class TwoLevelLockingConcurrentCertainBookStore implements BookStore, StockManager {

	/** The mapping of books from ISBN to {@link BookStoreBook}. */
	private Map<Integer, BookStoreBook> bookMap = null;
	private Map<Integer, ReadWriteLock> bookLock = null;

	/**
	 * Instantiates a new {@link CertainBookStore}.
	 */
	public TwoLevelLockingConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<>();
		bookLock = new HashMap<>();
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
			this.lock.writeLock().unlock();
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
		this.lock.readLock().lock();
		int isbn;
		int numCopies;
		if (bookCopiesSet == null) {
			this.lock.readLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.NUM_COPIES + numCopies + BookStoreConstants.INVALID);
			}
		}

		BookStoreBook book;

		// Update the number of copies
		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}
			numCopies = bookCopy.getNumCopies();
			
			book = bookMap.get(isbn);
			book.addCopies(numCopies);
			ReadWriteLock lock = bookLock.get(isbn);
			lock.writeLock().unlock();
		}
		this.lock.readLock().unlock();
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
		this.lock.readLock().lock();
		if (editorPicks == null) {
			this.lock.readLock().unlock();
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbnValue;

		for (BookEditorPick editorPickArg : editorPicks) {
			isbnValue = editorPickArg.getISBN();
			if (BookStoreUtility.isInvalidISBN(isbnValue)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbnValue)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.NOT_AVAILABLE);
			}
			if(bookLock.containsKey(isbnValue)) {
				ReadWriteLock lock = bookLock.get(isbnValue);
				lock.writeLock().lock();
			}else {
				bookLock.put(isbnValue, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbnValue);
				lock.writeLock().lock();
			}
			bookMap.get(editorPickArg.getISBN()).setEditorPick(editorPickArg.isEditorPick());
			ReadWriteLock lock = bookLock.get(isbnValue);
			lock.writeLock().unlock();
		}

		this.lock.readLock().unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public void buyBooks(Set<BookCopy> bookCopiesToBuy) throws BookStoreException {
		this.lock.readLock().lock();
		if (bookCopiesToBuy == null) {
			this.lock.readLock().unlock();
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
				this.lock.readLock().unlock();
				throw new BookStoreException(
						BookStoreConstants.NUM_COPIES + bookCopyToBuy.getNumCopies() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.readLock().unlock();
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
			this.lock.readLock().unlock();
			throw new BookStoreException(BookStoreConstants.BOOK + BookStoreConstants.NOT_AVAILABLE);
		}
		// Then make the purchase.
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			book = bookMap.get(bookCopyToBuy.getISBN());
			isbn = book.getISBN();
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}
			
			book.buyCopies(bookCopyToBuy.getNumCopies());
			
			ReadWriteLock lock = bookLock.get(isbn);
			lock.writeLock().unlock();
		}
		this.lock.readLock().unlock();
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
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.readLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.readLock().lock();
			}
			
			listBooks.add(bookMap.get(isbn).immutableStockBook());
			
			ReadWriteLock lock = bookLock.get(isbn);
			lock.readLock().unlock();
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
		List<Book> listBooks = new ArrayList<>();
		
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
			if(bookLock.containsKey(ISBN)) {
				ReadWriteLock lock = bookLock.get(ISBN);
				lock.readLock().lock();
			}else {
				bookLock.put(ISBN, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(ISBN);
				lock.readLock().lock();
			}
			listBooks.add(bookMap.get(ISBN).immutableBook());
			
			ReadWriteLock lock = bookLock.get(ISBN);
			lock.readLock().unlock();
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
			int isbn = book.getISBN();
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.readLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.readLock().lock();
			}
			
			listEditorPicks.add(book.immutableBook());
			
			ReadWriteLock lock = bookLock.get(isbn);
			lock.readLock().unlock();
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
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
		}
		this.lock.readLock().lock();
		
		List<BookStoreBook> listAllRatedBooks = new ArrayList<>();
		List<Book> topRatedBooks = new ArrayList<>();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
		BookStoreBook book;
		// Get all books.
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = it.next();
			book = pair.getValue();
			int isbn = book.getISBN();
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}
			
			listAllRatedBooks.add(book);
			
			ReadWriteLock lock = bookLock.get(isbn);
			lock.writeLock().unlock();
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
		
		for( int i = 0; i < min; i++) {
			book = listAllRatedBooks.get(i);
			topRatedBooks.add(book.immutableBook());
			Collections.reverse(topRatedBooks);
		}
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
		if (bookRating == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int isbn;
		int rating;
		BookStoreBook book;
		boolean invalid_rating;
		this.lock.readLock().lock();
		for (BookRating bookSingleRating : bookRating) {
			isbn = bookSingleRating.getISBN();
			rating = bookSingleRating.getRating();
			invalid_rating = BookStoreUtility.isInvalidRating(rating);
			
			if (BookStoreUtility.isInvalidISBN(isbn)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (invalid_rating) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.RATING + rating + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				this.lock.readLock().unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			book = bookMap.get(isbn);
			
			if(bookLock.containsKey(isbn)) {
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}else {
				bookLock.put(isbn, new ReentrantReadWriteLock());
				ReadWriteLock lock = bookLock.get(isbn);
				lock.writeLock().lock();
			}
			
			book.addRating(rating);
			
			ReadWriteLock lock = bookLock.get(isbn);
			lock.writeLock().unlock();
		}
		this.lock.readLock().unlock();
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
