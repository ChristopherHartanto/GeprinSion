package com.chcreation.geprin_sion.remaja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.Remaja
import com.chcreation.geprin_sion.presenter.AbsentPresenter
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_absent.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class AbsentActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: AbsentPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    companion object{
        var remajaItems = mutableListOf<Remaja>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absent)

        supportActionBar?.title = "Absensi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AbsentPresenter(this,mAuth,mDatabase,this)
        remajaItems.clear()

        llAbsentAddRemaja.onClick {
            llAbsentAddRemaja.startAnimation(normalClickAnimation())

            startActivity<AddRemajaActivity>()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveRemajaList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        private val tabName : Array<String> = arrayOf("All", "Anggota Padus")

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> {
                RemajaListFragment()
            }
            else -> PadusListFragment()
        }

        override fun getCount(): Int = tabName.size
        override fun getPageTitle(position: Int): CharSequence? = tabName[position]
    }

    private fun bindViewPager(){

        val adapter = TabAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        vpAbsent.adapter = adapter
        tlAbsent.setupWithViewPager(vpAbsent)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_REMAJA_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children){
                    val item = data.getValue(Remaja::class.java)
                    if (item != null){
                        remajaItems.add(item)
                    }
                }
            }
            pbAbsent.visibility = View.GONE
            bindViewPager()
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
