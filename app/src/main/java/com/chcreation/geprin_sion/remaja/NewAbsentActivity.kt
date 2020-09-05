package com.chcreation.geprin_sion.remaja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.AbsentPresenter
import com.chcreation.geprin_sion.presenter.RemajaPresenter
import com.chcreation.geprin_sion.util.dateFormat
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.view.TransactionInterface
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_new_absent.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*
import kotlin.collections.ArrayList

class NewAbsentActivity : AppCompatActivity(),MainView {

    private lateinit var adapter: RemajaAbsentRecyclerViewAdapter
    private lateinit var presenter: AbsentPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var remajaItems = mutableListOf<Remaja>()
    private var absentItems = mutableListOf<AbsentDetail>()
    private var presentItems = mutableListOf<String>()
    private var date = ""
    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_absent)
//
//        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
//        val purchasedItems : MutableList<Cart> = gson.fromJson(boughtList.DETAIL,arrayCartType)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AbsentPresenter(this,mAuth,mDatabase,this)
        adapter = RemajaAbsentRecyclerViewAdapter(this,absentItems){
            absentItems[it].PRESENT = !absentItems[it].PRESENT!!
            adapter.notifyDataSetChanged()
            fetchPresent()
            tvNewAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
            if (absentItems.none { it.PRESENT == true })
                tvNewAbsentAddRemoveAll.text = "Add All"
            else
                tvNewAbsentAddRemoveAll.text = "Remove All"
        }

        tvNewAbsentAddRemoveAll.onClick {
            tvNewAbsentAddRemoveAll.startAnimation(normalClickAnimation())

            if (absentItems.none { it.PRESENT == true }){
                for (data in absentItems){
                    data.PRESENT = true
                }
                adapter.notifyDataSetChanged()
                fetchPresent()
                tvNewAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
                tvNewAbsentAddRemoveAll.text = "Remove All"
            }
            else{
                for (data in absentItems){
                    data.PRESENT = false
                }
                adapter.notifyDataSetChanged()
                fetchPresent()
                tvNewAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
                tvNewAbsentAddRemoveAll.text = "Add All"
            }
        }

        date = intent.extras!!.getString(EAbsent.ABSENT_DATE.toString(),"")
        type = intent.extras!!.getString(EAbsent.TYPE.toString(), "")

        supportActionBar?.title = date
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvNewAbsent.apply {
            adapter = this@NewAbsentActivity.adapter
            layoutManager = LinearLayoutManager(this@NewAbsentActivity)
        }
        presenter.retrieveRemajaList()

        btnSaveAbsent.onClick {
            btnSaveAbsent.startAnimation(normalClickAnimation())

            var confirmType = ""
            if (type == EAbsentType.All.toString())
                confirmType = "Semua Remaja"
            else if (type == EAbsentType.PADUS.toString())
                confirmType = "Anggota Padus"
            alert ("Confirm to Create Absent for $confirmType?\nPresent: ${presentItems.size}/${remajaItems.size}"){
                title = "Save"
                yesButton {
                    loading()
                    saveAbsent(presentItems)
                }
                noButton {  }
            }.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fetchPresent(){
        presentItems.clear()
        for (data in absentItems){
            if (data.PRESENT!!)
                presentItems.add(data.ID.toString())
        }
    }

    private fun saveAbsent(item: MutableList<String>){
        val detail = Gson().toJson(item)
        presenter.createAbsent(object : TransactionInterface{
            override fun handleData(data: Any, resultCode: Int) {
                endLoading()
                if (resultCode == EResultCode.SUCCESS.value){
                    toast("Absent Created Success")
                    finish()
                }else{
                    toast("Failed to Create")
                }
            }

        },Absent(detail,EChannel.Remaja.toString(),type,EStatusCode.ACTIVE.toString(),date,generateAbsent(),
            dateFormat().format(Date()),mAuth.currentUser!!.uid,dateFormat().format(Date()),mAuth.currentUser!!.uid))
    }

    private fun generateAbsent() : String{
        return "A${mDatabase.push().key.toString()}"
    }

    private fun loading(){
        pbNewAbsent.visibility = View.VISIBLE
    }

    private fun endLoading(){
        pbNewAbsent.visibility = View.GONE
    }

    private fun fetchData(){
        absentItems.clear()
        for (data in remajaItems){
            absentItems.add(AbsentDetail(data.ID,data.NAMA,data.IMAGE,false))
        }
        adapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
         if (response == EMessageResult.FETCH_REMAJA_SUCCESS.toString()){
             if (dataSnapshot.exists()){
                 remajaItems.clear()
                 for (data in dataSnapshot.children){
                     val item = data.getValue(Remaja::class.java)
                     if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()){
                         if (type == EAbsentType.PADUS.toString()){
                             if (item.IS_PADUS!!)
                                 remajaItems.add(item)
                         }
                         else if (type == EAbsentType.All.toString())
                             remajaItems.add(item)
                     }
                 }
                 fetchData()
             }
             pbNewAbsent.visibility = View.GONE
         }
    }

    override fun response(message: String) {

    }
}
