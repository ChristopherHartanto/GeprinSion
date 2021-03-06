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
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.RemajaPresenter
import com.chcreation.geprin_sion.util.dateFormat
import com.chcreation.geprin_sion.util.normalClickAnimation
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_remaja.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.util.*

class AddRemajaActivity : AppCompatActivity(), MainView {

    private var cal = Calendar.getInstance()
    private lateinit var tanggalLahirListener : DatePickerDialog.OnDateSetListener
    private lateinit var spGenderAdapter: ArrayAdapter<String>
    private var genderItems = arrayListOf(EGender.Pria.toString(), EGender.Perempuan.toString())
    private var selectedGender = EGender.Pria.toString()
    private lateinit var spJenisSuaraAdapter: ArrayAdapter<String>
    private var jenisSuaraItems = arrayListOf(EJenisSuara.SOPRAN.toString(), EJenisSuara.ALTO.toString(),
        EJenisSuara.TENOR.toString(),EJenisSuara.BASS.toString())
    private var selectedJenisSuara = EJenisSuara.SOPRAN.toString()
    private lateinit var storage: StorageReference
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var remajaId = ""
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: RemajaPresenter
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_remaja)

        supportActionBar?.title = "New Remaja"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = RemajaPresenter(this,mAuth,mDatabase, this)
        storage = FirebaseStorage.getInstance().reference
        remajaId = generateRemajaId()
        initDateListener()
        initSpinner()

        swRemajaPadus.onCheckedChange { buttonView, isChecked ->
            if (swRemajaPadus.isChecked){
                layoutRemajaPadus.visibility = View.VISIBLE
            }else{
                layoutRemajaPadus.visibility = View.GONE
            }
        }

        swRemajaPelayanan.onCheckedChange { buttonView, isChecked ->
            if (swRemajaPelayanan.isChecked){
                layoutRemajaPelayanan1.visibility = View.VISIBLE
                layoutRemajaPelayanan2.visibility = View.VISIBLE
                layoutRemajaPelayanan3.visibility = View.VISIBLE
                layoutRemajaPelayanan4.visibility = View.VISIBLE
                layoutRemajaPelayanan5.visibility = View.VISIBLE
            }else{
                layoutRemajaPelayanan1.visibility = View.GONE
                layoutRemajaPelayanan2.visibility = View.GONE
                layoutRemajaPelayanan3.visibility = View.GONE
                layoutRemajaPelayanan4.visibility = View.GONE
                layoutRemajaPelayanan5.visibility = View.GONE
            }
        }

        ivRemajaImage.onClick {
            selectImage()
        }

        btnRemajaSave.onClick {
            btnRemajaSave.startAnimation(normalClickAnimation())

            if (filePath != null)
                uploadImage()
            else
                saveRemaja("")
        }

        tvRemajaTanggalLahir.onClick {
            DatePickerDialog(this@AddRemajaActivity,
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

    private fun saveRemaja(image: String){
        loading()
        val name = etRemajaName.text.toString()
        val alamat = etRemajaAlamat.text.toString()
        val hobby = etRemajaHobby.text.toString()
        val sekolah = etRemajaSekolah.text.toString()
        val kelas = etRemajaKelas.text.toString()
        val favWarna = etRemajaWarnaFav.text.toString()
        val gender = selectedGender
        val tempatLahir = etRemajaTempatLahir.text.toString()
        val tanggalLahir = tvRemajaTanggalLahir.text.toString()
        val noTel = etRemajaNoHp.text.toString()
        val note = etRemajaNote.text.toString()
        val isPadus = swRemajaPadus.isChecked
        val jenisSuara = selectedJenisSuara
        val isPelayanan = swRemajaPelayanan.isChecked
        val isLiturgos = cbRemajaLiturgos.isChecked
        val isKolektor = cbRemajaKolektor.isChecked
        val isLcd = cbRemajaLCD.isChecked
        val isPenyambut = cbRemajaPenyambut.isChecked
        val isAbsensi = cbRemajaAbsensi.isChecked
        val isGitaris = cbRemajaGitaris.isChecked
        val isPengurus = cbRemajaPengurus.isChecked
        val isPianis = cbRemajaPianis.isChecked
        val isPadus1 = cbRemajaPadus.isChecked

        presenter.createRemaja(object : TransactionInterface{
            override fun handleData(data: Any, resultCode: Int) {
                endLoading()

                if (resultCode == EResultCode.SUCCESS.value){
                    toast("Success Create")
                    finish()
                }else
                    toast("Create Failed")
            }

        },Remaja(name,gender,tempatLahir,tanggalLahir,noTel,alamat,kelas,sekolah,hobby,favWarna,note,
            isPadus,jenisSuara,isPelayanan,isLiturgos,isPenyambut,isPianis,isGitaris,isLcd,isPengurus,isAbsensi,isKolektor,isPadus1,
            dateFormat().format(Date()),dateFormat().format(Date()),
            mAuth.currentUser!!.uid,mAuth.currentUser!!.uid,image,remajaId,EStatusCode.ACTIVE.toString()))
    }

    private fun uploadImage(){
        if(filePath != null){
            loading()

            val ref = storage.child(ETable.REMAJA.toString())
                .child(remajaId)

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
                    if (ContextCompat.checkSelfPermission(this@AddRemajaActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@AddRemajaActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@AddRemajaActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@AddRemajaActivity,
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
                ivRemajaImage.setImageBitmap(rotateImage(bitmap))
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
                ivRemajaImage.setImageBitmap(rotateImage(bitmap))
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
    private fun generateRemajaId() : String{
        return "R${mDatabase.push().key.toString()}"
    }

    private fun loading(){
        pbRemaja.visibility = View.VISIBLE
        btnRemajaSave.isEnabled = false
    }

    private fun endLoading(){
        pbRemaja.visibility = View.GONE
        btnRemajaSave.isEnabled = true
    }

    private fun initDateListener(){
        tanggalLahirListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tvRemajaTanggalLahir.text = simpleDateFormat().format(cal.time)
            }
    }

    private fun initSpinner(){
        spGenderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,genderItems)
        spGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spRemajaJenisKelamin.adapter = spGenderAdapter
        spRemajaJenisKelamin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spRemajaJenisKelamin.gravity = Gravity.CENTER

        spJenisSuaraAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,jenisSuaraItems)
        spJenisSuaraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spRemajaJenisSuara.adapter = spJenisSuaraAdapter
        spRemajaJenisSuara.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spRemajaJenisSuara.gravity = Gravity.CENTER
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        endLoading()
    }
}
