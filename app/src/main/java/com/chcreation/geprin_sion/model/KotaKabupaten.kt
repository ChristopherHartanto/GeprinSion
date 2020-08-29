package com.chcreation.geprin_sion.model

data class KotaKabupaten(
    var id: Int? = 0,
    var id_provinsi: String? = "",
    var nama: String? = ""
)

data class KotaKabupatenResponse(
    val kota_kabupaten: List<KotaKabupaten>
)