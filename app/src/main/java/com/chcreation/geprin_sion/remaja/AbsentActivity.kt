package com.chcreation.geprin_sion.remaja

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EAbsent
import com.chcreation.geprin_sion.model.EAbsentType
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.Remaja
import com.chcreation.geprin_sion.presenter.RemajaPresenter
import com.chcreation.geprin_sion.remaja.statistic.StatiscticRemajaActivity
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.simpleDateFormat
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_absent.*
import kotlinx.android.synthetic.main.activity_manage_remaja.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AbsentActivity : AppCompatActivity(), MainView {

    private var cal = Calendar.getInstance()
    private lateinit var presenter: RemajaPresenter
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
        presenter = RemajaPresenter(this,mAuth,mDatabase,this)
        remajaItems.clear()

        llAbsentAddRemaja.onClick {
            llAbsentAddRemaja.startAnimation(normalClickAnimation())

            startActivity<AddRemajaActivity>()
        }

        layoutRemajaStatistic.onClick {
            layoutRemajaStatistic.startAnimation(normalClickAnimation())

            startActivity<StatiscticRemajaActivity>()
        }

        layoutRemajaExport.onClick {
            layoutRemajaExport.startAnimation(normalClickAnimation())

            createXlsx()
        }

        layoutRemajaAbsent.onClick {
            layoutRemajaAbsent.startAnimation(normalClickAnimation())
            var type = EAbsentType.All.toString()

            val dateListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                startActivity(intentFor<NewAbsentActivity>(EAbsent.ABSENT_DATE.toString() to simpleDateFormat().format(cal.time),
                    EAbsent.TYPE.toString() to type))
            }

            val options = mutableListOf("Semua Remaja", "Anggota Padus")

            selector("Absent",options) {
                    dialogInterface, i ->
                when(i){
                    0 -> {
                        type = EAbsentType.All.toString()
                        DatePickerDialog(this@AbsentActivity,
                            dateListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                    1 -> {
                        type = EAbsentType.PADUS.toString()
                        DatePickerDialog(this@AbsentActivity,
                            dateListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }
            }
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

    private fun createXlsx() {
        try{
            toast("Generating XLSX . . . ")
            val fileName = "Data Remaja.xlsx"
            val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//            val dir = File(ctx.filesDir,"test")
//            if (!dir.exists())
//                dir.mkdir()
            GlobalScope.launch {
                val fileOutputStream = FileOutputStream(File(path,"/$fileName"))

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Data Jemaat")
                var row = sheet.createRow(0)
                row.createCell(0).setCellValue("Nama")
                row.createCell(1).setCellValue("Jenis Kelamin")
                row.createCell(2).setCellValue("TTL")
                row.createCell(3).setCellValue("No Telepon")
                row.createCell(4).setCellValue("Alamat")
                row.createCell(5).setCellValue("Sekolah")
                row.createCell(6).setCellValue("Kelas")
                row.createCell(7).setCellValue("Hobby")
                row.createCell(8).setCellValue("Warna Favorit")
                row.createCell(9).setCellValue("Catatan")

                for ((index,data) in remajaItems.withIndex()){
                    row = sheet.createRow(index+1)

                    row.createCell(0).setCellValue(data.NAMA)
                    row.createCell(1).setCellValue(data.GENDER)
                    row.createCell(2).setCellValue("${data.TEMPAT_LAHIR}, ${data.TANGGAL_LAHIR}")
                    row.createCell(3).setCellValue(data.NO_TEL)
                    row.createCell(4).setCellValue(data.ALAMAT)
                    row.createCell(5).setCellValue(data.SEKOLAH)
                    row.createCell(6).setCellValue(data.KELAS)
                    row.createCell(7).setCellValue(data.HOBBY)
                    row.createCell(8).setCellValue(data.WARNA_FAV)
                    row.createCell(9).setCellValue(data.NOTE)
                }

                workbook.write(fileOutputStream)
                fileOutputStream.close()
                share(
                    FileProvider.getUriForFile(this@AbsentActivity, "com.example.android.geprin_sion.fileprovider",
                        File(path,"/$fileName")
                    ))
            }

        }catch(e:Exception){
            toast(e.message.toString())
            e.printStackTrace()
        }
    }

    private fun share(uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "application/xlsx"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Data Remaja"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toast("No App Available")
        }
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
                remajaItems.clear()
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
