package com.example.theguardian.db.dao

import androidx.room.*
import com.example.theguardian.db.dbobjects.News
import io.reactivex.Flowable

@Dao
interface NewsDAO {

    @Query("SELECT * FROM News ORDER BY datePublished DESC")
    fun getAllNews(): MutableList<News>

    @Query("SELECT * FROM News WHERE sectionID = :subject")
    fun getAllNewsBySubject(subject: String): MutableList<News>

    @Query("SELECT * FROM NEWS WHERE authorID = :authorID")
    fun getAllNewsByAuthor(authorID: String): MutableList<News>

    @Query("SELECT * FROM NEWS WHERE id = :newsID")
    fun getNewsByID(newsID: String): News?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNews(news: News)

    @Update
    fun updateNews(news: News)

    @Delete
    fun deleteNews(news: News)

    @Query("DELETE FROM News")
    fun deleteAllNews()
}