package com.sedsoftware.yaptalker.data.mappers

import com.sedsoftware.yaptalker.data.parsed.ForumPageParsed
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import io.reactivex.functions.Function
import java.util.ArrayList
import javax.inject.Inject

/**
 * Mapper class used to transform parsed forum page from the data layer into BaseEntity list in the domain layer.
 */
class ForumPageMapper @Inject constructor() : Function<ForumPageParsed, List<BaseEntity>> {

  companion object {
    private const val TOPICS_PER_PAGE = 30
  }

  override fun apply(from: ForumPageParsed): List<BaseEntity> {

    val result: MutableList<BaseEntity> = ArrayList(TOPICS_PER_PAGE)

    with(from) {
      result.add(
        com.sedsoftware.yaptalker.domain.entity.base.ForumInfoBlock(
          forumTitle = forumTitle,
          forumId = forumId.toInt()
        )
      )

      topics.forEach { topic ->
        result.add(
          com.sedsoftware.yaptalker.domain.entity.base.Topic(
            title = topic.title,
            link = topic.link,
            isPinned = topic.isPinned.isNotEmpty(),
            isClosed = topic.isClosed.isNotEmpty(),
            author = topic.author,
            authorLink = topic.authorLink,
            rating = topic.rating.toInt(),
            answers = topic.answers.toInt(),
            lastPostDate = topic.lastPostDate,
            lastPostAuthor = topic.lastPostAuthor
          )
        )
      }

      result.add(
        com.sedsoftware.yaptalker.domain.entity.base.NavigationPanel(
          currentPage = navigation.currentPage.toInt(),
          totalPages = navigation.totalPages.toInt()
        )
      )
    }

    return result
  }
}
