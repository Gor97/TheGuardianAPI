package com.example.theguardian.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.theguardian.Constants
import com.example.theguardian.R
import com.example.theguardian.api.ServiceBuilder
import com.example.theguardian.api.feedmodel.FeedResponse
import com.example.theguardian.db.NewsDatabase
import com.example.theguardian.db.dbobjects.Author
import com.example.theguardian.db.dbobjects.News
import com.example.theguardian.db.dbobjects.Subject
import com.example.theguardian.recyclerview.CustomRecyclerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val localDB: NewsDatabase by lazy { NewsDatabase.getDatabase(this@MainActivity) }
    private var author = Author()
    private var newsList: MutableList<News> = mutableListOf()
    private var pageNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView(newsList)

        val extras = intent.extras
        if (extras != null && extras.containsKey(Constants.AUTHOR_CLICKED_ID)) {
            author_info_layout.visibility = View.VISIBLE
            val authorID = extras.getString(Constants.AUTHOR_CLICKED_ID).toString()
            loadAuthorNews(authorID)
        } else {
            author_info_layout.visibility = View.GONE
            loadHomePageNews()
        }
    }

    private fun initRecyclerView(list: MutableList<News>) {
        news_feed_recycler_view.layoutManager = LinearLayoutManager(this)
        news_feed_recycler_view.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        news_feed_recycler_view.adapter =
            CustomRecyclerAdapter(list, this@MainActivity) {
                val intent = Intent(this, ArticleActivity::class.java).apply {
                    putExtra(Constants.LIST_ITEM_CLICKED_ID, it.id)
                }
                startActivity(intent)
            }
        news_feed_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    progressBar.visibility = View.VISIBLE
                    pageNumber++
                    loadMoreNews(pageNumber)
                }
            }
        })

    }

    private fun moveToLocalDB(newsFeedResponse: FeedResponse) {
        val size: Int = newsFeedResponse.response.pageSize
        for (i in 0 until size) {
            val result = newsFeedResponse.response.results[i]
            val news = News(
                result.id,
                result.webTitle,
                "", // full text is yet empty
                result.webPublicationDate,
                "", //imageURL is yet empty.... need articleRequest in ArticleActivity
                result.webUrl,
                result.apiUrl,
                result.sectionId,
                "" //authorID is yet empty
            )
            //this means that I have made a authorNews response so in FeedResponse I am getting extra TAG value
            if (newsFeedResponse.response.tag != null) {
                news.authorID = newsFeedResponse.response.tag.id
            }
            val subject = Subject(result.sectionId, result.sectionName)
            localDB.subjectDAO().insertSubject(subject)
            localDB.newsDAO().insertNews(news)
        }
    }

    private fun loadAuthorNews(authorID: String) {
        ServiceBuilder.buildService().getNewsFeedByAuthor(authorID)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                moveToLocalDB(it)
                newsList.addAll(localDB.newsDAO().getAllNewsByAuthor(authorID))
                getAuthorInfo(authorID)
            }
            .doOnError {
                newsList.addAll(localDB.newsDAO().getAllNewsByAuthor(authorID))
                getAuthorInfo(authorID)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<FeedResponse?>() {
                override fun onNext(feedResponse: FeedResponse) {
                    news_feed_recycler_view.adapter?.notifyDataSetChanged()
                    progressBar.visibility = View.GONE
                    setAuthorLayout()
                    println(feedResponse)
                }

                override fun onError(e: Throwable) {
                    news_feed_recycler_view.adapter?.notifyDataSetChanged()
                    setAuthorLayout()
                }

                override fun onComplete() {
                    println("Done!")
                }
            })
    }

    private fun loadHomePageNews(pageNumber: Int = 1) {
        ServiceBuilder.buildService().getNewsFeed(pageNumber)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                newsList.clear()
                moveToLocalDB(it)
                newsList.addAll(localDB.newsDAO().getAllNews())
            }
            .doOnError {
                newsList.clear()
                newsList.addAll(localDB.newsDAO().getAllNews())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<FeedResponse?>() {
                override fun onNext(feedResponse: FeedResponse) {
                    Toast.makeText(this@MainActivity, "News from Web", Toast.LENGTH_SHORT)
                        .show()
                    news_feed_recycler_view.adapter?.notifyDataSetChanged()
                    println(feedResponse)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(this@MainActivity, "News from Cache", Toast.LENGTH_SHORT)
                        .show()
                    news_feed_recycler_view.adapter?.notifyDataSetChanged()
                }

                override fun onComplete() {
                    println("Done!")
                }
            })
    }

    private fun loadMoreNews(pageNumber: Int) {
        loadHomePageNews(pageNumber) // or author ????????
    }

    private fun setAuthorLayout() {
        author_info_layout.visibility = View.VISIBLE
        author_info_layout_bio.text =
            Html.fromHtml(author.shortDescription, Html.FROM_HTML_MODE_LEGACY)
        author_info_layout_bio.movementMethod = LinkMovementMethod.getInstance()
        author_info_layout_name.text = author.name
        Glide.with(this@MainActivity)
            .load(author.imageURL)
            .apply(RequestOptions().override(300, 300))
            .into(author_info_layout_image)

    }

    private fun getAuthorInfo(id: String) {
        author.imageURL = localDB.authorDAO().getAuthorByID(id).imageURL
        author.name = localDB.authorDAO().getAuthorByID(id).name
        author.shortDescription = localDB.authorDAO().getAuthorByID(id).shortDescription
    }

    override fun onBackPressed() {
        val extras = intent.extras
        if (extras != null && extras.containsKey(Constants.AUTHOR_CLICKED_ID)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            finishAffinity()
        }
    }

}