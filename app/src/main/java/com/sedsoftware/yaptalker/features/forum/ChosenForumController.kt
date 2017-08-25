package com.sedsoftware.yaptalker.features.forum

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.jakewharton.rxbinding2.view.RxView
import com.sedsoftware.yaptalker.R
import com.sedsoftware.yaptalker.commons.extensions.scopeProvider
import com.sedsoftware.yaptalker.commons.extensions.setAppColorScheme
import com.sedsoftware.yaptalker.commons.extensions.stringRes
import com.sedsoftware.yaptalker.commons.extensions.toastError
import com.sedsoftware.yaptalker.commons.extensions.toastWarning
import com.sedsoftware.yaptalker.data.model.Topic
import com.sedsoftware.yaptalker.features.base.BaseController
import com.uber.autodispose.kotlin.autoDisposeWith
import kotlinx.android.synthetic.main.controller_chosen_forum.view.*
import kotlinx.android.synthetic.main.include_navigation_panel.view.*
import java.util.Locale

class ChosenForumController(val bundle: Bundle) : BaseController(bundle), ChosenForumView {

  companion object {
    private const val ANIMATION_DELAY = 100L
    private const val ANIMATION_DURATION = 250L
    private const val PANEL_OFFSET = 200f
    private const val TOPICS_LIST_KEY = "TOPICS_LIST"
    const val FORUM_ID_KEY = "FORUM_ID_KEY"
  }

  private val forumId: Int by lazy {
    bundle.getInt(FORUM_ID_KEY)
  }

  @InjectPresenter
  lateinit var forumPresenter: ChosenForumPresenter

  private lateinit var forumAdapter: ChosenForumAdapter

  override val controllerLayoutId: Int
    get() = R.layout.controller_chosen_forum

  override fun onViewBound(view: View, savedViewState: Bundle?) {

    forumAdapter = ChosenForumAdapter()
    forumAdapter.setHasStableIds(true)

    with(view.forum_refresh_layout) {
      setOnRefreshListener { forumPresenter.loadForum(forumId) }
      setAppColorScheme()
    }

    with(view.forum_topics_list) {
      val linearLayout = LinearLayoutManager(context)
      layoutManager = linearLayout
      adapter = forumAdapter

      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

      setHasFixedSize(true)
    }

    forumPresenter.checkSavedState(forumId, savedViewState, TOPICS_LIST_KEY)
  }

  override fun subscribeViews(parent: View) {

    val buttonPrevious = parent.navigation_go_previous

    buttonPrevious?.let {
      RxView
          .clicks(buttonPrevious)
          .autoDisposeWith(scopeProvider)
          .subscribe { forumPresenter.goToPreviousPage() }
    }

    val buttonNext = parent.navigation_go_next

    buttonNext?.let {
      RxView
          .clicks(buttonNext)
          .autoDisposeWith(scopeProvider)
          .subscribe { forumPresenter.goToNextPage() }
    }

    val pagesLabel = parent.navigation_pages_label

    pagesLabel?.let {
      RxView
          .clicks(pagesLabel)
          .autoDisposeWith(scopeProvider)
          .subscribe { forumPresenter.goToChosenPage() }
    }
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    super.onSaveViewState(view, outState)
    val topics = forumAdapter.getTopics()
    if (topics.isNotEmpty()) {
      outState.putParcelableArrayList(TOPICS_LIST_KEY, topics)
    }
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    view.forum_topics_list.adapter = null
  }

  override fun showRefreshing() {
    view?.forum_refresh_layout?.isRefreshing = true
  }

  override fun hideRefreshing() {
    view?.forum_refresh_layout?.isRefreshing = false
  }

  override fun showErrorMessage(message: String) {
    toastError(message)
  }

  override fun refreshTopics(topics: List<Topic>) {
    forumAdapter.setTopics(topics)
  }

  override fun setNavigationPagesLabel(page: Int, totalPages: Int) {

    val template = view?.context?.stringRes(R.string.navigation_pages_template) ?: ""

    if (template.isNotEmpty()) {
      view?.navigation_pages_label?.text = String.format(Locale.US, template, page, totalPages)
    }
  }

  override fun setIfNavigationBackEnabled(isEnabled: Boolean) {
    view?.navigation_go_previous?.isEnabled = isEnabled
  }

  override fun setIfNavigationForwardEnabled(isEnabled: Boolean) {
    view?.navigation_go_next?.isEnabled = isEnabled
  }

  override fun showGoToPageDialog(maxPages: Int) {

    view?.context?.let {
      MaterialDialog.Builder(it)
          .title(R.string.navigation_go_to_page_title)
          .inputType(InputType.TYPE_CLASS_NUMBER)
          .input(R.string.navigation_go_to_page_hint, 0, false, { _, input ->
            forumPresenter.loadChosenForumPage(input.toString().toInt())
          })
          .show()
    }
  }

  override fun showCantLoadPageMessage(page: Int) {
    val messageTemplate = view?.context?.stringRes(R.string.navigation_page_not_available)

    messageTemplate?.let {
      toastWarning(String.format(Locale.US, it, page))
    }
  }
}
