package com.olam.digital.ofis.mvvmmodule.crsOgaModel

data class Moduleassignment(
    val active: Any,
    val app_id: Any,
    val childLabour: Boolean,
    val country_id: Int,
    val created_at: Any,
    val disabled_months: String,
    val farmerRoleId: List<Any>,
    val fkFarmerRoleId: Any,
    val id: Int,
    val isChildLabour: Boolean,
    val isFarmSurvey: Boolean,
    val isOfflineDownload: Any,
    val isPlotSurvey: Any,
    val module_id: Int,
    val moduleassignment_id: Int,
    val moduleassignment_type: String,
    val recurrence_frequency: Int,
    val recurrence_type: Any,
    val startDate: String,
    val surveyCode: Any,
    val surveyDuration: Int,
    val updated_at: Any,
    val view_module: Int
)