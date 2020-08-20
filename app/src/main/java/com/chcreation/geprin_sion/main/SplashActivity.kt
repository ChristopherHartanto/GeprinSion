package com.chcreation.geprin_sion.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar!!.hide()
        mAuth = FirebaseAuth.getInstance()

        val version = packageManager.getPackageInfo(packageName,0).versionName
        tvSplashVersion.text = version

        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                if (mAuth.currentUser == null)
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
}
