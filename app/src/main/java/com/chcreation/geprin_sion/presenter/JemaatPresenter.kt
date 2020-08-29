package com.chcreation.geprin_sion.presenter

import android.content.Context
import com.chcreation.geprin_sion.api.ApiRepository
import com.chcreation.geprin_sion.api.DaerahIndonesiaDBApi
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.util.*
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.lang.Exception

class JemaatPresenter(private val view: MainView, private val diView: DaerahIndonesiaView,
                      private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun createJemaat(jemaat: Jemaat){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var key = 0
                    if (p0.exists()){
                        for (data in p0.children){
                            key = data.key.toString().toInt() + 1
                            break
                        }
                    }

                    val values  = hashMapOf(
                        EJemaat.CREATED_DATE.toString() to jemaat.CREATED_DATE,
                        EJemaat.UPDATED_DATE.toString() to jemaat.UPDATED_DATE,
                        EJemaat.CREATED_BY.toString() to jemaat.CREATED_BY,
                        EJemaat.UPDATED_BY.toString() to jemaat.UPDATED_BY,
                        EJemaat.NAMA.toString() to jemaat.NAMA,
                        EJemaat.PROVINSI.toString() to jemaat.PROVINSI,
                        EJemaat.KOTA.toString() to jemaat.KOTA,
                        EJemaat.KECAMATAN.toString() to jemaat.KECAMATAN,
                        EJemaat.KELURAHAN.toString() to jemaat.KELURAHAN,
                        EJemaat.RT.toString() to jemaat.RT,
                        EJemaat.RW.toString() to jemaat.RW,
                        EJemaat.ALAMAT.toString() to jemaat.ALAMAT,
                        EJemaat.GENDER.toString() to jemaat.GENDER,
                        EJemaat.GOL_DARAH.toString() to jemaat.GOL_DARAH,
                        EJemaat.TEMPAT_LAHIR.toString() to jemaat.TEMPAT_LAHIR,
                        EJemaat.TANGGAL_LAHIR.toString() to jemaat.TANGGAL_LAHIR,
                        EJemaat.NO_TEL.toString() to jemaat.NO_TEL,
                        EJemaat.NOTE.toString() to jemaat.NOTE,
                        EJemaat.BAPTIS.toString() to jemaat.BAPTIS,
                        EJemaat.NO_SERTIFIKAT.toString() to jemaat.NO_SERTIFIKAT,
                        EJemaat.TEMPAT_BAPTIS.toString() to jemaat.TEMPAT_BAPTIS,
                        EJemaat.TANGGAL_BAPTIS.toString() to jemaat.TANGGAL_BAPTIS,
                        EJemaat.IMAGE.toString() to jemaat.IMAGE,
                        EJemaat.ID.toString() to jemaat.ID,
                        EJemaat.STATUS.toString() to jemaat.STATUS
                    )
                    database.child(getSinode())
                        .child(ETable.JEMAAT.toString())
                        .child(key.toString())
                        .setValue(values).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            view.response(EMessageResult.SUCCESS.toString())
                        }
                }

            }
            database.child(getSinode())
                .child(ETable.JEMAAT.toString())
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(
                context,
                e.message.toString()
            )
            e.printStackTrace()
        }

    }

    fun updateJemaat(jemaat: Jemaat,key:String){
        try {
            val values  = hashMapOf(
                EJemaat.CREATED_DATE.toString() to jemaat.CREATED_DATE,
                EJemaat.UPDATED_DATE.toString() to jemaat.UPDATED_DATE,
                EJemaat.CREATED_BY.toString() to jemaat.CREATED_BY,
                EJemaat.UPDATED_BY.toString() to jemaat.UPDATED_BY,
                EJemaat.NAMA.toString() to jemaat.NAMA,
                EJemaat.PROVINSI.toString() to jemaat.PROVINSI,
                EJemaat.KOTA.toString() to jemaat.KOTA,
                EJemaat.KECAMATAN.toString() to jemaat.KECAMATAN,
                EJemaat.KELURAHAN.toString() to jemaat.KELURAHAN,
                EJemaat.RT.toString() to jemaat.RT,
                EJemaat.RW.toString() to jemaat.RW,
                EJemaat.ALAMAT.toString() to jemaat.ALAMAT,
                EJemaat.GENDER.toString() to jemaat.GENDER,
                EJemaat.GOL_DARAH.toString() to jemaat.GOL_DARAH,
                EJemaat.TEMPAT_LAHIR.toString() to jemaat.TEMPAT_LAHIR,
                EJemaat.TANGGAL_LAHIR.toString() to jemaat.TANGGAL_LAHIR,
                EJemaat.NO_TEL.toString() to jemaat.NO_TEL,
                EJemaat.NOTE.toString() to jemaat.NOTE,
                EJemaat.BAPTIS.toString() to jemaat.BAPTIS,
                EJemaat.NO_SERTIFIKAT.toString() to jemaat.NO_SERTIFIKAT,
                EJemaat.TEMPAT_BAPTIS.toString() to jemaat.TEMPAT_BAPTIS,
                EJemaat.TANGGAL_BAPTIS.toString() to jemaat.TANGGAL_BAPTIS,
                EJemaat.IMAGE.toString() to jemaat.IMAGE,
                EJemaat.ID.toString() to jemaat.ID,
                EJemaat.STATUS.toString() to jemaat.STATUS
            )
            database.child(getSinode())
                .child(ETable.JEMAAT.toString())
                .child(key)
                .setValue(values).addOnFailureListener {
                    view.response(it.message.toString())
                }
                .addOnSuccessListener {
                    view.response(EMessageResult.SUCCESS.toString())
                }

        }catch (e: Exception){
            showError(
                context,
                e.message.toString()
            )
            e.printStackTrace()
        }

    }


    fun retrieveUserLists(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_USER_LIST_SUCCESS.toString())
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(EMerchant.USER_LIST.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveJemaats(){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_JEMAAT_SUCCESS.toString())
                }

            }
            database.child(getSinode())
                .child(ETable.JEMAAT.toString())
                .orderByChild(EJemaat.NAMA.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveJemaatByKey(key:String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_JEMAAT_BY_KEY_SUCCESS.toString())
                }

            }
            database.child(getSinode())
                .child(ETable.JEMAAT.toString())
                .child(key)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun deleteJemaat(key:String){
        try{
            database.child(getSinode())
                .child(ETable.JEMAAT.toString())
                .child(key)
                .child(EJemaat.STATUS.toString()).setValue(EStatusCode.DELETE.toString())
                .addOnSuccessListener {
                    view.response(EMessageResult.DELETE_SUCCESS.toString())
                }
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun getUserName(Id:String, callback: (name:String)->Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(User::class.java)
                        if (item != null) {
                            callback(item.NAME.toString())
                        }
                    }
                }

            }
            database.child(getSinode())
                .child(ETable.USER.toString())
                .child(Id)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveProvinsi(){
        try{
            val data = Gson().fromJson(ApiRepository().doRequest(DaerahIndonesiaDBApi.provinsi()),
                ProvinsiResponse::class.java)
            if (data != null)
                diView.showProvinsi(data.provinsi)
        }catch (e:Exception){
            showError(context,"-- retrieveProvinsi -- ${e.message}")
            e.printStackTrace()
        }
    }

    fun retrieveKotaKabupaten(provinsiKey: String){
        try{
            val data = Gson().fromJson(ApiRepository()
                .doRequest(DaerahIndonesiaDBApi.kotaKabupaten(provinsiKey)),
                KotaKabupatenResponse::class.java)
            if (data != null)
                diView.showKotaKabupaten(data.kota_kabupaten)
        }catch (e:Exception){
            showError(context,"-- retrieveKotaKabupaten -- ${e.message}")
            e.printStackTrace()
        }
    }

    fun retrieveKecamatan(kotaKey: String){
        try{
            val data = Gson().fromJson(ApiRepository()
                .doRequest(DaerahIndonesiaDBApi.kecamatan(kotaKey)),
                KecamatanResponse::class.java)
            if (data != null)
                diView.showKecamatan(data.kecamatan)
        }catch (e:Exception){
            showError(context,"-- retrieveKotaKabupaten -- ${e.message}")
            e.printStackTrace()
        }
    }

    fun retrieveKelurahan(kecamatanKey: String){
        try{
            val data = Gson().fromJson(ApiRepository()
                .doRequest(DaerahIndonesiaDBApi.kelurahan(kecamatanKey)),
                KelurahanResponse::class.java)
            if (data != null)
                diView.showKelurahan(data.kelurahan)
        }catch (e:Exception){
            showError(context,"-- retrieveKelurahan -- ${e.message}")
            e.printStackTrace()
        }
    }

    fun dismissListener(){
    }
}

