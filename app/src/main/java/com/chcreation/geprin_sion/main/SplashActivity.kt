package com.chcreation.geprin_sion.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.home.Content
import com.chcreation.geprin_sion.home.HomeFragment
import com.chcreation.geprin_sion.home.HomeFragment.Companion.contentItems
import com.chcreation.geprin_sion.home.HomeFragment.Companion.likeItems
import com.chcreation.geprin_sion.home.HomeFragment.Companion.likeKeyItems
import com.chcreation.geprin_sion.login.LoginActivity
import com.chcreation.geprin_sion.model.Admin
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.Like
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.geprin_sion.presenter.UserPresenter
import com.chcreation.geprin_sion.util.getName
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var userPresenter: UserPresenter
    private lateinit var homePresenter: HomePresenter

    companion object{
        var adminName = ""
        var adminNoTel = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar!!.hide()
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        userPresenter = UserPresenter(this,mAuth,mDatabase,this)
        homePresenter = HomePresenter(this,mAuth,mDatabase,this)

        val version = packageManager.getPackageInfo(packageName,0).versionName
        tvSplashVersion.text = version

        tvSplashLoading.text = "Fetch Data . . ."
        MainActivity().clearContentData()

        if (mAuth.currentUser != null){
            homePresenter.retrieveLikes()
        }else{
            homePresenter.retrieveContent()
        }

    }

    private fun loading(){
        val timer = object: CountDownTimer(500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                if (mAuth.currentUser == null || getName(this@SplashActivity) == "")
                    startActivity<LoginActivity>()
                else
                    startActivity<MainActivity>()

                finish()

                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }
        timer.start()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_ADMIN_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Admin::class.java)
                if (item != null) {
                    adminName = item.NAME.toString()
                    adminNoTel = item.NO_TEL.toString()
                }
            }
            tvSplashLoading.text = "Fetch Completed . . ."
            loading()
        }
        if (response == EMessageResult.FETCH_CONTENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                contentItems.clear()

                for (data in dataSnapshot.children){
                    val item = data.getValue(Content::class.java)
                    if (item != null){
                        for (likeItem in likeItems){
                            if (likeItem.CONTENT_ID == item.KEY)
                                item.LIKE = true
                        }
                        contentItems.add(item)
                    }
                }
                contentItems.reverse()
            }

            tvSplashLoading.text = "Fetch Admin Data . . ."
            userPresenter.retrieveAdmin()
        }
        if (response == EMessageResult.FETCH_LIKE_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                likeItems.clear()
                likeKeyItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Like::class.java)
                    if (item != null){
                        likeItems.add(item)
                        likeKeyItems.add(data.key.toString())
                    }
                }
            }
            tvSplashLoading.text = "Fetch Content . . ."
            homePresenter.retrieveContent()
        }
    }

    override fun response(message: String) {
    }
}
