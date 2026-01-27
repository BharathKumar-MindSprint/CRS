package com.olam.digital.crs.model

import androidx.annotation.Keep
import com.olam.digital.crs.room.CRSModuleEntity
import com.olam.digital.crs.room.CRSQuestionEntity
import com.olam.digital.crs.room.CRSResponseTypeDTO
import com.olam.digital.crs.room.ModuleAssignmentEntity
import java.io.Serializable


@Keep
data class CrsQuestionaryDataModel(
    val modules: MutableList<CRSModuleEntity?>? = null,
    val moduleassignments: MutableList<ModuleAssignmentEntity?>? = null,
    val responseType: MutableList<CRSResponseTypeDTO?>? = null,
    val crsQuestion: MutableList<CRSQuestionEntity?>? = null
) : Serializable
