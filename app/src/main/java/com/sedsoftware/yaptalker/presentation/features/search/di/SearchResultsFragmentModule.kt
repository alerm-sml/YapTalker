package com.sedsoftware.yaptalker.presentation.features.search.di

import com.sedsoftware.yaptalker.data.repository.YapSearchTopicsRepository
import com.sedsoftware.yaptalker.di.scopes.FragmentScope
import com.sedsoftware.yaptalker.domain.repository.SearchTopicsRepository
import com.sedsoftware.yaptalker.presentation.features.search.SearchResultsFragment
import com.sedsoftware.yaptalker.presentation.features.search.adapters.SearchResultsItemClickListener
import dagger.Binds
import dagger.Module

@Module
abstract class SearchResultsFragmentModule {

  @FragmentScope
  @Binds
  abstract fun searchRepository(repo: YapSearchTopicsRepository): SearchTopicsRepository

  @FragmentScope
  @Binds
  abstract fun searchResultsClickListener(fragment: SearchResultsFragment): SearchResultsItemClickListener
}
