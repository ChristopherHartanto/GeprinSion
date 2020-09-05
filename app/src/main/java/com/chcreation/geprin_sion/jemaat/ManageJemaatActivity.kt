package com.chcreation.geprin_sion.jemaat

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.util.dateFormat
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.showError
import com.chcreation.geprin_sion.util.simpleDateFormat
import com.chcreation.geprin_sion.view.DaerahIndonesiaView
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_manage_jemaat.*
import kotlinx.android.synthetic.main.activity_new_jemaat.*
import kotlinx.android.synthetic.main.activity_new_jemaat.layoutJemaatBaptis
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.util.*

class ManageJemaatActivity : AppCompatActivity(), MainView, DaerahIndonesiaView {

    private var cal = Calendar.getInstance()
    private lateinit var tanggalBaptisListener : DatePickerDialog.OnDateSetListener
    private lateinit var tanggalLahirListener : DatePickerDialog.OnDateSetListener
    private lateinit var spGenderAdapter: ArrayAdapter<String>
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
    private var genderItems = arrayListOf(EGender.Pria.toString(),EGender.Perempuan.toString())
    private var golDarahItems = arrayListOf(EGolDarah.A.toString(),EGolDarah.B.toString(),EGolDarah.O.toString(),EGolDarah.AB.toString())
    private var selectedGender = EGender.Pria.toString()
    private var selectedGolDarah = EGolDarah.A.toString()
    private var selectedProvinsi = ""
    private var selectedKota = ""
    private var selectedKecamatan = ""
    private var selectedKelurahan = ""
    private lateinit var storage: StorageReference
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var jemaatId = ""
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: JemaatPresenter
    private lateinit var mAuth: FirebaseAuth
    private var currentJemaat = Jemaat()
    private var loadProvinsi = false
    private var loadKota = false
    private var loadKecamatan = false
    private var loadKelurahan = false
    private var currentJemaatKey = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_jemaat)

        supportActionBar?.title = "Edit Jemaat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = JemaatPresenter(this,this,mAuth,mDatabase, this)
        storage = FirebaseStorage.getInstance().reference
        jemaatId = generateJemaatId()
        initDateListener()
        initSpinners()
        currentJemaatKey = intent.extras?.getInt(EJemaat.KEY.toString()) ?: 0

        swMJemaatBaptis.onCheckedChange { buttonView, isChecked ->
            if (swMJemaatBaptis.isChecked){
                layoutJemaatBaptis.visibility = View.VISIBLE
            }else{
                layoutJemaatBaptis.visibility = View.GONE
            }
        }

        ivMJemaatImage.onClick {
            selectImage()
        }

        tvMJemaatTanggalBaptis.onClick {
            DatePickerDialog(this@ManageJemaatActivity,
                tanggalBaptisListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvMJemaatTanggalLahir.onClick {
            DatePickerDialog(this@ManageJemaatActivity,
                tanggalLahirListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnMJemaatSave.onClick {
            btnMJemaatSave.startAnimation(normalClickAnimation())

            if (filePath == null)
                saveJemaat("")
            else
                uploadImage()
        }
    }

    override fun onStart() {
        super.onStart()

        val id = intent.extras?.getString(EJemaat.ID.toString(),"")
        if (id != null) {
            presenter.retrieveJemaatByKey(currentJemaatKey.toString())
        }
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

    private fun saveJemaat(newImage: String?){
        loading()

        val nama = etMJemaatName.text.toString()
        val alamat = etMJemaatAlamat.text.toString()
        val golDarah = selectedGolDarah
        val jenisKelamin = selectedGender
        val noTel = etMJemaatNoHp.text.toString()
        val note = etMJemaatNote.text.toString()
        val baptis = swMJemaatBaptis.isChecked
        val tanggalLahir = tvMJemaatTanggalLahir.text.toString()
        val tempatLahir = etMJemaatTempatLahir.text.toString()
        val tanggalBaptis = tvMJemaatTanggalBaptis.text.toString()
        val tempatBaptis = etMJemaatTempatBaptis.text.toString()
        val noSertifikat = etMJemaatNoSertifikat.text.toString()
        val rt = etMJemaatRT.text.toString()
        val rw = etMJemaatRW.text.toString()

        val image = if (newImage == "") currentJemaat.IMAGE else newImage

        presenter.updateJemaat(Jemaat(nama,selectedProvinsi,selectedKota,selectedKecamatan,selectedKelurahan,rt,rw,alamat,jenisKelamin,golDarah,tempatLahir,tanggalLahir,noTel,note,
            baptis,noSertifikat,tempatBaptis,tanggalBaptis,
            currentJemaat.CREATED_DATE,
            dateFormat().format(Date()),
            currentJemaat.CREATED_BY,
            mAuth.currentUser!!.uid,image,currentJemaat.ID), currentJemaatKey.toString()
        )
    }

    private fun uploadImage(){
        if(filePath != null){
            loading()

            val ref = storage.child(ETable.JEMAAT.toString())
                .child(jemaatId)

            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveJemaat(downloadUri.toString())
                } else {
                    toast("Failed to Save Image")
                    endLoading()
                }
            }.addOnFailureListener{
                toast("Error : ${it.message}")
                endLoading()
            }
        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = dateFormat().format(Date(1000))
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = "$absolutePath"

        }

    }

    // Select Image method
    private fun selectImage() { // Defining Implicit Intent to mobile gallery

        intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {
                    if (ContextCompat.checkSelfPermission(this@ManageJemaatActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageJemaatActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@ManageJemaatActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageJemaatActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),READ_PERMISION
                        )
                    else
                        openGallery()
                }
            }
        }

    }

    private fun openCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.geprin_sion.fileprovider",
                        it
                    )
                    Log.d("uri: ",photoURI.toString())
                    filePath = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                }
            }
        }
    }

    private fun openGallery(){
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(
            intent,
            PICK_IMAGE_GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                ivMJemaatImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: Exception) {
                filePath = null
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            filePath = data.data
            currentPhotoPath = getRealPathFromURI(filePath!!)
            try { // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                ivMJemaatImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: IOException) { // Log the exception
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera()
            }
            else
                toast("Permission Denied")
        }
        else if (requestCode == READ_PERMISION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery()
            }
            else
                toast("Permission Denied")
        }
    }

    private fun rotateImage(bitmapSource : Bitmap) : Bitmap {
        val ei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ExifInterface(File(currentPhotoPath))
        } else {
            ExifInterface(currentPhotoPath)
        };
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED);

        var rotatedBitmap = null;
        var degree = 0F
        when(orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 -> {
                degree = 90F
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                degree = 180F
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                degree = 270F
            }
            ExifInterface.ORIENTATION_NORMAL -> {
                degree = 360F
            }
        }

        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.width, bitmapSource.height,
            matrix, true)
    }

    fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private fun generateJemaatId() : String{
        return "J${mDatabase.push().key.toString()}"
    }

    private fun initSpinners(){
        spGenderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,genderItems)
        spGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatJenisKelamin.adapter = spGenderAdapter
        spMJemaatJenisKelamin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatJenisKelamin.gravity = Gravity.CENTER

        spGolDarahAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,golDarahItems)
        spGolDarahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatGolDarah.adapter = spGolDarahAdapter
        spMJemaatGolDarah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatGolDarah.gravity = Gravity.CENTER

        spProvinsiAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,provinsiSpinnerItems)
        spProvinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatProvinsi.adapter = spProvinsiAdapter
        spMJemaatProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatProvinsi.gravity = Gravity.CENTER

        // kota spinner
        spKotaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kotaSpinnerItems)
        spKotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatKotaKabupaten.adapter = spKotaAdapter
        spMJemaatKotaKabupaten.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatKotaKabupaten.gravity = Gravity.CENTER

        // kecamatan spinner
        spKecamatanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kecamatanSpinnerItems)
        spKecamatanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatKecamatan.adapter = spKecamatanAdapter
        spMJemaatKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatKecamatan.gravity = Gravity.CENTER

        // kelurahan
        spKelurahanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,kelurahanSpinnerItems)
        spKelurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMJemaatKelurahan.adapter = spKelurahanAdapter
        spMJemaatKelurahan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spMJemaatKelurahan.gravity = Gravity.CENTER
    }

    private fun initDateListener(){
        tanggalBaptisListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tvMJemaatTanggalBaptis.text = simpleDateFormat().format(cal.time)
            }

        tanggalLahirListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tvMJemaatTanggalLahir.text = simpleDateFormat().format(cal.time)
            }
    }

    private fun fetchData(){
        etMJemaatName.setText(currentJemaat.NAMA)
        etMJemaatAlamat.setText(currentJemaat.ALAMAT)
        etMJemaatNoHp.setText(currentJemaat.NO_TEL)
        etMJemaatNote.setText(currentJemaat.NOTE)
        spMJemaatGolDarah.setSelection(golDarahItems.indexOf(currentJemaat.GOL_DARAH))
        spMJemaatJenisKelamin.setSelection(genderItems.indexOf(currentJemaat.GENDER))
        swMJemaatBaptis.isChecked = currentJemaat.BAPTIS!!
        etMJemaatTempatBaptis.setText(currentJemaat.TEMPAT_BAPTIS)
        tvMJemaatTanggalLahir.text = currentJemaat.TANGGAL_LAHIR
        etMJemaatTempatLahir.setText(currentJemaat.TEMPAT_LAHIR)
        tvMJemaatTanggalBaptis.text = currentJemaat.TANGGAL_BAPTIS
        etMJemaatNoSertifikat.setText(currentJemaat.NO_SERTIFIKAT)
        etMJemaatCreatedDate.setText(currentJemaat.CREATED_DATE)
        etMJemaatUpdatedDate.setText(currentJemaat.UPDATED_DATE)

        etMJemaatRT.setText(currentJemaat.RT)
        etMJemaatRW.setText(currentJemaat.RW)

        presenter.getUserName(currentJemaat.CREATED_BY.toString()){
            etMJemaatCreatedBy.setText(it)
        }
        presenter.getUserName(currentJemaat.UPDATED_BY.toString()){
            etMJemaatUpdatedBy.setText(it)
        }
        if (currentJemaat.IMAGE != "")
            Glide.with(this).load(currentJemaat.IMAGE).into(ivMJemaatImage)

    }

    private fun loading(){
        pbMJemaat.visibility = View.VISIBLE
    }

    private fun endLoading(){
        pbMJemaat.visibility = View.GONE
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_JEMAAT_BY_KEY_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Jemaat::class.java)
                if (item != null) {
                    currentJemaat = item
                    presenter.retrieveProvinsi()
                    fetchData()
                }
            }
            pbMJemaat.visibility = View.GONE
            layoutMJemaat.visibility = View.VISIBLE
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            toast("Save Success")
            finish()
            btnMJemaatSave.isEnabled = true
            endLoading()
        }
        else{
            btnMJemaatSave.isEnabled = true
            toast(message)
            endLoading()
        }
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
            spMJemaatProvinsi.setSelection(provinsiSpinnerItems.indexOf(currentJemaat.PROVINSI))
            loadProvinsi = true
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
            spMJemaatKotaKabupaten.setSelection(kotaSpinnerItems.indexOf(currentJemaat.KOTA))
            loadKota = true
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
            spMJemaatKecamatan.setSelection(kecamatanSpinnerItems.indexOf(currentJemaat.KECAMATAN))
            loadKecamatan = true
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
            spMJemaatKelurahan.setSelection(kelurahanSpinnerItems.indexOf(currentJemaat.KELURAHAN))
            loadKelurahan = true
        }
        spKelurahanAdapter.notifyDataSetChanged()
    }
}

