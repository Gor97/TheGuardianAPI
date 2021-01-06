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
        author_info_layout.visibility = View.GONE
        if (newsList.isEmpty()) {
            showNews()
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
                    putExtra("list_result_id", it.id)
                }
                startActivity(intent)
            }
        news_feed_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    progressBar.visibility = View.VISIBLE
                    pageNumber++
                    showNews()
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

            newsList.add(news)
            localDB.newsDAO().insertNews(news)
        }
    }

    private fun showNews() {
        val extras = intent.extras
        if (extras != null && extras.containsKey("author_clicked_id")) {
            val result = extras.getString("author_clicked_id").toString()
            ServiceBuilder.buildService().getNewsFeedByAuthor(result)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    moveToLocalDB(it)
                    newsList = localDB.newsDAO().getAllNewsByAuthor(result)
                    getAuthorInfo(result)
                }
                .doOnError {
                    newsList = localDB.newsDAO().getAllNewsByAuthor(result)
                    getAuthorInfo(result)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<FeedResponse?>() {
                    override fun onNext(feedResponse: FeedResponse) {
                        initRecyclerView(newsList)
                        news_feed_recycler_view.adapter?.notifyDataSetChanged()
                        //progressBar.visibility = View.GONE
                        setAuthorLayout()
                        println(feedResponse)
                    }

                    override fun onError(e: Throwable) {
                        initRecyclerView(newsList)
                        //news_feed_recycler_view.adapter?.notifyDataSetChanged()

                        setAuthorLayout()
                        println("Network Error: Author")
                    }

                    override fun onComplete() { //onComplete not working when internet is off
                        println("Done!")
                    }
                })
        } else {
            ServiceBuilder.buildService().getNewsFeed(pageNumber)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    newsList.clear()
                    moveToLocalDB(it)
                }
                .doOnError {
                    newsList.clear()
                    newsList = localDB.newsDAO().getAllNews()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<FeedResponse?>() {
                    override fun onNext(feedResponse: FeedResponse) {
                        Toast.makeText(this@MainActivity, "News from Web", Toast.LENGTH_SHORT)
                            .show()
                        //initRecyclerView(newsList)
                        news_feed_recycler_view.adapter?.notifyDataSetChanged()

                        println(feedResponse)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(this@MainActivity, "News from Cache", Toast.LENGTH_SHORT)
                            .show()
                        //initRecyclerView(newsList)
                        news_feed_recycler_view.adapter?.notifyDataSetChanged()
                        println("Error + ${e}")
                    }

                    override fun onComplete() { //onComplete not working when internet is off
                        println("Done!")
                    }
                })
        }

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
        if (extras != null && extras.containsKey("author_clicked_id")) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            finishAffinity()
        }
    }

}

//test of different calls.
/*   val call2: Call<ArticleResponse> = newsService.getArticle(news!!.response.results[0].id)
   val response2: Response<ArticleResponse> = call2.execute()
   article = response2.body()
   Log.d("RESPONSE getArticle", article?.response.toString())



   val call3: Call<FeedResponse> =
       newsService.getNewsFeedBySection(news!!.response.results[0].sectionId)
   val response3: Response<FeedResponse> = call3.execute()
   val newsBySection = response3.body()
   Log.d("RESPONSE newsBySection", newsBySection?.response.toString())




   val call4: Call<FeedResponse> =
       newsService.getNewsFeedByAuthor(article?.response?.content?.tags?.get(0)?.id.toString())
   val response4: Response<FeedResponse> = call4.execute()
   val newsByAuthor = response4.body()
   Log.d("RESPONSE newsByAuthor", newsByAuthor?.response.toString())



   val news: News = ApiToDBConverter.convertAPIToDBNews(article!!)
   val author: Author = ApiToDBConverter.convertAPIToDBAuthor(article!!)
   Log.d("NEWS", news.toString())
   Log.d("AUTHOR", author.toString())*/


//call example with coroutines
/*
        val call: Call<FeedResponse> = ServiceBuilder.buildService().getNewsFeed(pageNumber)
        pageNumber++
        call.enqueue(object : Callback<FeedResponse?> {
            override fun onResponse(
                call: Call<FeedResponse?>?,
                response: Response<FeedResponse?>
            ) {
                if (response.isSuccessful) {
                    GlobalScope.launch(Dispatchers.IO) {
                        response.body()?.let { news = it } ?: Log.e(
                            "Response error",
                            "Body is NULL"
                        )
                        localDB.newsDAO().deleteAllNews()
                        newsList = moveToLocalDB(news)
                        withContext(Dispatchers.Main) {
                            initRecyclerView(newsList)
                        }
                    }
                } else {
                    Log.e("Request error", response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<FeedResponse?>?, t: Throwable) {
                Log.e("Network error", t.toString())
                GlobalScope.launch(Dispatchers.IO) {
                    newsList = localDB.newsDAO().getAllNews()
                    withContext(Dispatchers.Main) {
                        initRecyclerView(newsList)
                    }
                }
            }
        })*/