package com.chcreation.geprin_sion.api

import android.net.Uri
import com.chcreation.geprin_sion.BuildConfig

object DaerahIndonesiaDBApi {

    fun provinsi(): String {
        return Uri.parse(BuildConfig.BASE_URL).buildUpon()
            .appendPath("api")
            .appendPath("daerahindonesia")
            .appendPath("provinsi")
            .build()
            .toString()
    }

    fun kotaKabupaten(provinsiKey : String): String {
        return Uri.parse(BuildConfig.BASE_URL).buildUpon()
            .appendPath("api")
            .appendPath("daerahindonesia")
            .appendPath("kota")
            .appendQueryParameter("id_provinsi",provinsiKey)
            .build()
            .toString()
    }

    fun kecamatan(kotaKey : String): String {
        return Uri.parse(BuildConfig.BASE_URL).buildUpon()
            .appendPath("api")
            .appendPath("daerahindonesia")
            .appendPath("kecamatan")
            .appendQueryParameter("id_kota",kotaKey)
            .build()
            .toString()
    }

    fun kelurahan(kecamatanKey : String): String {
        return Uri.parse(BuildConfig.BASE_URL).buildUpon()
            .appendPath("api")
            .appendPath("daerahindonesia")
            .appendPath("kelurahan")
            .appendQueryParameter("id_kecamatan",kecamatanKey)
            .build()
            .toString()
    }
}