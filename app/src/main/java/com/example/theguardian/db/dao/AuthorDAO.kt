package com.example.theguardian.db.dao

import androidx.room.*
import com.example.theguardian.db.dbobjects.Author

@Dao
interface AuthorDAO {

    @Query("SELECT * FROM Author")
    fun getAllAuthor(): List<Author>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAuthor(author: Author)

    @Update
    fun updateAuthor(author: Author)

    @Delete
    fun deleteAuthor(author: Author)

    @Query("SELECT * FROM Author where id = :id")
    fun getAuthorByID(id: String?): Author

    @Query("DELETE FROM Author")
    fun deleteAllAuthor()

}