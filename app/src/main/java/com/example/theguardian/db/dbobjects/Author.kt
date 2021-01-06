package com.example.theguardian.db.dbobjects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Author(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var shortDescription: String = "",
    var imageURL: String = "",

    var apiURL: String = "",
    var webURL: String = ""

) {
    override fun toString(): String {
        return "Author(id='$id', name='$name', shortDescription='$shortDescription', imageURL='$imageURL', apiURL='$apiURL', webURL='$webURL')"
    }
}
