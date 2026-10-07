package com.olam.digital.crs.model

sealed class AnswerValue {
    data class Text(val value: String) : AnswerValue()
    data class Number(val value: Double) : AnswerValue()
    data class SingleOption(val optionId: Long) : AnswerValue()
    data class MultiOption(val optionIds: Set<Long>) : AnswerValue()
    data class Media(val uris: List<String>) : AnswerValue()  // photos/audio/video if any
    data object Empty : AnswerValue()
}