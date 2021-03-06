package com.chcreation.geprin_sion.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.geprin_sion.presenter.ProfilePresenter
import com.chcreation.geprin_sion.util.*
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
import kotlinx.android.synthetic.main.activity_add_content.*
import kotlinx.android.synthetic.main.activity_manage_jemaat.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.util.*

class ProfileFragment : Fragment(), MainView, DaerahIndonesiaView {

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
    private var loadProvinsi = false
    private var loadKota = false
    private var loadKecamatan = false
    private var loadKelurahan = false
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: ProfilePresenter
    private lateinit var storage: StorageReference
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var currentUser = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProfilePresenter(this,this,mAuth,mDatabase, ctx)
        storage = FirebaseStorage.getInstance().reference

        ivProfileImage.onClick {
            selectImage()
        }

        btnProfileSave.onClick {
            btnProfileSave.startAnimation(normalClickAnimation())
            if (filePath != null)
                uploadImage()
            else
                updateProfile("")
        }

        tvProfileTanggalLahir.onClick {
            DatePickerDialog(ctx,
                tanggalLahirListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        initSpinner()
        initDateListener()
    }

    override fun onStart() {
        super.onStart()
        if (filePath == null){
            btnProfileSave.isEnabled = false
            presenter.retrieveUser(mAuth.currentUser!!.uid)
        }
    }
    private fun uploadImage(){
        if(filePath != null){
            loading()

            val ref = storage.child(ETable.USER.toString())
                .child(mAuth.currentUser!!.uid)

            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        unLoading()
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateProfile(downloadUri.toString())
                } else {
                    unLoading()
                    toast("Failed to Save Image")
                }
            }.addOnFailureListener{
                unLoading()
                toast("Error : ${it.message}")
            }
        }else{
            toast("Please Upload an Image")
        }
    }

    private fun updateProfile(uploadImage: String){
        loading()

        val name = etProfileName.text.toString()
        val alamat = etProfileAlamat.text.toString()
        val noTel = etProfileNoHp.text.toString()
        val tempatLahir = etProfileTempatLahir.text.toString()
        val tanggaLahir = tvProfileTanggalLahir.text.toString()
        val rt = etProfileRT.text.toString()
        val rw = etProfileRW.text.toString()
        val image = if (uploadImage == "") currentUser.IMAGE.toString() else uploadImage

        GlobalScope.launch {
            if (uploadImage != "" || name != currentUser.NAME)
                presenter.updateContentUserData(mAuth.currentUser!!.uid,name,uploadImage)

            presenter.updateUser(User(name,image,currentUser.EMAIL,selectedProvinsi,selectedKota,
                selectedKecamatan,selectedKelurahan,rt,rw,alamat,selectedGender,selectedGolDarah,
                tempatLahir,tanggaLahir,noTel,currentUser.ACTIVE,currentUser.CREATED_DATE,currentUser.UPDATED_DATE,
                currentUser.STATUS)){
                if (isVisible && isResumed){
                    if (it){
                        setDataPreference(
                            ctx,
                            ESharedPreference.NAME.toString(),
                            name,
                            EDataType.STRING
                        )
                        setDataPreference(
                            ctx,
                            ESharedPreference.IMAGE.toString(),
                            image,
                            EDataType.STRING
                        )
                        toast("Update Success")
                    }
                    else
                        toast("Update Failed")

                    unLoading()
                }
            }
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = dateFormat().format(Date(1000))
        val storageDir: File? = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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

        val intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {
                    if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(requireActivity(),
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
            takePictureIntent.resolveActivity(ctx.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        ctx,
                        "com.example.android.geprin_sion.fileprovider",
                        it
                    )
                    filePath = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                }
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                        ctx.contentResolver,
                        filePath
                    )
                ivProfileImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: Exception) {
                filePath = null
                showError(ctx,e.message.toString())
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
                        ctx.contentResolver,
                        filePath
                    )
                ivProfileImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: IOException) { // Log the exception
                showError(ctx,e.message.toString())
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

    private fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = requireActivity().managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private fun initSpinner(){
        spGenderAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,genderItems)
        spGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileJenisKelamin.adapter = spGenderAdapter
        spProfileJenisKelamin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileJenisKelamin.gravity = Gravity.CENTER

        spGolDarahAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,golDarahItems)
        spGolDarahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileGolDarah.adapter = spGolDarahAdapter
        spProfileGolDarah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileGolDarah.gravity = Gravity.CENTER

        spProvinsiAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,provinsiSpinnerItems)
        spProvinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileProvinsi.adapter = spProvinsiAdapter
        spProfileProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileProvinsi.gravity = Gravity.CENTER

        // kota spinner
        spKotaAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,kotaSpinnerItems)
        spKotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileKotaKabupaten.adapter = spKotaAdapter
        spProfileKotaKabupaten.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileKotaKabupaten.gravity = Gravity.CENTER

        // kecamatan spinner
        spKecamatanAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,kecamatanSpinnerItems)
        spKecamatanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileKecamatan.adapter = spKecamatanAdapter
        spProfileKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileKecamatan.gravity = Gravity.CENTER

        // kelurahan
        spKelurahanAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,kelurahanSpinnerItems)
        spKelurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProfileKelurahan.adapter = spKelurahanAdapter
        spProfileKelurahan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        spProfileKelurahan.gravity = Gravity.CENTER

    }

    private fun initDateListener(){
        tanggalLahirListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                tvProfileTanggalLahir.text = simpleDateFormat().format(cal.time)
            }
    }

    private fun loading(){
        btnProfileSave.isEnabled = false
        pbProfile.visibility = View.VISIBLE
    }

    private fun unLoading(){
        btnProfileSave.isEnabled = true
        pbProfile.visibility = View.GONE
    }

    private fun fetchData(){
        etProfileName.setText(currentUser.NAME)
        etProfileAlamat.setText(currentUser.ALAMAT)
        etProfileNoHp.setText(currentUser.NO_TEL)
        etProfileRT.setText(currentUser.RT)
        etProfileRW.setText(currentUser.RW)
        etProfileTempatLahir.setText(currentUser.TEMPAT_LAHIR)
        tvProfileTanggalLahir.text = currentUser.TANGGAL_LAHIR
        if (currentUser.IMAGE != "")
            Glide.with(ctx).load(currentUser.IMAGE).into(ivProfileImage)
        spProfileGolDarah.setSelection(golDarahItems.indexOf(currentUser.GOL_DARAH))
        spProfileJenisKelamin.setSelection(genderItems.indexOf(currentUser.GENDER))
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    val item = dataSnapshot.getValue(User::class.java)
                    if (item != null) {
                        currentUser = item
                        fetchData()
                        presenter.retrieveProvinsi()
                    }
                }
                pbProfile.visibility = View.GONE
                layoutProfile.visibility = View.VISIBLE
                btnProfileSave.isEnabled = true
            }
        }
    }

    override fun response(message: String) {
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
            spProfileProvinsi.setSelection(provinsiSpinnerItems.indexOf(currentUser.PROVINSI))
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
            spProfileKotaKabupaten.setSelection(kotaSpinnerItems.indexOf(currentUser.KOTA))
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
            spProfileKecamatan.setSelection(kecamatanSpinnerItems.indexOf(currentUser.KECAMATAN))
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
            spProfileKelurahan.setSelection(kelurahanSpinnerItems.indexOf(currentUser.KELURAHAN))
            loadKelurahan = true
        }
        spKelurahanAdapter.notifyDataSetChanged()
    }

}
