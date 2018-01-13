package com.sedsoftware.yaptalker.data.repository

import com.sedsoftware.yaptalker.data.network.site.YapLoader
import com.sedsoftware.yaptalker.data.parsed.mappers.SettingsPageMapper
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.repository.SitePreferencesRepository
import io.reactivex.Observable
import javax.inject.Inject

class YapSitePreferencesRepository @Inject constructor(
    private val dataLoader: YapLoader,
    private val dataMapper: SettingsPageMapper
) : SitePreferencesRepository {

  companion object {
    private const val SETTINGS_ACT = "UserCP"
    private const val SETTINGS_CODE = "04"
  }

  override fun getSitePreferences(): Observable<BaseEntity> =
      dataLoader
          .loadSitePreferences(
              act = SETTINGS_ACT,
              code = SETTINGS_CODE
          )
          .map { parsedPrefsPage -> dataMapper.transform(parsedPrefsPage) }
}
