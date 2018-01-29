package com.sedsoftware.yaptalker.presentation.features.activetopics

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup
import com.sedsoftware.yaptalker.domain.device.Settings
import com.sedsoftware.yaptalker.presentation.base.adapters.NavigationPanelClickListener
import com.sedsoftware.yaptalker.presentation.base.adapters.YapEntityDelegateAdapter
import com.sedsoftware.yaptalker.presentation.base.adapters.delegates.ActiveTopicsDelegateAdapter
import com.sedsoftware.yaptalker.presentation.base.adapters.delegates.NavigationPanelDelegateAdapter
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.model.YapEntityTypes
import com.sedsoftware.yaptalker.presentation.model.base.ActiveTopicModel
import java.util.ArrayList
import javax.inject.Inject

class ActiveTopicsAdapter @Inject constructor(
  clickListener: NavigationPanelClickListener,
  presenter: ActiveTopicsPresenter,
  settings: Settings
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var items: ArrayList<YapEntity>
  private var delegateAdapters = SparseArrayCompat<YapEntityDelegateAdapter>()

  init {
    delegateAdapters.put(YapEntityTypes.NAVIGATION_PANEL_ITEM, NavigationPanelDelegateAdapter(clickListener))
    delegateAdapters.put(YapEntityTypes.ACTIVE_TOPIC_ITEM, ActiveTopicsDelegateAdapter(presenter, settings))
    items = ArrayList()

    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    delegateAdapters.get(viewType).onCreateViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    delegateAdapters.get(getItemViewType(position)).onBindViewHolder(holder, items[position])
  }

  override fun getItemCount(): Int = items.size

  override fun getItemViewType(position: Int): Int = items[position].getBaseEntityType()

  override fun getItemId(position: Int): Long =
    (items[position] as? ActiveTopicModel)?.topicId?.toLong() ?: position.toLong()

  fun addActiveTopicItem(item: YapEntity) {
    val insertPosition = items.size
    items.add(item)
    notifyItemInserted(insertPosition)
  }

  fun clearActiveTopics() {
    notifyItemRangeRemoved(0, items.size)
    items.clear()
  }
}
