package com.sedsoftware.yaptalker.data.repository

import com.sedsoftware.yaptalker.data.mappers.ForumsListMapper
import com.sedsoftware.yaptalker.data.mappers.ListToObservablesMapper
import com.sedsoftware.yaptalker.data.network.site.YapLoader
import com.sedsoftware.yaptalker.domain.device.Settings
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.entity.base.Forum
import com.sedsoftware.yaptalker.domain.repository.ForumsListRepository
import io.reactivex.Observable
import javax.inject.Inject

class YapForumsListRepository @Inject constructor(
  private val dataLoader: YapLoader,
  private val dataMapper: ForumsListMapper,
  private val listMapper: ListToObservablesMapper,
  private val settings: Settings
) : ForumsListRepository {

  @Suppress("MagicNumber")
  companion object {
    private val nsfwForumSections = setOf(4, 33, 36)
  }

  override fun getMainForumsList(): Observable<BaseEntity> =
    dataLoader
      .loadForumsList()
      .map(dataMapper)
      .flatMap(listMapper)
      .filter { forumItem ->
        forumItem as Forum
        if (settings.isNsfwEnabled())
          true
        else
          !nsfwForumSections.contains(forumItem.forumId)
      }
}
