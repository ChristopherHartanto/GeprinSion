package com.chcreation.geprin_sion.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.chcreation.geprin_sion.*
import com.chcreation.geprin_sion.main.MainActivity
import com.chcreation.geprin_sion.model.EDataType
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.ESharedPreference
import com.chcreation.geprin_sion.model.User
import com.chcreation.geprin_sion.presenter.UserPresenter
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.setDataPreference
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: UserPresenter
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = UserPresenter(this,mAuth,mDatabase, this)

        btnLogin.onClick {
            btnLogin.startAnimation(normalClickAnimation())
            login()
        }

        tvLoginRegister.onClick {
            startActivity<SignUpActivity>()
            finish()
        }
    }

    private fun login () {

        email = etLoginEmail.text.toString()
        val password = etLoginPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("Fetch User Data . . .")
                    mAuth.currentUser?.uid?.let { presenter.retrieveUser(it) }
                }else {
                    Toast.makeText(this, "Error Success, try again later ", Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                    pbLogin.visibility = View.GONE
                }
            })
        }else {
            etLoginEmail.setText("")
            etLoginPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
            btnLogin.isEnabled = true
            pbLogin.visibility = View.GONE
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)

                setDataPreference(
                    this,
                    ESharedPreference.EMAIL.toString(),
                    email,
                    EDataType.STRING
                )
                if (item != null) {
                    setDataPreference(
                        this,
                        ESharedPreference.NAME.toString(),
                        item.NAME.toString(),
                        EDataType.STRING
                    )
                }
                toast("Login Success")
                startActivity<MainActivity>()
                finish()
            }
        }
    }

    override fun response(message: String) {
        toast(message)
    }
}
