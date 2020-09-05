package com.chcreation.geprin_sion.remaja

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.ERemaja
import com.chcreation.geprin_sion.model.Remaja
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.presenter.RemajaPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_remaja_detail.*
import org.jetbrains.anko.*

class RemajaDetailActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: RemajaPresenter
    private var currentRemaja = Remaja()
    private var remajaKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remaja_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Remaja"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = RemajaPresenter(this,mAuth,mDatabase,this)

        remajaKey = intent.extras!!.getString(ERemaja.ID.toString(),"")

        presenter.retrieveRemajaByKey(remajaKey)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_edit ->{
                startActivity(intentFor<ManageRemajaActivity>(ERemaja.ID.toString() to remajaKey))
                finish()
                true
            }
            R.id.action_delete ->{
                alert ("Do You Want to Delete ${currentRemaja.NAMA} ?"){
                    title = "Delete"
                    yesButton {
                        presenter.deleteRemaja(currentRemaja.ID.toString()){
                            if (it){
                                toast("Delete Success")
                                finish()
                            }else{
                                toast("Delete Failed, Please Try Again")
                            }
                        }
                    }
                    noButton {  }
                }.show()
                true
            }
            R.id.action_chat_wa ->{
                intentWhatsapp(currentRemaja.NO_TEL.toString())
                true
            }
            android.R.id.home ->{
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun intentWhatsapp(number: String){
        val url = "https://api.whatsapp.com/send?phone=$number"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun fetchData(){
        tvRemajaDName.text = currentRemaja.NAMA
        supportActionBar?.title = currentRemaja.NAMA

        tvRemajaDAlamat.text = currentRemaja.ALAMAT
        tvRemajaDHobby.text = currentRemaja.HOBBY
        tvRemajaDWarnaFav.text = currentRemaja.WARNA_FAV
        tvRemajaDJenisKelamin.text = currentRemaja.GENDER
        tvRemajaDSekolah.text = currentRemaja.SEKOLAH
        tvRemajaDKelas.text = currentRemaja.KELAS
        tvRemajaDJenisSuara.text = currentRemaja.JENIS_SUARA
        tvRemajaDTempatLahir.text = currentRemaja.TEMPAT_LAHIR
        tvRemajaDTglLahir.text = currentRemaja.TANGGAL_LAHIR
        tvRemajaDNoHp.text = currentRemaja.NO_TEL
        tvRemajaDNote.text = currentRemaja.NOTE

        if (currentRemaja.IMAGE != "")
            Glide.with(this).load(currentRemaja.IMAGE).into(ivRemajaDImage)

        cbRemajaDAbsensi.isChecked = currentRemaja.ABSENSI!!
        cbRemajaDGitaris.isChecked = currentRemaja.GITARIS!!
        cbRemajaDPianis.isChecked = currentRemaja.PIANIS!!
        cbRemajaDLCD.isChecked = currentRemaja.LCD!!
        cbRemajaDKolektor.isChecked = currentRemaja.KOLEKTOR!!
        cbRemajaDLiturgos.isChecked = currentRemaja.LITURGOS!!
        cbRemajaDPadus.isChecked = currentRemaja.IS_PADUS!!
        cbRemajaDPenyambut.isChecked = currentRemaja.PENYAMBUT!!
        cbRemajaDPengurus.isChecked = currentRemaja.PENGURUS!!
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_REMAJA_BY_KEY_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                pbRemajaD.visibility = View.GONE
                for (data in dataSnapshot.children){
                    val item = data.getValue(Remaja::class.java)
                    if (item != null) {
                        currentRemaja = item
                        fetchData()
                    }
                }
            }
        }
    }

    override fun response(message: String) {

    }
}
