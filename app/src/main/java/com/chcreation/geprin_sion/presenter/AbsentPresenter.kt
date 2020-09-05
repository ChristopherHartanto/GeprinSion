package com.chcreation.geprin_sion.presenter

import android.content.ContentValues
import android.content.Context
import com.chcreation.geprin_sion.api.ApiRepository
import com.chcreation.geprin_sion.api.DaerahIndonesiaDBApi
import com.chcreation.geprin_sion.home.Content
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.util.*
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.geprin_sion.view.TransactionInterface
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.lang.Exception
import java.util.*

class AbsentPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun createAbsent(callback: TransactionInterface,absent: Absent){
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
                        EAbsent.DETAIL.toString() to absent.DETAIL,
                        EAbsent.CHANNEL.toString() to absent.CHANNEL,
                        EAbsent.TYPE.toString() to absent.TYPE,
                        EAbsent.ABSENT_DATE.toString() to absent.ABSENT_DATE,
                        EAbsent.KEY.toString() to absent.KEY,
                        EAbsent.CREATED_DATE.toString() to absent.CREATED_DATE,
                        EAbsent.UPDATED_DATE.toString() to absent.UPDATED_DATE,
                        EAbsent.CREATED_BY.toString() to absent.CREATED_BY,
                        EAbsent.UPDATED_BY.toString() to absent.UPDATED_BY,
                        EAbsent.STATUS.toString() to absent.STATUS
                    )
                    database.child(getSinode())
                        .child(getPost())
                        .child(ETable.ABSENT.toString())
                        .child(key.toString())
                        .setValue(values).addOnFailureListener {
                            callback.handleData("",EResultCode.FAILED.value)
                        }
                        .addOnSuccessListener {
                            callback.handleData("",EResultCode.SUCCESS.value)
                        }
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.ABSENT.toString())
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

    fun updateAbsent(callback: TransactionInterface,absent: Absent, key: String){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            val values  = hashMapOf(
                                EAbsent.DETAIL.toString() to absent.DETAIL,
                                EAbsent.CHANNEL.toString() to absent.CHANNEL,
                                EAbsent.TYPE.toString() to absent.TYPE,
                                EAbsent.ABSENT_DATE.toString() to absent.ABSENT_DATE,
                                EAbsent.KEY.toString() to absent.KEY,
                                EAbsent.CREATED_DATE.toString() to absent.CREATED_DATE,
                                EAbsent.UPDATED_DATE.toString() to absent.UPDATED_DATE,
                                EAbsent.CREATED_BY.toString() to absent.CREATED_BY,
                                EAbsent.UPDATED_BY.toString() to absent.UPDATED_BY,
                                EAbsent.STATUS.toString() to absent.STATUS
                            )
                            database.child(getSinode())
                                .child(getPost())
                                .child(ETable.ABSENT.toString())
                                .child(data.key.toString())
                                .updateChildren(values as Map<String, Any>).addOnFailureListener {
                                    callback.handleData("",EResultCode.FAILED.value)
                                }
                                .addOnSuccessListener {
                                    callback.handleData("",EResultCode.SUCCESS.value)
                                }
                        }
                    }else
                        callback.handleData("",EResultCode.FAILED.value)

                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.ABSENT.toString())
                .orderByChild(EAbsent.KEY.toString())
                .equalTo(key)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(
                context,
                e.message.toString()
            )
            e.printStackTrace()
        }

    }

    fun updateRemaja(callback: TransactionInterface, remaja: Remaja, remajaId: String){
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
                                    ERemaja.NAMA.toString() to remaja.NAMA,
                                    ERemaja.KELAS.toString() to remaja.KELAS,
                                    ERemaja.SEKOLAH.toString() to remaja.SEKOLAH,
                                    ERemaja.HOBBY.toString() to remaja.HOBBY,
                                    ERemaja.WARNA_FAV.toString() to remaja.WARNA_FAV,
                                    ERemaja.ALAMAT.toString() to remaja.ALAMAT,
                                    ERemaja.GENDER.toString() to remaja.GENDER,
                                    ERemaja.TEMPAT_LAHIR.toString() to remaja.TEMPAT_LAHIR,
                                    ERemaja.TANGGAL_LAHIR.toString() to remaja.TANGGAL_LAHIR,
                                    ERemaja.NO_TEL.toString() to remaja.NO_TEL,
                                    ERemaja.NOTE.toString() to remaja.NOTE,
                                    ERemaja.IS_PADUS.toString() to remaja.IS_PADUS,
                                    ERemaja.JENIS_SUARA.toString() to remaja.JENIS_SUARA,
                                    ERemaja.IS_PELAYANAN.toString() to remaja.IS_PELAYANAN,
                                    ERemaja.LITURGOS.toString() to remaja.LITURGOS,
                                    ERemaja.PENYAMBUT.toString() to remaja.PENYAMBUT,
                                    ERemaja.PIANIS.toString() to remaja.PIANIS,
                                    ERemaja.GITARIS.toString() to remaja.GITARIS,
                                    ERemaja.LCD.toString() to remaja.LCD,
                                    ERemaja.PENGURUS.toString() to remaja.PENGURUS,
                                    ERemaja.ABSENSI.toString() to remaja.ABSENSI,
                                    ERemaja.KOLEKTOR.toString() to remaja.KOLEKTOR,
                                    ERemaja.PADUS.toString() to remaja.PADUS,
                                    ERemaja.CREATED_DATE.toString() to remaja.CREATED_DATE,
                                    ERemaja.UPDATED_DATE.toString() to remaja.UPDATED_DATE,
                                    ERemaja.CREATED_BY.toString() to remaja.CREATED_BY,
                                    ERemaja.UPDATED_BY.toString() to remaja.UPDATED_BY,
                                    ERemaja.IMAGE.toString() to remaja.IMAGE,
                                    ERemaja.ID.toString() to remaja.ID,
                                    ERemaja.STATUS.toString() to remaja.STATUS
                                )
                                database.child(getSinode())
                                    .child(getPost())
                                    .child(ETable.REMAJA.toString())
                                    .child(data.key.toString())
                                    .updateChildren(values).addOnFailureListener {
                                        callback.handleData("",EResultCode.FAILED.value)
                                    }
                                    .addOnSuccessListener {
                                        callback.handleData("",EResultCode.SUCCESS.value)
                                    }
                            }
                        }
                    }else
                        callback.handleData("",EResultCode.FAILED.value)
                }

            }
            database.child(getSinode())
                .child(getPost())
                .child(ETable.REMAJA.toString())
                .orderByChild(ERemaja.ID.toString())
                .equalTo(remajaId)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }


    fun retrieveRemajaList(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_REMAJA_SUCCESS.toString())
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.REMAJA.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveAbsents(callback: TransactionInterface){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    callback.handleData(p0,EResultCode.SUCCESS.value)
                }else
                    callback.handleData("",EResultCode.FAILED.value)
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.ABSENT.toString())
            .orderByChild(EAbsent.ABSENT_DATE.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveAbsentByTypeAndDate(type: String,date:String,callback: TransactionInterface){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    var success = false

                    for (data in p0.children){
                        val item = data.getValue(Absent::class.java)
                        if (item != null){
                            if (item.ABSENT_DATE == date){
                                callback.handleData(item,EResultCode.SUCCESS.value)
                                success = true
                                break
                            }
                        }
                    }
                    if (!success)
                        callback.handleData("",EResultCode.FAILED.value)
                }else
                    callback.handleData("",EResultCode.FAILED.value)
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.ABSENT.toString())
            .child(EAbsent.TYPE.toString())
            .equalTo(type)
            .addListenerForSingleValueEvent(postListener)
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

