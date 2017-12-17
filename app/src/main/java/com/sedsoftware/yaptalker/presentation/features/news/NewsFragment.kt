package com.sedsoftware.yaptalker.presentation.features.news

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.sedsoftware.yaptalker.R
import com.sedsoftware.yaptalker.presentation.commons.InfiniteScrollListener
import com.sedsoftware.yaptalker.data.SettingsManager
import com.sedsoftware.yaptalker.presentation.base.BaseFragment
import com.sedsoftware.yaptalker.presentation.base.lifecycle.FragmentLifecycle
import com.sedsoftware.yaptalker.presentation.commons.extensions.moveWithAnimationAxisY
import com.sedsoftware.yaptalker.presentation.commons.extensions.setIndicatorColorScheme
import com.sedsoftware.yaptalker.presentation.commons.extensions.toastError
import com.sedsoftware.yaptalker.presentation.features.news.adapter.NewsAdapter
import com.sedsoftware.yaptalker.presentation.features.news.adapter.NewsItemClickListener
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.uber.autodispose.kotlin.autoDisposable
import kotlinx.android.synthetic.main.fragment_news.*
import javax.inject.Inject

class NewsFragment : BaseFragment(), NewsView, NewsItemClickListener {

  companion object {
    fun getNewInstance() = NewsFragment()
  }

  override val layoutId: Int
    get() = R.layout.fragment_news

  @Inject
  lateinit var settings: SettingsManager

  @Inject
  @InjectPresenter
  lateinit var newsPresenter: NewsPresenter

  @ProvidePresenter
  fun provideNewsPresenter() = newsPresenter

  private lateinit var newsAdapter: NewsAdapter
  private var isFabShown = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    newsAdapter = NewsAdapter(this, settings)
    newsAdapter.setHasStableIds(true)

    with(news_list) {
      val linearLayout = LinearLayoutManager(context)
      layoutManager = linearLayout
      adapter = newsAdapter

      setHasFixedSize(true)
      clearOnScrollListeners()

      addOnScrollListener(InfiniteScrollListener({
        newsPresenter.loadNews(loadFromFirstPage = false)
      }, linearLayout))
    }

    refresh_layout.setIndicatorColorScheme()

    subscribeViews()
  }

  override fun showErrorMessage(message: String) {
    toastError(message)
  }

  override fun appendNewsItem(entity: YapEntity) {
    newsAdapter.addNewsItem(entity)
  }

  override fun clearNewsList() {
    newsAdapter.clearNews()
  }

  override fun showLoadingIndicator() {
    refresh_layout?.isRefreshing = true
  }

  override fun hideLoadingIndicator() {
    refresh_layout?.isRefreshing = false
  }

  override fun showFab() {
    news_fab?.let { fab ->
      fab.moveWithAnimationAxisY(offset = 0f)
      isFabShown = true
    }
  }

  override fun hideFab() {
    news_fab?.let { fab ->
      val offset = fab.height + fab.paddingTop + fab.paddingBottom
      fab.moveWithAnimationAxisY(offset = offset.toFloat())
      isFabShown = false
    }
  }

  override fun onNewsItemClick(topicLink: String, forumLink: String) {
    // TODO () Navigate to chosen topic
  }

  private fun subscribeViews() {

    RxSwipeRefreshLayout
        .refreshes(refresh_layout)
        .autoDisposable(event(FragmentLifecycle.STOP))
        .subscribe { newsPresenter.loadNews(loadFromFirstPage = true) }

    RxRecyclerView
        .scrollEvents(news_list)
        .autoDisposable(event(FragmentLifecycle.STOP))
        .subscribe { event -> newsPresenter.handleFabVisibility(event.dy()) }

    RxView
        .clicks(news_fab)
        .autoDisposable(event(FragmentLifecycle.STOP))
        .subscribe { newsPresenter.loadNews(loadFromFirstPage = true) }
  }
}
