package com.chcreation.geprin_sion.model

data class provinsi(
    var id: Int? = 0,
    var nama: String? = ""
)

data class ProvinsiResponse(
    val provinsi: List<provinsi>
)