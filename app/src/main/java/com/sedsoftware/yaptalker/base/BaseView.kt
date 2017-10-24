package com.sedsoftware.yaptalker.base

import com.arellomobile.mvp.MvpView

interface BaseView : MvpView {

  fun showErrorMessage(message: String)

  @Suppress("EmptyFunctionBlock")
  fun showLoadingIndicator(shouldShow: Boolean) {
    // Default empty implementation
  }
}