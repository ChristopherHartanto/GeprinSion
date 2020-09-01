package com.chcreation.geprin_sion.presenter

import android.content.ContentValues
import android.content.Context
import com.chcreation.geprin_sion.api.ApiRepository
import com.chcreation.geprin_sion.api.DaerahIndonesiaDBApi
import com.chcreation.geprin_sion.home.Content
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

class HomePresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun createContent(content: Content){
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
                        EContent.IMAGE_CONTENT.toString() to content.IMAGE_CONTENT,
                        EContent.LINK.toString() to content.LINK,
                        EContent.USER_CODE.toString() to content.USER_CODE,
                        EContent.USER_NAME.toString() to content.USER_NAME,
                        EContent.CAPTION.toString() to content.CAPTION,
                        EContent.TYPE.toString() to content.TYPE,
                        EContent.TOTAL_LIKE.toString() to content.TOTAL_LIKE,
                        EContent.KEY.toString() to content.KEY,
                        EContent.CREATED_DATE.toString() to content.CREATED_DATE,
                        EContent.UPDATED_DATE.toString() to content.UPDATED_DATE,
                        EContent.CREATED_BY.toString() to content.CREATED_BY,
                        EContent.UPDATED_BY.toString() to content.UPDATED_BY
                    )
                    database.child(getSinode())
                        .child(getPost())
                        .child(ETable.CONTENT.toString())
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
                .child(getPost())
                .child(ETable.CONTENT.toString())
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

    fun updateContent(contentId:String, content: Content,callback: (success:Boolean) ->Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            val item = data.getValue(Content::class.java)
                            if (item != null) {
                                val values  = hashMapOf(
                                    EContent.USER_IMAGE.toString() to content.USER_IMAGE,
                                    EContent.IMAGE_CONTENT.toString() to content.IMAGE_CONTENT,
                                    EContent.LINK.toString() to content.LINK,
                                    EContent.USER_CODE.toString() to content.USER_CODE,
                                    EContent.USER_NAME.toString() to content.USER_NAME,
                                    EContent.CAPTION.toString() to content.CAPTION,
                                    EContent.TYPE.toString() to content.TYPE,
                                    EContent.CHANNEL.toString() to content.CHANNEL,
                                    EContent.TOTAL_LIKE.toString() to item.TOTAL_LIKE,
                                    EContent.KEY.toString() to content.KEY,
                                    EContent.CREATED_DATE.toString() to content.CREATED_DATE,
                                    EContent.UPDATED_DATE.toString() to content.UPDATED_DATE,
                                    EContent.CREATED_BY.toString() to content.CREATED_BY,
                                    EContent.UPDATED_BY.toString() to content.UPDATED_BY
                                )
                                database.child(getSinode())
                                    .child(getPost())
                                    .child(ETable.CONTENT.toString())
                                    .child(data.key.toString())
                                    .setValue(values).addOnFailureListener {
                                        callback(false)
                                    }
                                    .addOnSuccessListener {
                                        callback(true)
                                    }
                            }
                        }
                    }else
                        callback(false)
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.CONTENT.toString())
                .orderByChild(EContent.KEY.toString())
                .equalTo(contentId)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun updateTotalLikes(contentId:String, value:Int,callback: (totalLike: Int) ->Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            val item = data.getValue(Content::class.java)
                            if (item != null) {
                                database.child(getSinode())
                                    .child(getPost())
                                    .child(ETable.CONTENT.toString())
                                    .child(data.key.toString())
                                    .child(EContent.UPDATED_DATE.toString())
                                    .setValue(dateFormat().format(Date()))

                                database.child(getSinode())
                                    .child(getPost())
                                    .child(ETable.CONTENT.toString())
                                    .child(data.key.toString())
                                    .child(EContent.UPDATED_BY.toString())
                                    .setValue(auth.currentUser!!.uid)

                                database.child(getSinode())
                                    .child(getPost())
                                    .child(ETable.CONTENT.toString())
                                    .child(data.key.toString())
                                    .child(EContent.TOTAL_LIKE.toString())
                                    .setValue(item.TOTAL_LIKE!! + value)
                                    .addOnSuccessListener {
                                        callback(item.TOTAL_LIKE!! + value)
                                    }
                                    .addOnFailureListener {
                                        callback(-99)
                                    }
                            }
                        }
                    }else
                        callback(-99)
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.CONTENT.toString())
                .orderByChild(EContent.KEY.toString())
                .equalTo(contentId)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveLikes(){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_LIKE_SUCCESS.toString())
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.LIKE.toString())
                .child(auth.currentUser!!.uid)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,"--retrieveLikes-- ${e.message}")
            e.printStackTrace()
        }
    }

    fun createLike(contentId: String,callback: (success: Boolean) ->Unit){
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
                        ELike.CONTENT_ID.toString() to contentId
                    )
                    database.child(getSinode())
                        .child(getPost())
                        .child(ETable.LIKE.toString())
                        .child(auth.currentUser!!.uid)
                        .child(key.toString())
                        .setValue(values).addOnFailureListener {
                            callback(false)
                        }
                        .addOnSuccessListener {
                            callback(true)
                        }
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.LIKE.toString())
                .child(auth.currentUser!!.uid)
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,"--createLike-- ${e.message}")
            e.printStackTrace()
        }

    }

    fun deleteLike(key: String,callback: (success: Boolean) ->Unit){
        try {
            database.child(getSinode())
                .child(getPost())
                .child(ETable.LIKE.toString())
                .child(auth.currentUser!!.uid)
                .child(key)
                .removeValue().addOnFailureListener {
                    callback(false)
                }
                .addOnSuccessListener {
                    callback(true)
                }
        }catch (e:Exception){
            showError(context,"--deleteLike-- ${e.message}")
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
                .child(getPost())
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


    fun retrieveContent(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_CONTENT_SUCCESS.toString())
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.CONTENT.toString())
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
                .child(getPost())
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
                .child(getPost())
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
                .child(getPost())
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
                .child(getPost())
                .child(ETable.USER.toString())
                .child(Id)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun dismissListener(){
    }
}

