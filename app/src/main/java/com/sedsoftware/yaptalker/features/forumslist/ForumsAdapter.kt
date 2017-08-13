package com.sedsoftware.yaptalker.features.forumslist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sedsoftware.yaptalker.R
import com.sedsoftware.yaptalker.data.model.ForumItem
import kotlinx.android.synthetic.main.controller_forums_list_item.view.*

class ForumsAdapter : RecyclerView.Adapter<ForumsAdapter.ForumsViewHolder>() {

  private var forumsList: ArrayList<ForumItem> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumsViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_forums_list_item,
        parent, false)
    return ForumsViewHolder(view)
  }

  override fun onBindViewHolder(holder: ForumsViewHolder, position: Int) {
    holder.bindTo(forumsList[position])
  }

  override fun getItemCount() = forumsList.size

  override fun getItemId(position: Int) = forumsList[position].id.toLong()

  fun addForumsList(list: List<ForumItem>) {
    forumsList.clear()
    forumsList.addAll(list)
    notifyDataSetChanged()
  }

  fun getForumsList() = forumsList

  class ForumsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindTo(forum: ForumItem) {
      with(itemView) {
        forum_name.text = forum.title
      }
    }
  }
}