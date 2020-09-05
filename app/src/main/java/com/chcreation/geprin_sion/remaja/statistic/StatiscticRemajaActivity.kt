package com.chcreation.geprin_sion.remaja.statistic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.Absent
import com.chcreation.geprin_sion.presenter.AbsentPresenter
import com.chcreation.geprin_sion.remaja.PadusListFragment
import com.chcreation.geprin_sion.remaja.RemajaListFragment
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.view.TransactionInterface
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_absent.*
import kotlinx.android.synthetic.main.activity_statisctic_remaja.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class StatiscticRemajaActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: AbsentPresenter

    companion object{
        var absentItems = mutableListOf<Absent>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statisctic_remaja)

        supportActionBar?.title = "Statistic Remaja"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AbsentPresenter(this,mAuth,mDatabase,this)

    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveAbsents(object : TransactionInterface{
            override fun handleData(data: Any, resultCode: Int) {
                val dataSnapshot = data as DataSnapshot
                absentItems.clear()
                for (value in dataSnapshot.children){
                    val item = value.getValue(Absent::class.java)
                    if (item != null) {
                        absentItems.add(item)
                    }
                }
                bindViewPager()
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
    class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        private val tabName : Array<String> = arrayOf("Semua Remaja", "Paduan Suara")

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> {
                StatisticAbsentRemajaFragment()
            }
            else -> StatisticAbsentPadusFragment()
        }

        override fun getCount(): Int = tabName.size
        override fun getPageTitle(position: Int): CharSequence? = tabName[position]
    }

    private fun bindViewPager(){

        val adapter = TabAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        vpStatisticRemaja.adapter = adapter

        tlStatisticRemaja.setupWithViewPager(vpStatisticRemaja)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
