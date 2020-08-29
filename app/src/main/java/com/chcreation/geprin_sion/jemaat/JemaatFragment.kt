package com.chcreation.geprin_sion.jemaat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.main.MainActivity
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.util.dateFormat
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_jemaat.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.anko.noButton
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton
import java.io.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class JemaatFragment : Fragment(), MainView, DaerahIndonesiaView {

    private lateinit var adapter: JemaatRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: JemaatPresenter
    private var jemaatItems = mutableListOf<Jemaat>()
    private var jemaatKeys = mutableListOf<Int>()
    private var filteredJemaatItems = mutableListOf<Jemaat>()
    private var filterJemaatKeys = mutableListOf<Int>()
    private lateinit var spGenderAdapter: ArrayAdapter<String>
    private lateinit var spBaptisAdapter: ArrayAdapter<String>
    private lateinit var spGolDarahAdapter: ArrayAdapter<String>
    private var genderItems = arrayListOf(EGender.All.toString(),EGender.Pria.toString(),EGender.Perempuan.toString())
    private var baptisItems = arrayListOf(EBaptis.All.toString(),EBaptis.Yes.toString(),EBaptis.No.toString())
    private var golDarahItems = arrayListOf(
        EGolDarah.All.toString(),
        EGolDarah.A.toString(),
        EGolDarah.B.toString(),
        EGolDarah.O.toString(),
        EGolDarah.AB.toString())
    private var selectedGender = EGender.All.toString()
    private var selectedGolDarah = EGolDarah.All.toString()
    private var selectedBaptis = EBaptis.All.toString()
    private var searchFilter = ""
    private var WRITE_PERMISION = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jemaat, container, false)
    }

    companion object{
        var active = false // to end application
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = JemaatPresenter(this,this,mAuth,mDatabase, ctx)
        srJemaat.setColorSchemeColors(Color.BLUE, Color.RED)

        adapter = JemaatRecyclerViewAdapter(ctx,filteredJemaatItems){
            startActivity(intentFor<JemaatDetailActivity>(EJemaat.ID.toString() to filteredJemaatItems[it].ID,
                EJemaat.KEY.toString() to filterJemaatKeys[it]))
        }

        val linearLayoutManager = LinearLayoutManager(ctx)
//        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true
        rvJemaat.adapter = adapter
        rvJemaat.layoutManager = LinearLayoutManager(ctx)

        fabJemaat.onClick {
            val options = mutableListOf("Add New","Export XLSX")
            selector("Options",options){dialogInterface, i ->
                when(i){
                    0->{
                        startActivity<NewJemaatActivity>()
                    }
                    1->{
                        alert("Do You Want Generate to XLSX Format ?"){
                            title = "Generate XLSX"
                            yesButton {
                                if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED)
                                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),WRITE_PERMISION)
                                else
                                    createXlsx()
                            }
                            noButton {  }
                        }.show()
                    }
                }
            }
        }

        srJemaat.onRefresh {
            presenter.retrieveJemaats()
        }

        svJemaat.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter = newText
                filterData()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })

        initPopup()
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveJemaats()
    }

    override fun onResume() {
        super.onResume()
        active = true
    }
    override fun onPause() {
        super.onPause()
        active = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_PERMISION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                createXlsx()
            }
            else
                toast("Permission Denied")
        }
    }

    private fun initPopup(){
        spGenderAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,genderItems)
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

        spGolDarahAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,golDarahItems)
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

        spBaptisAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,baptisItems)
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

        btnJemaatApplyFilter.onClick {
            btnJemaatApplyFilter.startAnimation(normalClickAnimation())
            cvJemaatPopUpFilter.visibility = View.GONE
            tvJemaatOpenPopupFilter.text = "Filter"

            tvJemaatFilterGender.text = selectedGender
            tvJemaatFilterGolDarah.text = selectedGolDarah

            filterData()
        }

        tvJemaatOpenPopupFilter.onClick {
            cvJemaatPopUpFilter.visibility = if (cvJemaatPopUpFilter.isVisible) View.GONE else View.VISIBLE
            tvJemaatOpenPopupFilter.text = if (tvJemaatOpenPopupFilter.isVisible) "Close" else "Filter"
        }
    }

    private fun filterData(){
        filteredJemaatItems.clear()
        filterJemaatKeys.clear()

        for((index,data) in jemaatItems.withIndex()){
            val baptis = if (data.BAPTIS!!) EBaptis.Yes else EBaptis.No

            if ((baptis.toString() == selectedBaptis || selectedBaptis == EBaptis.All.toString())
                && (data.GENDER == selectedGender || selectedGender == EGender.All.toString())
                && (data.GOL_DARAH == selectedGolDarah || selectedGolDarah == EGolDarah.All.toString())
                && (data.NAMA!!.toLowerCase(Locale.getDefault()).contains(searchFilter) || searchFilter == "")){
                filterJemaatKeys.add(jemaatKeys[index])
                filteredJemaatItems.add(jemaatItems[index])
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun createXlsx() {
        try{
            toast("Generating XLSX . . . ")
            val fileName = "Data Jemaat${dateFormat().format(Date())}.xlsx"
            val path = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//            val dir = File(ctx.filesDir,"test")
//            if (!dir.exists())
//                dir.mkdir()
            GlobalScope.launch {
                val fileOutputStream = FileOutputStream(File(path,"/$fileName"))

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Data Jemaat")
                var row = sheet.createRow(0)
                row.createCell(0).setCellValue("Nama")
                row.createCell(1).setCellValue("Alamat")
                row.createCell(2).setCellValue("Jenis Kelamin")
                row.createCell(3).setCellValue("Golongan Darah")
                row.createCell(4).setCellValue("TTL")
                row.createCell(5).setCellValue("No Telepon")
                row.createCell(6).setCellValue("No Sertifikat Baptis")
                row.createCell(7).setCellValue("Tempat Baptis")
                row.createCell(8).setCellValue("Tanggal Baptis")
                row.createCell(9).setCellValue("Catatan")

                for ((index,data) in filteredJemaatItems.withIndex()){
                    row = sheet.createRow(index+1)

                    row.createCell(0).setCellValue(data.NAMA)
                    row.createCell(1).setCellValue(data.ALAMAT)
                    row.createCell(2).setCellValue(data.GENDER)
                    row.createCell(3).setCellValue(data.GOL_DARAH)
                    row.createCell(4).setCellValue("${data.TEMPAT_LAHIR}, ${data.TANGGAL_LAHIR}")
                    row.createCell(5).setCellValue(data.NO_TEL)
                    row.createCell(6).setCellValue(data.NO_SERTIFIKAT)
                    row.createCell(7).setCellValue(data.TEMPAT_BAPTIS)
                    row.createCell(8).setCellValue(data.TANGGAL_BAPTIS)
                    row.createCell(9).setCellValue(data.NOTE)
                }

                workbook.write(fileOutputStream)
                fileOutputStream.close()
                createNotificationChannel(fileName,File(path,"/$fileName").toString())
                share(FileProvider.getUriForFile(ctx, "com.example.android.geprin_sion.fileprovider",
                    File(path,"/$fileName")))
            }

        }catch(e:Exception){
            toast(e.message.toString())
            e.printStackTrace()
        }
    }

    private fun readXlsx(file: String){
        try{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(file));
            startActivity(browserIntent)
        } catch (e: Exception) {
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
            startActivity(Intent.createChooser(intent, "Share Data Jemaat"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toast("No App Available")
        }
    }

    private fun createNotificationChannel(fileName: String, dir: String) {
        val intent = Intent(ctx, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("0", "WorkManager", NotificationManager.IMPORTANCE_DEFAULT)
            ctx.notificationManager.createNotificationChannel(channel)
        }

        val bitmap = BitmapFactory.decodeResource(ctx.resources,R.drawable.icon)

        val notification = NotificationCompat.Builder(ctx, "0")
            .setContentTitle("Generate $fileName Success")
            .setContentText("Saved to $dir ")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.icon)
            .setLargeIcon(bitmap)
            .setVibrate(longArrayOf(200,200))

        ctx.notificationManager.notify(1, notification.build())
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_JEMAAT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    jemaatItems.clear()
                    jemaatKeys.clear()
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Jemaat::class.java)

                        if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()) {
                            jemaatKeys.add(data.key!!.toInt())
                            jemaatItems.add(item)
                        }
                    }
                    //jemaatItems.reverse()
                    srJemaat.isRefreshing = false
                    filterData()
                }
            }
            srJemaat.isRefreshing = false
        }
    }

    override fun response(message: String) {
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
