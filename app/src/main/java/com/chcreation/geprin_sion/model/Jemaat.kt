package com.chcreation.geprin_sion.model

data class Jemaat(
    var NAMA: String? = "",
    var PROVINSI: String? = "",
    var KOTA: String? = "",
    var KECAMATAN: String? = "",
    var KELURAHAN: String? = "",
    var RT: String? = "",
    var RW: String? = "",
    var ALAMAT: String? = "",
    var GENDER: String? = "",
    var GOL_DARAH: String? = "",
    var TEMPAT_LAHIR: String? = "",
    var TANGGAL_LAHIR: String? = "",
    var NO_TEL: String? = "",
    var NOTE: String? = "",
    var BAPTIS: Boolean? = false,
    var NO_SERTIFIKAT: String? = "",
    var TEMPAT_BAPTIS: String? = "",
    var TANGGAL_BAPTIS: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = "",
    var IMAGE: String? = "",
    var ID: String? = "",
    var STATUS: String? = EStatusCode.ACTIVE.toString()

)