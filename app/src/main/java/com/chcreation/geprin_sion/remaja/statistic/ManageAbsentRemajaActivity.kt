package com.chcreation.geprin_sion.remaja.statistic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.AbsentPresenter
import com.chcreation.geprin_sion.remaja.RemajaAbsentRecyclerViewAdapter
import com.chcreation.geprin_sion.remaja.statistic.StatisticAbsentPadusFragment.Companion.currentAbsent
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
import kotlinx.android.synthetic.main.activity_manage_absent_remaja.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*

class ManageAbsentRemajaActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: RemajaAbsentRecyclerViewAdapter
    private lateinit var presenter: AbsentPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var remajaItems = mutableListOf<Remaja>()
    private var absentItems = mutableListOf<AbsentDetail>()
    private var lastAbsentItems = mutableListOf<String>()
    private var presentItems = mutableListOf<String>()
    private var absent = Absent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_absent_remaja)

        if (currentAbsent.CREATED_DATE != "")
            absent = currentAbsent
        else if (StatisticAbsentRemajaFragment.currentAbsent.CREATED_DATE != "")
            absent = StatisticAbsentRemajaFragment.currentAbsent

        val currentAbsentType = object : TypeToken<MutableList<String>>() {}.type
        lastAbsentItems = Gson().fromJson(absent.DETAIL,currentAbsentType)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AbsentPresenter(this,mAuth,mDatabase,this)

        presenter.retrieveRemajaList()

        adapter = RemajaAbsentRecyclerViewAdapter(this,absentItems){
            absentItems[it].PRESENT = !absentItems[it].PRESENT!!
            adapter.notifyDataSetChanged()
            fetchPresent()
            tvManageAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
            if (absentItems.none { it.PRESENT == true })
                tvManageAbsentAddRemoveAll.text = "Add All"
            else
                tvManageAbsentAddRemoveAll.text = "Remove All"
        }

        tvManageAbsentAddRemoveAll.onClick {
            tvManageAbsentAddRemoveAll.startAnimation(normalClickAnimation())

            if (absentItems.none { it.PRESENT == true }){
                for (data in absentItems){
                    data.PRESENT = true
                }
                adapter.notifyDataSetChanged()
                fetchPresent()
                tvManageAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
                tvManageAbsentAddRemoveAll.text = "Remove All"
            }
            else{
                for (data in absentItems){
                    data.PRESENT = false
                }
                adapter.notifyDataSetChanged()
                fetchPresent()
                tvManageAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
                tvManageAbsentAddRemoveAll.text = "Add All"
            }
        }

        supportActionBar?.title = absent.ABSENT_DATE
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvManageAbsent.apply {
            adapter = this@ManageAbsentRemajaActivity.adapter
            layoutManager = LinearLayoutManager(this@ManageAbsentRemajaActivity)
        }
        presenter.retrieveRemajaList()

        btnManageAbsent.onClick {
            btnManageAbsent.startAnimation(normalClickAnimation())

            var confirmType = ""
            if (absent.TYPE == EAbsentType.All.toString())
                confirmType = "Semua Remaja"
            else if (absent.TYPE == EAbsentType.PADUS.toString())
                confirmType = "Anggota Padus"
            alert ("Confirm to Update Absent for $confirmType?\nPresent: ${presentItems.size}/${remajaItems.size}"){
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
        presenter.updateAbsent(object : TransactionInterface {
            override fun handleData(data: Any, resultCode: Int) {
                endLoading()
                if (resultCode == EResultCode.SUCCESS.value){
                    toast("Absent Update Succeed")
                    finish()
                }else{
                    toast("Failed to Update")
                }
            }

        },
            Absent(detail,absent.CHANNEL,
                absent.TYPE,
                absent.STATUS,absent.ABSENT_DATE,absent.KEY,
                absent.CREATED_DATE,absent.CREATED_BY,
                dateFormat().format(Date()),mAuth.currentUser!!.uid)
        ,absent.KEY.toString())
    }

    private fun loading(){
        pbManageAbsent.visibility = View.VISIBLE
    }

    private fun endLoading(){
        pbManageAbsent.visibility = View.GONE
    }

    private fun fetchData(){
        absentItems.clear()
        for (data in remajaItems){
            var check = false
            for (value in lastAbsentItems){
                if (data.ID == value){
                    absentItems.add(AbsentDetail(data.ID,data.NAMA,data.IMAGE,true))
                    check = true
                }
            }
            if (!check)
                absentItems.add(AbsentDetail(data.ID,data.NAMA,data.IMAGE,false))
        }
        tvManageAbsentTotal.text = "${presentItems.size}/${absentItems.size}"
        adapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_REMAJA_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                remajaItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Remaja::class.java)
                    if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()){
                        if (absent.TYPE == EAbsentType.PADUS.toString()){
                            if (item.IS_PADUS!!)
                                remajaItems.add(item)
                        }
                        else if (absent.TYPE == EAbsentType.All.toString())
                            remajaItems.add(item)
                    }
                }
                fetchData()
                fetchPresent()
            }
            pbManageAbsent.visibility = View.GONE
        }
    }

    override fun response(message: String) {

    }
}
