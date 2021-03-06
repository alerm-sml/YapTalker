package com.sedsoftware.yaptalker.presentation.features.search

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.sedsoftware.yaptalker.R
import com.sedsoftware.yaptalker.commons.annotation.LayoutResource
import com.sedsoftware.yaptalker.presentation.base.BaseFragment
import com.sedsoftware.yaptalker.presentation.base.enums.lifecycle.FragmentLifecycle
import com.sedsoftware.yaptalker.presentation.base.enums.navigation.NavigationSection
import com.sedsoftware.yaptalker.presentation.extensions.setIndicatorColorScheme
import com.sedsoftware.yaptalker.presentation.extensions.toastError
import com.sedsoftware.yaptalker.presentation.features.search.adapters.SearchResultsAdapter
import com.sedsoftware.yaptalker.presentation.features.search.adapters.SearchResultsItemClickListener
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.utility.InfiniteScrollListener
import com.uber.autodispose.kotlin.autoDisposable
import kotlinx.android.synthetic.main.fragment_site_search_results.*
import javax.inject.Inject

@LayoutResource(value = R.layout.fragment_site_search_results)
class SearchResultsFragment : BaseFragment(), SearchResultsView, SearchResultsItemClickListener {

  companion object {
    fun getNewInstance(request: SearchRequest): SearchResultsFragment {
      val fragment = SearchResultsFragment()
      val args = Bundle()
      args.putParcelable(SEARCH_REQUEST_KEY, request)
      fragment.arguments = args
      return fragment
    }

    private const val SEARCH_REQUEST_KEY = "SEARCH_REQUEST_KEY"
  }

  @Inject
  lateinit var searchResultsAdapter: SearchResultsAdapter

  @Inject
  @InjectPresenter
  lateinit var presenter: SearchResultsPresenter

  @ProvidePresenter
  fun provideSearchResultsPresenter() = presenter

  private val searchRequest: SearchRequest by lazy {
    arguments?.getParcelable(SEARCH_REQUEST_KEY) as SearchRequest
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    with(search_results_list) {
      val linearLayout = LinearLayoutManager(context)
      layoutManager = linearLayout
      adapter = searchResultsAdapter
      setHasFixedSize(true)
      clearOnScrollListeners()

      addOnScrollListener(
        InfiniteScrollListener(
          func = { presenter.loadNextSearchResultsPage() },
          layoutManager = linearLayout,
          visibleThreshold = 6
        )
      )
    }

    search_refresh_layout.setIndicatorColorScheme()

    presenter.searchForFirstTime(searchRequest)

    RxSwipeRefreshLayout
      .refreshes(search_refresh_layout)
      .autoDisposable(event(FragmentLifecycle.DESTROY))
      .subscribe { search_refresh_layout?.isRefreshing = false }
  }

  override fun showLoadingIndicator() {
    search_refresh_layout?.isRefreshing = true
  }

  override fun hideLoadingIndicator() {
    search_refresh_layout?.isRefreshing = false
  }

  override fun showErrorMessage(message: String) {
    toastError(message)
  }

  override fun appendSearchResultsTopicItem(topic: YapEntity) {
    searchResultsAdapter.addResultsItem(topic)
  }

  override fun updateCurrentUiState() {
    presenter.setAppbarTitle(searchRequest.searchFor)
    presenter.setNavDrawerItem(NavigationSection.SITE_SEARCH)
  }

  override fun onSearchResultsItemClick(triple: Triple<Int, Int, Int>) {
    presenter.navigateToChosenTopic(triple)
  }
}
