package com.olam.digital.ofis.mvvmmodule.crsOgaModel

data class CrsQuestion(
    val helpText: String,
    val moduleId: Int,
    val moduleName: String,
    val moduleStatus: String,
    val options: List<Option>,
    val questionCode: String,
    val questionId: Int,
    val questionStatus: String,
    val questionText: String,
    val responseType: Int
)