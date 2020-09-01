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
import java.util.*

class ProfilePresenter(private val view: MainView,
                       private val diView: DaerahIndonesiaView,
                       private val auth: FirebaseAuth,
                       private val database: DatabaseReference,
                       private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun updateContentUserData(userKey: String, userName: String, userImage: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (data in p0.children){
                        database.child(getSinode())
                            .child(getPost())
                            .child(ETable.CONTENT.toString())
                            .child(data.key.toString())
                            .child(EContent.USER_NAME.toString())
                            .setValue(userName).addOnFailureListener {
                                view.response(it.message.toString())
                            }

                        database.child(getSinode())
                            .child(getPost())
                            .child(ETable.CONTENT.toString())
                            .child(data.key.toString())
                            .child(EContent.USER_IMAGE.toString())
                            .setValue(userImage).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                    }
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.CONTENT.toString())
                .orderByChild(EContent.USER_CODE.toString())
                .equalTo(userKey)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun updateUser(user: User, callback: (success: Boolean) ->Unit){
        try {
            val values  = hashMapOf(
                EUser.NAME.toString() to user.NAME,
                EUser.EMAIL.toString() to user.EMAIL,
                EUser.GENDER.toString() to user.GENDER,
                EUser.GOL_DARAH.toString() to user.GOL_DARAH,
                EUser.TANGGAL_LAHIR.toString() to user.TANGGAL_LAHIR,
                EUser.TEMPAT_LAHIR.toString() to user.TEMPAT_LAHIR,
                EUser.RT.toString() to user.RT,
                EUser.RW.toString() to user.RW,
                EUser.NO_TEL.toString() to user.NO_TEL,
                EUser.PROVINSI.toString() to user.PROVINSI,
                EUser.KOTA.toString() to user.KOTA,
                EUser.KECAMATAN.toString() to user.KECAMATAN,
                EUser.KELURAHAN.toString() to user.KELURAHAN,
                EUser.ALAMAT.toString() to user.ALAMAT,
                EUser.IMAGE.toString() to user.IMAGE,
                EUser.ACTIVE.toString() to user.ACTIVE,
                EUser.STATUS.toString() to user.STATUS,
                EUser.CREATED_DATE.toString() to dateFormat().format(Date()),
                EUser.UPDATED_DATE.toString() to dateFormat().format(Date())
            )
            database.child(getSinode())
                .child(getPost())
                .child(ETable.USER.toString())
                .child(auth.currentUser!!.uid)
                .setValue(values).addOnFailureListener {
                    callback(false)
                }
                .addOnSuccessListener {
                    callback(true)
                }

        }catch (e: Exception){
            showError(
                context,
                e.message.toString()
            )
            e.printStackTrace()
        }
    }

    fun retrieveUser(userId: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_USER_SUCCESS.toString())
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.USER.toString())
            .child(userId)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveProvinsi(){
        try{
            val data = Gson().fromJson(
                ApiRepository().doRequest(DaerahIndonesiaDBApi.provinsi()),
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
            val data = Gson().fromJson(
                ApiRepository()
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
            val data = Gson().fromJson(
                ApiRepository()
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
            val data = Gson().fromJson(
                ApiRepository()
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

