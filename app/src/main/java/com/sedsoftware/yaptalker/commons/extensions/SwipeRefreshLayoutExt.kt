package com.sedsoftware.yaptalker.commons.extensions

import android.support.v4.widget.SwipeRefreshLayout
import com.sedsoftware.yaptalker.R

fun SwipeRefreshLayout.setIndicatorColorScheme() {
  setColorSchemeColors(
      context.color(R.color.colorPrimary),
      context.color(R.color.colorPrimaryDark),
      context.color(R.color.colorAccent),
      context.color(R.color.colorAccentDark))
}