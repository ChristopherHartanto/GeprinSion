package com.chcreation.geprin_sion.model

data class User(
    var NAME: String? = "",
    var IMAGE: String? = "",
    var EMAIL: String? = "",
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
    var ACTIVE: Int? = 0,
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var STATUS: String? = EStatusUser.USER.toString()
)