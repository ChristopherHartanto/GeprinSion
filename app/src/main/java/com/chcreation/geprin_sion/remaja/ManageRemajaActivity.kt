package com.chcreation.geprin_sion.remaja

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
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.RemajaPresenter
import com.chcreation.geprin_sion.util.dateFormat
import com.chcreation.geprin_sion.util.showError
import com.chcreation.geprin_sion.util.simpleDateFormat
import com.chcreation.geprin_sion.view.TransactionInterface
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_remaja.*
import kotlinx.android.synthetic.main.activity_manage_remaja.*
import kotlinx.android.synthetic.main.activity_remaja_detail.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.IOException
import java.util.*

class ManageRemajaActivity : AppCompatActivity(), MainView {

    private var cal = Calendar.getInstance()
    private lateinit var tanggalLahirListener : DatePickerDialog.OnDateSetListener
    private lateinit var spGenderAdapter: ArrayAdapter<String>
    private var genderItems = arrayListOf(EGender.Pria.toString(), EGender.Perempuan.toString())
    private var selectedGender = EGender.Pria.toString()
    private lateinit var spJenisSuaraAdapter: ArrayAdapter<String>
    private var jenisSuaraItems = arrayListOf(
        EJenisSuara.SOPRAN.toString(), EJenisSuara.ALTO.toString(),
        EJenisSuara.TENOR.toString(), EJenisSuara.BASS.toString())
    private var selectedJenisSuara = EJenisSuara.SOPRAN.toString()
    private lateinit var storage: StorageReference
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: RemajaPresenter
    private var currentRemaja = Remaja()
    private var remajaKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_remaja)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Remaja"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = RemajaPresenter(this,mAuth,mDatabase,this)
        initDateListener()
        initSpinner()

        remajaKey = intent.extras!!.getString(ERemaja.ID.toString(),"")

        presenter.retrieveRemajaByKey(remajaKey)

        swRemajaMPadus.onCheckedChange { buttonView, isChecked ->
            if (swRemajaMPadus.isChecked){
                layoutRemajaMPadus.visibility = View.VISIBLE
            }else{
                layoutRemajaMPadus.visibility = View.GONE
            }
        }

        swRemajaMPelayanan.onCheckedChange { buttonView, isChecked ->
            if (swRemajaMPelayanan.isChecked){
                layoutRemajaMPelayanan1.visibility = View.VISIBLE
                layoutRemajaMPelayanan2.visibility = View.VISIBLE
                layoutRemajaMPelayanan3.visibility = View.VISIBLE
                layoutRemajaMPelayanan4.visibility = View.VISIBLE
                layoutRemajaMPelayanan5.visibility = View.VISIBLE
            }else{
                layoutRemajaMPelayanan1.visibility = View.GONE
                layoutRemajaMPelayanan2.visibility = View.GONE
                layoutRemajaMPelayanan3.visibility = View.GONE
                layoutRemajaMPelayanan4.visibility = View.GONE
                layoutRemajaMPelayanan5.visibility = View.GONE
            }
        }

        ivRemajaMImage.onClick {
            selectImage()
        }

        btnRemajaMSave.onClick {
            if (filePath != null)
                uploadImage()
            else
                saveRemaja("")
        }

        tvRemajaMTanggalLahir.onClick {
            DatePickerDialog(this@ManageRemajaActivity,
                tanggalLahirListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
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

    private fun saveRemaja(uploadImage: String){
        loading()
        val name = etRemajaMName.text.toString()
        val alamat = etRemajaMAlamat.text.toString()
        val hobby = etRemajaMHobby.text.toString()
        val sekolah = etRemajaMSekolah.text.toString()
        val kelas = etRemajaMKelas.text.toString()
        val favWarna = etRemajaMWarnaFav.text.toString()
        val gender = selectedGender
        val tempatLahir = etRemajaMTempatLahir.text.toString()
        val tanggalLahir = tvRemajaMTanggalLahir.text.toString()
        val noTel = etRemajaMNoHp.text.toString()
        val note = etRemajaMNote.text.toString()
        val isPadus = swRemajaMPadus.isChecked
        val jenisSuara = selectedJenisSuara
        val isPelayanan = swRemajaMPelayanan.isChecked
        val isLiturgos = cbRemajaMLiturgos.isChecked
        val isKolektor = cbRemajaMKolektor.isChecked
        val isLcd = cbRemajaMLCD.isChecked
        val isPenyambut = cbRemajaMPenyambut.isChecked
        val isAbsensi = cbRemajaMAbsensi.isChecked
        val isGitaris = cbRemajaMGitaris.isChecked
        val isPengurus = cbRemajaMPengurus.isChecked
        val isPianis = cbRemajaMPianis.isChecked
        val isPadus1 = cbRemajaMPadus.isChecked

        val image = if (uploadImage != "") uploadImage else currentRemaja.IMAGE

        presenter.updateRemaja(object : TransactionInterface {
            override fun handleData(data: Any, resultCode: Int) {
                endLoading()

                if (resultCode == EResultCode.SUCCESS.value){
                    toast("Success Update")
                    finish()
                }else
                    toast("Update Failed")
            }

        },Remaja(name,gender,tempatLahir,tanggalLahir,noTel,alamat,kelas,sekolah,hobby,favWarna,note,
            isPadus,jenisSuara,isPelayanan,isLiturgos,isPenyambut,isPianis,isGitaris,isLcd,isPengurus,isAbsensi,isKolektor,isPadus1,
            currentRemaja.CREATED_DATE,dateFormat().format(Date()),
            currentRemaja.CREATED_BY,mAuth.currentUser!!.uid,image,currentRemaja.ID.toString(),currentRemaja.STATUS),
            currentRemaja.ID.toString())
    }

    private fun uploadImage(){
        if(filePath != null){
            loading()

            val ref = storage.child(ETable.REMAJA.toString())
                .child(currentRemaja.ID.toString())

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
                    saveRemaja(downloadUri.toString())
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
            endLoading()
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
                    if (ContextCompat.checkSelfPermission(this@ManageRemajaActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageRemajaActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@ManageRemajaActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageRemajaActivity,
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
                ivRemajaMImage.setImageBitmap(rotateImage(bitmap))
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
                ivRemajaMImage.setImageBitmap(rotateImage(bitmap))
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

    private fun loading(){
        pbRemajaM.visibility = View.VISIBLE
        btnRemajaMSave.isEnabled = false
    }

    private fun endLoading(){
        pbRemajaM.visibility = View.GONE
        btnRemajaMSave.isEnabled = true
    }

    private fun initDateListener(){
        tanggalLahirListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tvRemajaMTanggalLahir.text = simpleDateFormat().format(cal.time)
            }
    }

    private fun initSpinner(){
        spGenderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,genderItems)
        spGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spRemajaMJenisKelamin.adapter = spGenderAdapter
        spRemajaMJenisKelamin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spRemajaMJenisKelamin.gravity = Gravity.CENTER

        spJenisSuaraAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,jenisSuaraItems)
        spJenisSuaraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spRemajaMJenisSuara.adapter = spJenisSuaraAdapter
        spRemajaMJenisSuara.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedJenisSuara = jenisSuaraItems[position]
            }

        }
        spRemajaMJenisSuara.gravity = Gravity.CENTER
    }

    private fun fetchData(){
        etRemajaMName.setText(currentRemaja.NAMA)
        supportActionBar?.title = currentRemaja.NAMA
        spRemajaMJenisKelamin.setSelection(genderItems.indexOf(currentRemaja.GENDER))
        spRemajaMJenisSuara.setSelection(jenisSuaraItems.indexOf(currentRemaja.JENIS_SUARA))

        swRemajaMPadus.isChecked = currentRemaja.IS_PADUS!!
        etRemajaMAlamat.setText(currentRemaja.ALAMAT)
        etRemajaMHobby.setText(currentRemaja.HOBBY)
        etRemajaMTempatLahir.setText(currentRemaja.TEMPAT_LAHIR)
        tvRemajaMTanggalLahir.text = currentRemaja.TANGGAL_LAHIR
        etRemajaMSekolah.setText(currentRemaja.SEKOLAH)
        etRemajaMKelas.setText(currentRemaja.KELAS)
        etRemajaMWarnaFav.setText(currentRemaja.WARNA_FAV)
        etRemajaMNoHp.setText(currentRemaja.NO_TEL)
        etRemajaMNote.setText(currentRemaja.NOTE)

        if (currentRemaja.IMAGE != "")
            Glide.with(this).load(currentRemaja.IMAGE).into(ivRemajaMImage)

        cbRemajaMAbsensi.isChecked = currentRemaja.ABSENSI!!
        cbRemajaMGitaris.isChecked = currentRemaja.GITARIS!!
        cbRemajaMPianis.isChecked = currentRemaja.PIANIS!!
        cbRemajaMLCD.isChecked = currentRemaja.LCD!!
        cbRemajaMKolektor.isChecked = currentRemaja.KOLEKTOR!!
        cbRemajaMLiturgos.isChecked = currentRemaja.LITURGOS!!
        cbRemajaMPadus.isChecked = currentRemaja.IS_PADUS!!
        cbRemajaMPenyambut.isChecked = currentRemaja.PENYAMBUT!!
        cbRemajaMPengurus.isChecked = currentRemaja.PENGURUS!!
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_REMAJA_BY_KEY_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                pbRemajaM.visibility = View.GONE
                for (data in dataSnapshot.children){
                    val item = data.getValue(Remaja::class.java)
                    if (item != null) {
                        currentRemaja = item
                        fetchData()
                    }
                }
            }
            pbRemajaM.visibility = View.GONE
            layoutRemajaM.visibility = View.VISIBLE
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
