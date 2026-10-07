package com.olam.digital.crs.model

import java.io.Serializable

// In your DAO file or a shared models file
// In your DAO or a shared models file
data class AnswerRow(
    val questionId: Long,
    val optionId: Long?,
    val valueText: String?,
    val valueNumber: Double?,
    val valueInt: Int?,
    val valueBoolean: Boolean?,
    val valueDate: Long?,
    val valueJson: String?,
    val fileUri: String?,
    val remoteUrl: String?,
    val updatedAt: Long?,
    val repeatIndex: Int? = null,   // NEW: distinguish repeated answers
    val repeaterId: Long? = null     // NEW: identify which repeater this belongs to
) : Serializable
