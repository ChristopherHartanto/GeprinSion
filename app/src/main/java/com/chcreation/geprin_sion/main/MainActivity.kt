package com.chcreation.geprin_sion.main

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.home.HomeFragment.Companion.active
import com.chcreation.geprin_sion.login.LoginActivity
import com.chcreation.geprin_sion.util.RESULT_CLOSE_ALL
import com.chcreation.geprin_sion.util.getName
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.removeAllSharedPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var doubleBackToExitPressedOnce = false
    private lateinit var view : View
    private lateinit var tvNavHeaderMerchantName : TextView
    private lateinit var ivNavHeader : ImageView
    private lateinit var ivNavLogout : ImageView
    private lateinit var tvNavHeaderFirstName : TextView
    private lateinit var tvUserName : TextView
    private lateinit var layoutNavHeaderDefaultImage: FrameLayout
    private lateinit var layoutNavHeader: LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,R.id.nav_jemaat
            ), drawerLayout
        )

        view = navView.getHeaderView(0)
        tvUserName = view.findViewById<TextView>(R.id.tvMainUserName)
        ivNavHeader = view.findViewById<ImageView>(R.id.imageView)
        ivNavLogout = view.findViewById<ImageView>(R.id.logOut)

        tvUserName.text = getName(this)

        ivNavLogout.onClick {
            ivNavLogout.startAnimation(normalClickAnimation())
            alert("Do You Want to Logout ?") {
                title = "Logout"
                yesButton {
                    removeAllSharedPreference(this@MainActivity)
                    mAuth.signOut()
                    startActivity<LoginActivity>()
                    finish()
                }
                noButton {  }
            }.show()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            setResult(RESULT_CLOSE_ALL)

            super.onBackPressed()
            return
        }
        if (active){
            this.doubleBackToExitPressedOnce = true
            toast("Please click BACK again to exit")
        }else
            super.onBackPressed()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
