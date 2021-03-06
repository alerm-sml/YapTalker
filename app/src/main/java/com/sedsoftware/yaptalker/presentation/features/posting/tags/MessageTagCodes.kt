package com.sedsoftware.yaptalker.presentation.features.posting.tags

import android.support.annotation.IntDef

class MessageTagCodes {
  companion object {
    const val TAG_B = 0L
    const val TAG_I = 1L
    const val TAG_U = 2L
    const val TAG_LINK = 3L
    const val TAG_VIDEO = 4L
  }

  @Retention(AnnotationRetention.SOURCE)
  @IntDef(
    TAG_B,
    TAG_I,
    TAG_U,
    TAG_LINK,
    TAG_VIDEO
  )
  annotation class Tag
}
