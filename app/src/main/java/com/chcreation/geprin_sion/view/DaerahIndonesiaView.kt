package com.chcreation.geprin_sion.view

import com.chcreation.geprin_sion.model.*

interface DaerahIndonesiaView {
    fun showProvinsi(provinsi: List<provinsi>)
    fun showKotaKabupaten(kotaKabupaten: List<KotaKabupaten>)
    fun showKecamatan(kecamatan: List<Kecamatan>)
    fun showKelurahan(kelurahan: List<Kelurahan>)
}