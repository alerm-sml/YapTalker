package com.sedsoftware.yaptalker.presentation.mappers

import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.entity.base.IncubatorItem
import com.sedsoftware.yaptalker.presentation.extensions.getLastDigits
import com.sedsoftware.yaptalker.presentation.mappers.util.DateTransformer
import com.sedsoftware.yaptalker.presentation.mappers.util.TextTransformer
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.model.base.IncubatorItemModel
import io.reactivex.functions.Function
import javax.inject.Inject

/**
 * Mapper class used to transform incubator entities list from the domain layer into YapEntity list
 * in the presentation layer.
 */
class IncubatorModelMapper @Inject constructor(
  private val textTransformer: TextTransformer,
  private val dateTransformer: DateTransformer
) : Function<BaseEntity, YapEntity> {

  override fun apply(item: BaseEntity): YapEntity {

    item as IncubatorItem

    return IncubatorItemModel(
      title = item.title,
      link = item.link,
      topicId = item.link.getLastDigits(),
      rating = textTransformer.transformRankToFormattedText(item.rating),
      images = item.images,
      videos = item.videos,
      videosRaw = item.videosRaw,
      author = item.author,
      authorLink = item.authorLink,
      date = dateTransformer.transformDateToShortView(item.date),
      forumName = textTransformer.transformNewsForumTitle(item.forumName),
      forumLink = item.forumLink,
      forumId = item.forumLink.getLastDigits(),
      comments = textTransformer.transformCommentsLabel(item.comments),
      cleanedDescription = textTransformer.transformHtmlToSpanned(item.cleanedDescription),
      isYapLink = item.link.contains("yaplakal") && !item.link.contains("/go/")
    )
  }
}
