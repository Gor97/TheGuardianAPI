package com.example.theguardian.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.theguardian.Constants
import com.example.theguardian.R
import com.example.theguardian.api.ServiceBuilder
import com.example.theguardian.api.articlemodel.ArticleResponse
import com.example.theguardian.db.NewsDatabase
import com.example.theguardian.db.dbobjects.Author
import com.example.theguardian.db.dbobjects.News
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_article.*


class ArticleActivity : AppCompatActivity() {

    private var title: String = ""
    private var text: String = ""
    private var url: String = ""
    private var authorName: String = ""
    private var authorID: String? = null

    private val localDB: NewsDatabase by lazy { NewsDatabase.getDatabase(this@ArticleActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        setAuthorClick()

        val newsID = intent.extras?.get(Constants.LIST_ITEM_CLICKED_ID) as String
        ServiceBuilder.buildService().getArticle(newsID)
            .subscribeOn(Schedulers.io())
            .doOnNext { itRes ->
                updateNewsInfo(newsID, itRes)
                localDB.newsDAO().getNewsByID(newsID)?.let {
                    text = it.fullText
                    url = it.imageURL
                    title = it.title
                    authorID = it.authorID
                    authorName = localDB.authorDAO().getAuthorByID(authorID).name
                } ?: run { println("No Internet") }
            }
            .doOnError {
                localDB.newsDAO().getNewsByID(newsID)?.let {
                    text = it.fullText
                    url = it.imageURL
                    authorID = it.authorID
                    authorName = localDB.authorDAO().getAuthorByID(it.authorID).name
                    title = it.title
                } ?: run { println("No Internet") }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<ArticleResponse?>() {
                override fun onComplete() {
                    println("Done!")

                }

                override fun onNext(articleResponse: ArticleResponse) {
                    println(articleResponse)
                    article_text_view.text = text
                    article_author_name.text = authorName
                    article_title.text = title
                    Glide.with(this@ArticleActivity)
                        .load(url)
                        .apply(RequestOptions().override(1200, 800))
                        .into(article_image_view)
                }

                override fun onError(e: Throwable) {
                    println("Network Error")
                    if(text.isNotEmpty()) {
                        article_text_view.text = text
                        article_author_name.text = authorName
                        article_title.text = title
                        Glide.with(this@ArticleActivity)
                            .load(url)
                            .apply(RequestOptions().override(800, 600))
                            .into(article_image_view)
                    } else {
                        article_text_view.text = "Can't get news, please check internet connection"
                    }
                }

            })
    }

    private fun updateNewsInfo(newsID: String, article: ArticleResponse) {
        val news: News? = localDB.newsDAO().getNewsByID(newsID)
        news?.fullText = article.response.content.fields.bodyText
        news?.imageURL = article.response.content.fields.thumbnail

        val authorInfo = article.response.content.tags[0]

        if (article.response.content.tags.isNotEmpty()) {
            news?.authorID = authorInfo.id //adding authorID to News

            //adding same author to Author table
            val author = Author(
                authorInfo.id,
                authorInfo.webTitle,
                authorInfo.bio,
                authorInfo.bylineImageUrl,
                authorInfo.apiUrl,
                authorInfo.webUrl
            )
            localDB.authorDAO().insertAuthor(author)
        }
        if (news != null) {
            localDB.newsDAO().updateNews(news)
        }
    }

    private fun setAuthorClick() {
        article_author_name.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(Constants.AUTHOR_CLICKED_ID, authorID)
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}