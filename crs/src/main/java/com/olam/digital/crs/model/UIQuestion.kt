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
    val answer: Any? = null ,  // reactive value for binding
    val repeatIndex: Int? =null,
    val repeaterId: Long? = null,
    val childIndex: Int? = null,
    val childMainQuestionPosition:Int?=null
): Serializable

@Keep
data class AnswerKey(
    val questionId: Long,
    val repeaterId: Long? = null,
    val repeatIndex: Int? = null // null for non-repeater questions
): Serializable

enum class ContentType {
    PDF,
    DOCUMENT,
    TEXT,
    IMAGE
}

enum class ShowRecycler {
    SHOW_SHIMMER,
    SHOW_FIRST_RECYLCERVIEW,
    SHOW_SECOND_RECYLCERVIEW,
    SHOW_NODATA_RECYLCERVIEW
}

sealed class ConsentSource {
    data class Text(val content: String) : ConsentSource()
    data class FilePath(val path: String) : ConsentSource()
}

fun mapConsentType(type: String?, path: String?): ConsentSource {
    return when (type) {
        "text" -> ConsentSource.Text(path ?: "")
        "file", "document", "image" -> ConsentSource.FilePath(path ?: "")
        else -> ConsentSource.Text("") // fallback
    }
}
