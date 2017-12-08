package com.sedsoftware.domain.entity.base

import com.sedsoftware.domain.entity.BaseEntity

class TopicInfoBlock(
    val topicTitle: String,
    val isClosed: Boolean,
    val authKey: String,
    val topicRating: Int,
    val topicRatingPlusAvailable: Boolean,
    val topicRatingMinusAvailable: Boolean,
    val topicRatingPlusClicked: Boolean,
    val topicRatingMinusClicked: Boolean,
    val topicRatingTargetId: Int
) : BaseEntity
