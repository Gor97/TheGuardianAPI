package com.example.theguardian.recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theguardian.R
import com.example.theguardian.api.feedmodel.FeedResponse
import com.example.theguardian.db.NewsDatabase
import com.example.theguardian.db.dbobjects.News
import com.example.theguardian.db.dbobjects.Subject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomRecyclerAdapter(
    private val values: MutableList<News>,
    private val context: Context,
    private val onItemClickListener: (News) -> Unit
) :
    RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {
    private val localDB: NewsDatabase by lazy { NewsDatabase.getDatabase(context) }
    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.titleTextView?.text = values[holder.adapterPosition].title
        holder.dateTextView?.text = values[holder.adapterPosition].datePublished
        holder.sectionTextView?.text = values[holder.adapterPosition].sectionID
        GlobalScope.launch(Dispatchers.IO) {
            val subjectName = localDB.subjectDAO().getSectionNameByID(values[holder.adapterPosition].sectionID)
            withContext(Dispatchers.Main){
                holder.sectionTextView?.text = subjectName
            }
        }

        holder.itemView.setOnClickListener { onItemClickListener(values[holder.adapterPosition]) }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView? = null
        var sectionTextView: TextView? = null
        var dateTextView: TextView? = null
        init {
            titleTextView = itemView.findViewById(R.id.news_title_textView)
            sectionTextView = itemView.findViewById(R.id.news_section_textView)
            dateTextView = itemView.findViewById(R.id.news_date_textView)
        }
    }
}