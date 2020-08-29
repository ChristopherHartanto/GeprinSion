package com.chcreation.geprin_sion.model

data class Kelurahan(
    var id: Long? = 0,
    var id_kecamatan: String? = "",
    var nama: String? = ""
)

data class KelurahanResponse(
    val kelurahan: List<Kelurahan>
)