package com.example.pencatatanbuku.ui.theme

import com.example.pencatatanbuku.Book
import com.example.pencatatanbuku.BookDao

class BookRepository(private val bookDao: BookDao) {
    suspend fun getAllBooks() = bookDao.getAllBooks()
    suspend fun getBookByBarcode(barcode: String) = bookDao.getBookByBarcode(barcode)
    suspend fun insert(book: Book) = bookDao.insert(book)
    suspend fun update(book: Book) = bookDao.update(book)
    suspend fun delete(book: Book) = bookDao.delete(book)
}