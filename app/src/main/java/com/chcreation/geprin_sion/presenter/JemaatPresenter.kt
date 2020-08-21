package com.chcreation.geprin_sion.presenter

import android.content.Context
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.util.*
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.util.*

class JemaatPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

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
                        EJemaat.ID.toString() to jemaat.ID
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
            .addListenerForSingleValueEvent(postListener)
    }

    fun dismissListener(){
    }
}

