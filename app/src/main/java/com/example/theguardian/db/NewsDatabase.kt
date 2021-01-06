package com.example.theguardian.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.theguardian.db.dao.AuthorDAO
import com.example.theguardian.db.dao.NewsDAO
import com.example.theguardian.db.dao.SubjectDAO
import com.example.theguardian.db.dbobjects.Author
import com.example.theguardian.db.dbobjects.News
import com.example.theguardian.db.dbobjects.Subject

@Database(entities = [News::class, Author::class, Subject::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDAO(): NewsDAO
    abstract fun authorDAO(): AuthorDAO
    abstract fun subjectDAO(): SubjectDAO

    private fun clearCache() {
        newsDAO().deleteAllNews()
        authorDAO().deleteAllAuthor()
        subjectDAO().deleteAllSubject()
    }

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}