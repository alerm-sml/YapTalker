package com.sedsoftware.yaptalker.presentation.features.userprofile

import com.arellomobile.mvp.InjectViewState
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.interactor.GetUserProfile
import com.sedsoftware.yaptalker.presentation.base.BasePresenter
import com.sedsoftware.yaptalker.presentation.base.enums.ConnectionState
import com.sedsoftware.yaptalker.presentation.base.enums.lifecycle.PresenterLifecycle
import com.sedsoftware.yaptalker.presentation.mappers.UserProfileModelMapper
import com.sedsoftware.yaptalker.presentation.model.YapEntity
import com.sedsoftware.yaptalker.presentation.model.base.UserProfileModel
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class UserProfilePresenter @Inject constructor(
    private val getUserProfileUseCase: GetUserProfile,
    private val userProfileModelMapper: UserProfileModelMapper
) : BasePresenter<UserProfileView>() {

  fun loadUserProfile(profileId: Int) {
    getUserProfileUseCase
        .buildUseCaseObservable(GetUserProfile.Params(profileId))
        .subscribeOn(Schedulers.io())
        .map { profile: BaseEntity -> userProfileModelMapper.transform(profile) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setConnectionState(ConnectionState.LOADING) }
        .doOnError { setConnectionState(ConnectionState.ERROR) }
        .doOnComplete { setConnectionState(ConnectionState.COMPLETED) }
        .autoDisposable(event(PresenterLifecycle.DESTROY))
        .subscribe(getUserProfileObserver())
  }

  private fun getUserProfileObserver() =
      object : DisposableObserver<YapEntity?>() {

        override fun onNext(profile: YapEntity) {
          profile as UserProfileModel
          viewState.displayProfile(profile)
          viewState.updateCurrentUiState(profile.nickname)
        }

        override fun onComplete() {
          Timber.i("User profile loaded successfully.")

        }

        override fun onError(e: Throwable) {
          e.message?.let { viewState.showErrorMessage(it) }
        }
      }
}
