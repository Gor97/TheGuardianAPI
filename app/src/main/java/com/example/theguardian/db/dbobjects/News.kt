package com.example.theguardian.db.dbobjects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class News(
    @PrimaryKey
    var id: String = "",
    var title: String = "",
    var fullText: String = "",
    var datePublished: String = "",
    var imageURL: String = "",

    var webURL: String = "",
    var apiURL: String = "",

    var sectionID: String = "",
    var authorID: String = ""


) {
    override fun toString(): String {
        return "News(id='$id', title='$title', fullText='$fullText', datePublished='$datePublished', imageURL='$imageURL', webURL='$webURL', apiURL='$apiURL', sectionID='$sectionID', authorID='$authorID')"
    }
}
