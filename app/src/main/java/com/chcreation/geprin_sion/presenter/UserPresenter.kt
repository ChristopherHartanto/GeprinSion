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

class UserPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun createUser(name: String, email: String, callback: (success: Boolean) ->Unit){
        try {
            val values  = hashMapOf(
                EUser.NAME.toString() to name,
                EUser.EMAIL.toString() to email,
                EUser.ACTIVE.toString() to 0,
                EUser.STATUS.toString() to EStatusUser.USER.toString(),
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

    fun retrieveAdmin(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_ADMIN_SUCCESS.toString())
            }

        }
        database.child(getSinode())
            .child(getPost())
            .child(ETable.ADMIN.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    private fun generateCustomerCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

