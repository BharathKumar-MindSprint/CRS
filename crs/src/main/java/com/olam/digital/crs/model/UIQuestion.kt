package com.olam.digital.crs.model

import androidx.annotation.Keep
import com.olam.digital.crs.room.CRSQuestionEntity
import com.olam.digital.crs.room.CrsOptionEntity
import java.io.Serializable

@Keep
data class UIQuestion(
    val question: CRSQuestionEntity,
    val options: List<CrsOptionEntity>,
    val type: QuestionType
): Serializable


// ui/UIQuestionUi.kt
data class UIQuestionUi(
    val base: UIQuestion,     // your original UIQuestion
    val answer: Any? = null   // reactive value for binding
): Serializable