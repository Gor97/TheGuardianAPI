package com.example.theguardian.db.dbobjects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Subject(
    @PrimaryKey
    var id: String = "",
    var name: String = ""
) {
    override fun toString(): String {
        return "Subject(id='$id', name='$name')"
    }
}