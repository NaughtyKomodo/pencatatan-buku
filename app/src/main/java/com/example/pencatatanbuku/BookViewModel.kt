package com.example.pencatatanbuku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(val repository: BookRepository) : ViewModel() {
    val allBooks: StateFlow<List<Book>> = repository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun scanAndAddBook(barcode: String, bookDetails: Book? = null) = viewModelScope.launch {
        val existingBook = repository.getBookByBarcode(barcode)
        if (existingBook != null) {
            val updatedBook = existingBook.copy(stock = existingBook.stock + 1)
            repository.update(updatedBook)
        } else {
            bookDetails?.let {
                repository.insert(it.copy(barcode = barcode))
            }
        }
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }

    fun borrowBook(book: Book) = viewModelScope.launch {
        if (book.stock > 0) {
            val updatedBook = book.copy(stock = book.stock - 1)
            repository.update(updatedBook)
        }
    }

    fun returnBook(book: Book) = viewModelScope.launch {
        val updatedBook = book.copy(stock = book.stock + 1)
        repository.update(updatedBook)
    }
}