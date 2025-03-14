package com.example.pencatatanbuku

import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {
    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()
    suspend fun getBookByBarcode(barcode: String) = bookDao.getBookByBarcode(barcode)
    suspend fun insert(book: Book) = bookDao.insert(book)
    suspend fun update(book: Book) = bookDao.update(book)
    suspend fun delete(book: Book) = bookDao.delete(book)
}