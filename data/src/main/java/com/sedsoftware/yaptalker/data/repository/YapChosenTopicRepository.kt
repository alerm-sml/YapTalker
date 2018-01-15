package com.sedsoftware.yaptalker.data.repository

import com.sedsoftware.yaptalker.data.network.site.YapLoader
import com.sedsoftware.yaptalker.data.parsed.mappers.EditedPostMapper
import com.sedsoftware.yaptalker.data.parsed.mappers.QuotedPostMapper
import com.sedsoftware.yaptalker.data.parsed.mappers.ServerResponseMapper
import com.sedsoftware.yaptalker.data.parsed.mappers.TopicPageMapper
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.repository.ChosenTopicRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class YapChosenTopicRepository @Inject constructor(
    private val dataLoader: YapLoader,
    private val dataMapper: TopicPageMapper,
    private val quoteMapper: QuotedPostMapper,
    private val editedPostMapper: EditedPostMapper,
    private val responseMapper: ServerResponseMapper
) : ChosenTopicRepository {

  companion object {
    private const val KARMA_ACT = "ST"
    private const val KARMA_CODE = "vote_post"
    private const val KARMA_TYPE_TOPIC = 1
    private const val KARMA_TYPE_POST = 0

    private const val POST_ACT = "Post"
    private const val POST_CODE = "03"
    private const val POST_MAX_FILE_SIZE = 512000
  }

  override fun getChosenTopic(forumId: Int, topicId: Int, startPostNumber: Int): Single<List<BaseEntity>> =
      dataLoader
          .loadTopicPage(forumId, topicId, startPostNumber)
          .map { parsedTopicPage -> dataMapper.transform(parsedTopicPage) }

  override fun requestPostTextAsQuote(forumId: Int, topicId: Int, targetPostId: Int): Single<BaseEntity> =
      dataLoader
          .loadTargetPostQuotedText(forumId, topicId, targetPostId)
          .map { quotedText -> quoteMapper.transform(quotedText) }

  override fun requestPostTextForEditing(forumId: Int, topicId: Int, targetPostId: Int, startingPost: Int): Single<BaseEntity> =
      dataLoader
          .loadTargetPostEditedText(forumId, topicId, targetPostId, startingPost)
          .map { editedText -> editedPostMapper.transform(editedText) }

  override fun requestKarmaChange(
      isTopic: Boolean, targetPostId: Int, targetTopicId: Int, diff: Int): Single<BaseEntity> {

    val karmaType = if (isTopic) KARMA_TYPE_TOPIC else KARMA_TYPE_POST

    return dataLoader
        .changeKarma(
            act = KARMA_ACT,
            code = KARMA_CODE,
            rank = diff,
            postId = targetPostId,
            topicId = targetTopicId,
            type = karmaType
        )
        .map { response -> responseMapper.transform(response) }
  }

  override fun requestPostKarmaChange(targetPostId: Int, targetTopicId: Int, diff: Int): Single<BaseEntity> =
      dataLoader
          .changeKarma(
              act = KARMA_ACT,
              code = KARMA_CODE,
              rank = diff,
              postId = targetPostId,
              topicId = targetTopicId,
              type = KARMA_TYPE_POST
          )
          .map { response -> responseMapper.transform(response) }

  override fun requestTopicKarmaChange(targetPostId: Int, targetTopicId: Int, diff: Int): Single<BaseEntity> =
      dataLoader
          .changeKarma(
              act = KARMA_ACT,
              code = KARMA_CODE,
              rank = diff,
              postId = targetPostId,
              topicId = targetTopicId,
              type = KARMA_TYPE_TOPIC
          )
          .map { response -> responseMapper.transform(response) }

  override fun requestMessageSending(
      targetForumId: Int, targetTopicId: Int, page: Int, authKey: String, message: String): Completable =
      dataLoader
          .postMessage(
              act = POST_ACT,
              code = POST_CODE,
              forum = targetForumId,
              topic = targetTopicId,
              st = page,
              enableemo = true,
              enablesig = true,
              authKey = authKey,
              postContent = message,
              maxFileSize = POST_MAX_FILE_SIZE,
              enabletag = 1
          )
          .map { response -> dataMapper.transform(response) }
          .toCompletable()
}
