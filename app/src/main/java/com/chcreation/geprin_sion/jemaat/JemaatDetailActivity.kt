package com.chcreation.geprin_sion.jemaat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_jemaat_detail.*
import org.jetbrains.anko.*


class JemaatDetailActivity : AppCompatActivity(), MainView,DaerahIndonesiaView {

    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: JemaatPresenter
    private lateinit var mAuth: FirebaseAuth
    private var currentJemaat = Jemaat()
    private var currentJemaatKey = 0
    private var currentJemaatId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jemaat_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = JemaatPresenter(this,this,mAuth,mDatabase, this)
        currentJemaatKey = intent.extras?.getInt(EJemaat.KEY.toString()) ?: 0
    }

    override fun onStart() {
        super.onStart()

        currentJemaatId = intent.extras?.getString(EJemaat.ID.toString(),"").toString()
        presenter.retrieveJemaatByKey(currentJemaatKey.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_edit -> {
                if (currentJemaat.NAMA != ""){
                    startActivity(intentFor<ManageJemaatActivity>(EJemaat.ID.toString() to currentJemaatId,
                        EJemaat.KEY.toString() to currentJemaatKey))
                    finish()
                }else
                    toast("Please Try Again")
                true
            }
            R.id.action_delete -> {
                if (currentJemaat.NAMA != ""){
                    alert {
                        alert ("Do You Want to Delete ${currentJemaat.NAMA}?"){
                            title = "Delete"

                            yesButton {
                                pbJemaatD.visibility = View.VISIBLE
                                presenter.deleteJemaat(currentJemaatKey.toString())
                            }
                            noButton {

                            }
                        }.show()
                    }
                }else
                    toast("Please Try Again")
                true
            }
            R.id.action_chat_wa ->{
                intentWhatsapp(currentJemaat.NO_TEL.toString())
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
        tvJemaatDName.text = currentJemaat.NAMA
        supportActionBar?.title = currentJemaat.NAMA
        tvJemaatDAlamat.text = currentJemaat.ALAMAT
        tvJemaatDTempatLahir.text = currentJemaat.TEMPAT_LAHIR
        tvJemaatDTglLahir.text = currentJemaat.TANGGAL_LAHIR
        tvJemaatDNoHp.text = currentJemaat.NO_TEL
        tvJemaatDGolDarah.text = currentJemaat.GOL_DARAH
        tvJemaatDJenisKelamin.text = currentJemaat.GENDER
        tvJemaatDProvinsi.text = currentJemaat.PROVINSI
        tvJemaatDKota.text = currentJemaat.KOTA
        tvJemaatDKecamatan.text = currentJemaat.KECAMATAN
        tvJemaatDKelurahan.text = currentJemaat.KELURAHAN
        tvJemaatDRT.text = currentJemaat.RT
        tvJemaatDRW.text = currentJemaat.RW

        val baptis = currentJemaat.BAPTIS

        if (baptis!!){
            tvJemaatDBaptis.text = "Sudah"
            tvJemaatDTempatBaptis.text = currentJemaat.TEMPAT_BAPTIS
            tvJemaatDTglBaptis.text = currentJemaat.TANGGAL_BAPTIS
            tvJemaatDNoSertifikat.text = currentJemaat.NO_SERTIFIKAT
        }
        tvJemaatDNote.text = currentJemaat.NOTE

        if (currentJemaat.IMAGE != "")
            Glide.with(this).load(currentJemaat.IMAGE).into(ivDJemaatImage)

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_JEMAAT_BY_KEY_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                pbJemaatD.visibility = View.GONE
                val item = dataSnapshot.getValue(Jemaat::class.java)
                if (item != null) {
                    currentJemaat = item

                    fetchData()
                }
            }
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.DELETE_SUCCESS.toString()){
            toast("Delete Success")
            pbJemaatD.visibility = View.GONE
            finish()
        }else
            toast(message)
    }

    override fun showProvinsi(provinsi: List<provinsi>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showKotaKabupaten(kotaKabupaten: List<KotaKabupaten>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showKecamatan(kecamatan: List<Kecamatan>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showKelurahan(kelurahan: List<Kelurahan>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
