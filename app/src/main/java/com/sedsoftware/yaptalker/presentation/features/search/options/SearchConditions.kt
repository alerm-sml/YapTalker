package com.sedsoftware.yaptalker.presentation.features.search.options

import android.support.annotation.StringDef

class SearchConditions {
  companion object {
    const val ANY_WORD = "any"
    const val ALL_WORDS = "all"
    const val EXACT_PHRASE = "exact"
  }

  @Retention(AnnotationRetention.SOURCE)
  @StringDef(ANY_WORD, ALL_WORDS, EXACT_PHRASE)
  annotation class Value
}
