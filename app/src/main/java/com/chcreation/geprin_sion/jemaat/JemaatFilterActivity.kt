package com.chcreation.geprin_sion.jemaat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_jemaat_filter.*
import kotlinx.android.synthetic.main.activity_manage_jemaat.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class JemaatFilterActivity : AppCompatActivity(), MainView, DaerahIndonesiaView {

    private lateinit var spGenderAdapter: ArrayAdapter<String>
    private lateinit var spBaptisAdapter: ArrayAdapter<String>
    private lateinit var spGolDarahAdapter: ArrayAdapter<String>
    private lateinit var spProvinsiAdapter: ArrayAdapter<String>
    private lateinit var spKotaAdapter: ArrayAdapter<String>
    private lateinit var spKecamatanAdapter: ArrayAdapter<String>
    private lateinit var spKelurahanAdapter: ArrayAdapter<String>
    private var provinsiSpinnerItems = arrayListOf<String>()
    private var kotaSpinnerItems = arrayListOf<String>("")
    private var kecamatanSpinnerItems = arrayListOf<String>("")
    private var kelurahanSpinnerItems = arrayListOf<String>("")
    private var provinsiItems = arrayListOf<provinsi>()
    private var kotaItems = arrayListOf<KotaKabupaten>()
    private var kecamatanItems = arrayListOf<Kecamatan>()
    private var kelurahanItems = arrayListOf<Kelurahan>()
    private var genderItems = arrayListOf(EGender.All.toString(),EGender.Pria.toString(), EGender.Perempuan.toString())
    private var baptisItems = arrayListOf(EBaptis.All.toString(),EBaptis.Yes.toString(),EBaptis.No.toString())
    private var golDarahItems = arrayListOf(
        EGolDarah.All.toString(),
        EGolDarah.A.toString(),
        EGolDarah.B.toString(),
        EGolDarah.O.toString(),
        EGolDarah.AB.toString())
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: JemaatPresenter
    private lateinit var mAuth: FirebaseAuth
    private var loadProvinsi = false
    private var loadKota = false
    private var loadKecamatan = false
    private var loadKelurahan = false

    companion object{
        var selectedGender = EGender.All.toString()
        var selectedBaptis = EBaptis.All.toString()
        var selectedGolDarah = EGolDarah.All.toString()
        var selectedProvinsi = ""
        var selectedKota = ""
        var selectedKecamatan = ""
        var selectedKelurahan = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jemaat_filter)

        supportActionBar?.title = "Filter"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = JemaatPresenter(this,this,mAuth,mDatabase, this)

        initSpinner()

        btnJemaatApplyFilter.onClick {
            btnJemaatApplyFilter.startAnimation(normalClickAnimation())
            finish()
        }

        btnJemaatRemoveFilter.onClick {
            btnJemaatRemoveFilter.startAnimation(normalClickAnimation())
            clearFilterData()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.retrieveProvinsi()
    }

    override fun onBackPressed() {
        clearFilterData()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home ->{
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearFilterData(){
        selectedGender = EGender.All.toString()
        selectedBaptis = EBaptis.All.toString()
        selectedGolDarah = EGolDarah.All.toString()
        selectedProvinsi = ""
        selectedKota = ""
        selectedKecamatan = ""
        selectedKelurahan = ""
    }

    private fun initSpinner(){
        spGenderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,genderItems)
        spGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatFilterJenisKelamin.adapter = spGenderAdapter
        spJemaatFilterJenisKelamin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedGender = genderItems[position]
            }

        }
        spJemaatFilterJenisKelamin.gravity = Gravity.CENTER

        spGolDarahAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,golDarahItems)
        spGolDarahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatFilterGolDarah.adapter = spGolDarahAdapter
        spJemaatFilterGolDarah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedGolDarah = golDarahItems[position]
            }

        }
        spJemaatFilterGolDarah.gravity = Gravity.CENTER

        spBaptisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,baptisItems)
        spBaptisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatFilterBaptis.adapter = spBaptisAdapter
        spJemaatFilterBaptis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedBaptis = baptisItems[position]
            }

        }
        spJemaatFilterBaptis.gravity = Gravity.CENTER

        spProvinsiAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,provinsiSpinnerItems)
        spProvinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatProvinsi.adapter = spProvinsiAdapter
        spJemaatProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedProvinsi = provinsiSpinnerItems[position]
                if (selectedProvinsi != "")
                    presenter.retrieveKotaKabupaten(provinsiItems[position].id.toString())
            }

        }
        spJemaatProvinsi.gravity = Gravity.CENTER

        // kota spinner
        spKotaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kotaSpinnerItems)
        spKotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatKotaKabupaten.adapter = spKotaAdapter
        spJemaatKotaKabupaten.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedKota = kotaSpinnerItems[position]
                if (selectedKota != "")
                    presenter.retrieveKecamatan(kotaItems[position].id.toString())
            }

        }
        spJemaatKotaKabupaten.gravity = Gravity.CENTER

        // kecamatan spinner
        spKecamatanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kecamatanSpinnerItems)
        spKecamatanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatKecamatan.adapter = spKecamatanAdapter
        spJemaatKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedKecamatan = kecamatanSpinnerItems[position]
                if (selectedKecamatan != "")
                    presenter.retrieveKelurahan(kecamatanItems[position].id.toString())
            }

        }
        spJemaatKecamatan.gravity = Gravity.CENTER

        // kelurahan
        spKelurahanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kelurahanSpinnerItems)
        spKelurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spJemaatKelurahan.adapter = spKelurahanAdapter
        spJemaatKelurahan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedKelurahan = kelurahanSpinnerItems[position]
            }

        }
        spJemaatKelurahan.gravity = Gravity.CENTER


    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showProvinsi(provinsi: List<provinsi>) {
        provinsiItems.clear()
        provinsiSpinnerItems.clear()
        provinsiItems.add(provinsi())

        provinsiItems.addAll(provinsi)
        for (data in provinsiItems){
            provinsiSpinnerItems.add(data.nama.toString())
        }

        if (!loadProvinsi){
            loadProvinsi = true
            if (selectedProvinsi != "")
                spJemaatProvinsi.setSelection(provinsiSpinnerItems.indexOf(selectedProvinsi))
        }

        spProvinsiAdapter.notifyDataSetChanged()
    }

    override fun showKotaKabupaten(kotaKabupaten: List<KotaKabupaten>) {
        kotaItems.clear()
        kotaSpinnerItems.clear()
        kotaItems.add(KotaKabupaten())

        kotaItems.addAll(kotaKabupaten)
        for (data in kotaItems){
            kotaSpinnerItems.add(data.nama.toString())
        }

        if (!loadKota){
            loadKota = true
            if (selectedKota != "")
                spJemaatKotaKabupaten.setSelection(kotaSpinnerItems.indexOf(selectedKota))
        }
        spKotaAdapter.notifyDataSetChanged()
    }


    override fun showKecamatan(kecamatan: List<Kecamatan>) {
        kecamatanItems.clear()
        kecamatanSpinnerItems.clear()
        kecamatanItems.add(Kecamatan())

        kecamatanItems.addAll(kecamatan)
        for (data in kecamatanItems){
            kecamatanSpinnerItems.add(data.nama.toString())
        }

        if (!loadKecamatan){
            loadKecamatan = true
            if (selectedKecamatan != "")
                spJemaatKecamatan.setSelection(kecamatanSpinnerItems.indexOf(selectedKecamatan))
        }
        spKecamatanAdapter.notifyDataSetChanged()
    }

    override fun showKelurahan(kelurahan: List<Kelurahan>) {
        kelurahanItems.clear()
        kelurahanSpinnerItems.clear()
        kelurahanItems.add(Kelurahan())

        kelurahanItems.addAll(kelurahan)
        for (data in kelurahanItems){
            kelurahanSpinnerItems.add(data.nama.toString())
        }
        if (!loadKelurahan){
            loadKelurahan = true
            if (selectedKelurahan != "")
                spJemaatKelurahan.setSelection(kelurahanSpinnerItems.indexOf(selectedKelurahan))
        }

        spKelurahanAdapter.notifyDataSetChanged()
    }
}
