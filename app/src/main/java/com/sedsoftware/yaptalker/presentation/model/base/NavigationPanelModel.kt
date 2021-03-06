package com.sedsoftware.yaptalker.presentation.model.base

import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.model.YapEntityTypes

/**
 * Class which represents navigation panel in presentation layer.
 */
class NavigationPanelModel(
  val currentPage: Int,
  val totalPages: Int,
  val navigationLabel: String
) : YapEntity {

  override fun getBaseEntityType(): Int = YapEntityTypes.NAVIGATION_PANEL_ITEM
}
