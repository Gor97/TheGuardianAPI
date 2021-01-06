package com.example.theguardian.db.dao

import androidx.room.*
import com.example.theguardian.db.dbobjects.Subject
import io.reactivex.Observable

@Dao
interface SubjectDAO {

    @Query("SELECT * FROM Subject")
    fun getAllSubjects(): List<Subject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubject(subject: Subject)

    @Update
    fun updateSubject(subject: Subject)

    @Delete
    fun deleteSubject(subject: Subject)

    @Query("SELECT name FROM Subject where id = :id")
    fun getSectionNameByID(id: String): String

    @Query("DELETE FROM Subject")
    fun deleteAllSubject()
}