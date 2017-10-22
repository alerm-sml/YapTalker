package com.sedsoftware.yaptalker.features.topic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.sedsoftware.yaptalker.R
import com.sedsoftware.yaptalker.base.BaseController
import com.sedsoftware.yaptalker.commons.extensions.hideBeyondScreenEdge
import com.sedsoftware.yaptalker.commons.extensions.scopeProvider
import com.sedsoftware.yaptalker.commons.extensions.setIndicatorColorScheme
import com.sedsoftware.yaptalker.commons.extensions.showFromScreenEdge
import com.sedsoftware.yaptalker.commons.extensions.toastError
import com.sedsoftware.yaptalker.data.model.TopicPost
import com.sedsoftware.yaptalker.features.posting.AddMessageActivity
import com.sedsoftware.yaptalker.features.userprofile.UserProfileController
import com.uber.autodispose.kotlin.autoDisposeWith
import kotlinx.android.synthetic.main.controller_chosen_topic.view.*


// TODO() Add share command
// TODO() Check if navigation can be replaced with fabs
class ChosenTopicController(val bundle: Bundle) : BaseController(bundle), ChosenTopicView {

  companion object {
    const val FORUM_ID_KEY = "FORUM_ID_KEY"
    const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    const val TOPIC_TITLE_KEY = "TOPIC_TITLE_KEY"
    const val MESSAGE_TEXT_KEY = "MESSAGE_TEXT_KEY"
    private const val INITIAL_FAB_OFFSET = 250f
    private const val MESSAGE_TEXT_REQUEST = 321
  }

  private val currentForumId: Int by lazy {
    bundle.getInt(FORUM_ID_KEY)
  }

  private val currentTopicId: Int by lazy {
    bundle.getInt(TOPIC_ID_KEY)
  }

  @InjectPresenter
  lateinit var topicPresenter: ChosenTopicPresenter

  private lateinit var topicAdapter: ChosenTopicAdapter
  private var isFabShown = true

  override val controllerLayoutId: Int
    get() = R.layout.controller_chosen_topic

  override fun onViewBound(view: View, savedViewState: Bundle?) {

    topicAdapter = ChosenTopicAdapter { userId ->
      val bundle = Bundle()
      bundle.putInt(UserProfileController.USER_ID_KEY, userId)
      router.pushController(
          RouterTransaction.with(UserProfileController(bundle))
              .pushChangeHandler(FadeChangeHandler())
              .popChangeHandler(FadeChangeHandler()))
    }

    topicAdapter.setHasStableIds(true)

    view.topic_refresh_layout.setIndicatorColorScheme()

    with(view.topic_posts_list) {
      val linearLayout = LinearLayoutManager(context)
      layoutManager = linearLayout
      adapter = topicAdapter

      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

      setHasFixedSize(true)
    }

    topicPresenter.checkSavedState(currentForumId, currentTopicId, savedViewState)
  }

  override fun subscribeViews(parent: View) {
    parent.topic_refresh_layout?.let {
      RxSwipeRefreshLayout
          .refreshes(parent.topic_refresh_layout)
          .autoDisposeWith(scopeProvider)
          .subscribe { topicPresenter.loadTopic(currentForumId, currentTopicId) }
    }

    parent.topic_posts_list?.let {
      RxRecyclerView
          .scrollEvents(parent.topic_posts_list)
          .autoDisposeWith(scopeProvider)
          .subscribe { event ->
            topicPresenter.handleFabVisibility(isFabShown, event.dy())
          }
    }

    parent.new_post_fab?.let {
      RxView
          .clicks(parent.new_post_fab)
          .autoDisposeWith(scopeProvider)
          .subscribe { topicPresenter.onFabClicked() }
    }
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    super.onSaveViewState(view, outState)
    val posts = topicAdapter.getPosts()
    if (posts.isNotEmpty()) {
      topicPresenter.saveCurrentState(outState, posts)
    }
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    view.topic_posts_list.adapter = null
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == MESSAGE_TEXT_REQUEST && resultCode == Activity.RESULT_OK) {
      data?.getStringExtra(MESSAGE_TEXT_KEY)?.let { message ->
        if (message.isNotEmpty()) {
          topicPresenter.sendMessage(message)
        }
      }
    }
  }

  override fun showErrorMessage(message: String) {
    toastError(message)
  }

  override fun showLoadingIndicator(shouldShow: Boolean) {
    view?.topic_refresh_layout?.isRefreshing = shouldShow
  }

  override fun refreshPosts(posts: List<TopicPost>) {
    topicAdapter.setPosts(posts)
  }

  override fun scrollToViewTop() {
    view?.topic_posts_list?.layoutManager?.scrollToPosition(0)
  }

  override fun showFab(shouldShow: Boolean) {

    if (shouldShow == isFabShown) {
      return
    }

    if (shouldShow) {
      view?.new_post_fab?.let { fab ->
        fab.showFromScreenEdge()
        isFabShown = true
      }
    } else {
      view?.new_post_fab?.let { fab ->
        val offset = fab.height + fab.paddingTop + fab.paddingBottom
        fab.hideBeyondScreenEdge(offset.toFloat())
        isFabShown = false
      }
    }
  }

  override fun hideFabWithoutAnimation() {
    view?.new_post_fab?.translationY = INITIAL_FAB_OFFSET
    isFabShown = false
  }

  override fun showAddMessageActivity(title: String) {
    view?.context?.let { ctx ->
      val activityIntent = Intent(ctx, AddMessageActivity::class.java)
      activityIntent.putExtra(TOPIC_TITLE_KEY, title)
      startActivityForResult(activityIntent, MESSAGE_TEXT_REQUEST)
    }
  }
}
