package com.sedsoftware.yaptalker.presentation.features.topic

import com.arellomobile.mvp.InjectViewState
import com.sedsoftware.yaptalker.domain.device.Settings
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.interactor.common.GetVideoThumbnail
import com.sedsoftware.yaptalker.domain.interactor.topic.GetChosenTopic
import com.sedsoftware.yaptalker.domain.interactor.topic.GetQuotedText
import com.sedsoftware.yaptalker.domain.interactor.topic.SendBookmarkAddRequest
import com.sedsoftware.yaptalker.domain.interactor.topic.SendChangeKarmaRequestPost
import com.sedsoftware.yaptalker.domain.interactor.topic.SendChangeKarmaRequestTopic
import com.sedsoftware.yaptalker.domain.interactor.topic.SendMessageRequest
import com.sedsoftware.yaptalker.presentation.base.BasePresenter
import com.sedsoftware.yaptalker.presentation.base.enums.ConnectionState
import com.sedsoftware.yaptalker.presentation.base.enums.lifecycle.PresenterLifecycle
import com.sedsoftware.yaptalker.presentation.base.enums.navigation.NavigationScreen
import com.sedsoftware.yaptalker.presentation.base.enums.navigation.RequestCode
import com.sedsoftware.yaptalker.presentation.mappers.QuotedPostModelMapper
import com.sedsoftware.yaptalker.presentation.mappers.ServerResponseModelMapper
import com.sedsoftware.yaptalker.presentation.mappers.TopicModelMapper
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.model.base.NavigationPanelModel
import com.sedsoftware.yaptalker.presentation.model.base.QuotedPostModel
import com.sedsoftware.yaptalker.presentation.model.base.ServerResponseModel
import com.sedsoftware.yaptalker.presentation.model.base.SinglePostModel
import com.sedsoftware.yaptalker.presentation.model.base.TopicInfoBlockModel
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class ChosenTopicPresenter @Inject constructor(
    private val router: Router,
    private val settings: Settings,
    private val getChosenTopicUseCase: GetChosenTopic,
    private val topicMapper: TopicModelMapper,
    private val getQuotedTextUseCase: GetQuotedText,
    private val quoteDataMapper: QuotedPostModelMapper,
    private val addToBookmarksUseCase: SendBookmarkAddRequest,
    private val changePostKarmaUseCase: SendChangeKarmaRequestPost,
    private val changeTopicKarmaUseCase: SendChangeKarmaRequestTopic,
    private val serverResponseMapper: ServerResponseModelMapper,
    private val sendMessageUseCase: SendMessageRequest,
    private val getVideoThumbnailUseCase: GetVideoThumbnail
) : BasePresenter<ChosenTopicView>() {

  companion object {
    private const val OFFSET_FOR_PAGE_NUMBER = 1
    private const val KARMA_SUCCESS_MARKER = "\"status\":1"
    private const val KARMA_ALREADY_CHANGED_MARKER = "\"status\":-1"
  }

  private val isScreenAlwaysActive: Boolean by lazy {
    settings.isScreenAlwaysOnEnabled()
  }

  private val postsPerPage = settings.getMessagesPerPage()
  private var currentForumId = 0
  private var currentTopicId = 0
  private var currentPage = 1
  private var totalPages = 1
  private var rating = 0
  private var ratingPlusAvailable = false
  private var ratingMinusAvailable = false
  private var ratingPlusClicked = false
  private var ratingMinusClicked = false
  private var ratingTargetId = 0
  private var authKey = ""
  private var isClosed = false
  private var currentTitle = ""
  private var clearCurrentList = false

  init {
    router.setResultListener(RequestCode.MESSAGE_TEXT, { message -> sendMessage(message as String) })
  }

  override fun onFirstViewAttach() {
    super.onFirstViewAttach()
    viewState.initiateTopicLoading()
  }

  override fun attachView(view: ChosenTopicView?) {
    super.attachView(view)

    if (isScreenAlwaysActive) {
      viewState.blockScreenSleep()
    }
  }

  override fun detachView(view: ChosenTopicView?) {
    if (isScreenAlwaysActive) {
      viewState.unblockScreenSleep()
    }

    super.detachView(view)
  }

  override fun onDestroy() {
    super.onDestroy()
    router.removeResultListener(RequestCode.MESSAGE_TEXT)
  }

  fun loadTopic(forumId: Int, topicId: Int, startingPost: Int = 0) {
    currentForumId = forumId
    currentTopicId = topicId

    currentPage = when {
      startingPost != 0 -> startingPost / postsPerPage + 1
      else -> 1
    }

    loadTopicCurrentPage(shouldScrollToViewTop = true)
  }

  fun refreshCurrentPage() {
    loadTopicCurrentPage(shouldScrollToViewTop = false)
  }

  fun goToFirstPage() {
    currentPage = 1
    loadTopicCurrentPage(shouldScrollToViewTop = true)
  }

  fun goToLastPage() {
    currentPage = totalPages
    loadTopicCurrentPage(shouldScrollToViewTop = true)
  }

  fun goToPreviousPage() {
    currentPage--
    loadTopicCurrentPage(shouldScrollToViewTop = true)
  }

  fun goToNextPage() {
    currentPage++
    loadTopicCurrentPage(shouldScrollToViewTop = true)
  }

  fun goToChosenPage(chosenPage: Int) {
    if (chosenPage in 1..totalPages) {
      currentPage = chosenPage
      loadTopicCurrentPage(shouldScrollToViewTop = true)
    } else {
      viewState.showCantLoadPageMessage(chosenPage)
    }
  }

  fun navigateToUserProfile(userId: Int) {
    router.navigateTo(NavigationScreen.USER_PROFILE_SCREEN, userId)
  }

  fun navigateToChosenVideo(html: String) {
    router.navigateTo(NavigationScreen.VIDEO_DISPLAY_SCREEN, html)
  }

  fun navigateToChosenImage(url: String) {
    router.navigateTo(NavigationScreen.IMAGE_DISPLAY_SCREEN, url)
  }

  fun navigateToChosenGif(url: String) {
    router.navigateTo(NavigationScreen.GIF_DISPLAY_SCREEN, url)
  }

  fun navigateToMessagePostingScreen() {
    router.navigateTo(NavigationScreen.ADD_MESSAGE_SCREEN, Pair(currentTitle, ""))
  }

  fun onUserProfileClicked(userId: Int) {
    if (authKey.isNotEmpty()) {
      viewState.showUserProfile(userId)
    }
  }

  fun shareCurrentTopic() {
    val startingPost = (currentPage - OFFSET_FOR_PAGE_NUMBER) * postsPerPage
    viewState.shareTopic(currentTitle, startingPost)
  }

  fun showPostKarmaMenuIfAvailable(postId: Int) {
    if (postId == 0 || authKey.isEmpty()) {
      return
    }

    viewState.showPostKarmaMenu(postId)
  }

  fun showTopicKarmaMenuIfAvailable() {
    if (ratingTargetId == 0 || authKey.isEmpty()) {
      return
    }

    viewState.showTopicKarmaMenu()
  }

  fun handleFabVisibility(diff: Int) {
    when {
      diff > 0 -> viewState.hideFab()
      diff < 0 -> viewState.showFab()
    }
  }

  fun onReplyButtonClicked(forumId: Int, topicId: Int, authorNickname: String, postDate: String, postId: Int) {
    getQuotedTextUseCase
        .execute(GetQuotedText.Params(forumId, topicId, postId))
        .subscribeOn(Schedulers.io())
        .map { quote: BaseEntity -> quoteDataMapper.transform(quote) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnSuccess { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe({ post ->
          post as QuotedPostModel
          val quote = "[QUOTE=$authorNickname,$postDate]${post.text}[/QUOTE]\n"
          router.navigateTo(NavigationScreen.ADD_MESSAGE_SCREEN, Pair(currentTitle, quote))
        }, { error ->
          error.message?.let { viewState.showErrorMessage(it) }
        })
  }


  fun addCurrentTopicToBookmarks() {

    val startingPost = (currentPage - OFFSET_FOR_PAGE_NUMBER) * postsPerPage

    addToBookmarksUseCase
        .execute(SendBookmarkAddRequest.Params(currentTopicId, startingPost))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnComplete { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe({
          // onComplete
          Timber.i("Current topic added to bookmarks.")
          viewState.showBookmarkAddedMessage()
        }, { error ->
          error.message?.let { viewState.showErrorMessage(it) }
        })
  }

  fun changeTopicKarma(shouldIncrease: Boolean) {

    if (authKey.isEmpty() || ratingTargetId == 0 || currentTopicId == 0) {
      return
    }

    val diff = if (shouldIncrease) 1 else -1

    changeTopicKarmaUseCase
        .execute(SendChangeKarmaRequestTopic.Params(ratingTargetId, currentTopicId, diff))
        .subscribeOn(Schedulers.io())
        .map { response: BaseEntity -> serverResponseMapper.transform(response) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnSuccess { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe(getKarmaResponseObserver(isTopic = true))
  }

  fun changePostKarma(postId: Int, shouldIncrease: Boolean) {
    if (authKey.isEmpty() || postId == 0 || currentTopicId == 0) {
      return
    }

    val diff = if (shouldIncrease) 1 else -1

    changePostKarmaUseCase
        .execute(SendChangeKarmaRequestPost.Params(postId, currentTopicId, diff))
        .subscribeOn(Schedulers.io())
        .map { response: BaseEntity -> serverResponseMapper.transform(response) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnSuccess { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe(getKarmaResponseObserver(isTopic = false))
  }

  fun requestThumbnail(videoUrl: String): Single<String> =
      getVideoThumbnailUseCase
          .execute(GetVideoThumbnail.Params(videoUrl))

  private fun sendMessage(message: String) {

    if (authKey.isEmpty()) {
      return
    }

    val startingPost = (currentPage - OFFSET_FOR_PAGE_NUMBER) * postsPerPage

    sendMessageUseCase
        .execute(SendMessageRequest.Params(currentForumId, currentTopicId, startingPost, authKey, message))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnComplete { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe({
          // onComplete
          Timber.i("Send message request completed.")
          loadTopicCurrentPage(shouldScrollToViewTop = false)
        }, { error ->
          // onError
          error.message?.let { viewState.showErrorMessage(it) }
        })
  }

  private fun loadTopicCurrentPage(shouldScrollToViewTop: Boolean) {

    if (!shouldScrollToViewTop) {
      viewState.saveScrollPosition()
    }

    val startingPost = (currentPage - OFFSET_FOR_PAGE_NUMBER) * postsPerPage
    clearCurrentList = true

    getChosenTopicUseCase
        .execute(GetChosenTopic.Params(currentForumId, currentTopicId, startingPost))
        .subscribeOn(Schedulers.io())
        .map { topicItems: List<BaseEntity> -> topicMapper.transform(topicItems) }
        .flatMapObservable { items: List<YapEntity> -> Observable.fromIterable(items) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnComplete { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe(getTopicObserver(shouldScrollToViewTop))
  }

  // ==== OBSERVERS ====

  private fun getKarmaResponseObserver(isTopic: Boolean) =
      object : SingleObserver<YapEntity?> {

        override fun onSubscribe(d: Disposable) {
        }

        override fun onSuccess(response: YapEntity) {
          response as ServerResponseModel

          when {
            response.text.contains(KARMA_SUCCESS_MARKER) -> viewState.showPostKarmaChangedMessage(isTopic)
            response.text.contains(KARMA_ALREADY_CHANGED_MARKER) -> viewState.showPostAlreadyRatedMessage(isTopic)
          }

          Timber.i("Karma changing request completed.")
          loadTopicCurrentPage(shouldScrollToViewTop = false)
        }

        override fun onError(e: Throwable) {
          e.message?.let { viewState.showErrorMessage(it) }
        }
      }

  private fun getTopicObserver(scrollToViewTop: Boolean) =
      object : DisposableObserver<YapEntity?>() {

        override fun onNext(item: YapEntity) {
          if (clearCurrentList) {
            clearCurrentList = false
            viewState.clearPostsList()
          }

          when (item) {
            is TopicInfoBlockModel -> {
              rating = item.topicRating
              ratingPlusAvailable = item.topicRatingPlusAvailable
              ratingMinusAvailable = item.topicRatingMinusAvailable
              ratingPlusClicked = item.topicRatingPlusClicked
              ratingMinusClicked = item.topicRatingMinusClicked
              ratingTargetId = item.topicRatingTargetId
              authKey = item.authKey
              isClosed = item.isClosed
              currentTitle = item.topicTitle
            }
            is NavigationPanelModel -> {
              currentPage = item.currentPage
              totalPages = item.totalPages
              viewState.appendPostItem(item)
            }
            is SinglePostModel -> {
              viewState.appendPostItem(item)
            }
          }
        }

        override fun onComplete() {
          Timber.i("Topic page loading completed.")
          viewState.updateCurrentUiState(currentTitle)
          setupCurrentLoginSessionState()

          if (scrollToViewTop) {
            viewState.scrollToViewTop()
          } else {
            viewState.restoreScrollPosition()
          }
        }

        override fun onError(error: Throwable) {
          error.message?.let { viewState.showErrorMessage(it) }
        }
      }

  // ==== UTILITY ====

  private fun setupCurrentLoginSessionState() {
    val loggedIn = authKey.isNotEmpty()
    val karmaAvailable = ratingPlusAvailable && ratingMinusAvailable
    viewState.setLoggedInState(loggedIn)
    viewState.setTopicKarmaState(karmaAvailable)
  }
}
