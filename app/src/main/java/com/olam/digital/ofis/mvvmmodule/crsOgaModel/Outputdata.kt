package com.olam.digital.ofis.mvvmmodule.crsOgaModel

data class Outputdata(
    val crsQuestion: List<CrsQuestion>,
    val moduleassignments: List<Moduleassignment>,
    val modules: List<Module>,
    val responseType: List<ResponseType>
)