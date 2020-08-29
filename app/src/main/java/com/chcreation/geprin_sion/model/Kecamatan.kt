package com.chcreation.geprin_sion.model

data class Kecamatan(
    var id: Int? = 0,
    var id_kota: String? = "",
    var nama: String? = ""
)

data class KecamatanResponse(
    val kecamatan: List<Kecamatan>
)