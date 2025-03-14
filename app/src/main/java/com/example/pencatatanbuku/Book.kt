package com.example.pencatatanbuku

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class Book(
    @PrimaryKey val barcode: String,
    val title: String,
    val author: String,
    val isbn: String,
    val publicationYear: Int,
    val stock: Int = 1,
    val imagePath: String? = null // Path gambar buku di penyimpanan lokal
)