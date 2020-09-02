package com.chcreation.geprin_sion.home

import android.app.Activity
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
import android.os.PersistableBundle
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
import com.chcreation.geprin_sion.home.HomeFragment.Companion.contentItems
import com.chcreation.geprin_sion.home.HomeFragment.Companion.editContent
import com.chcreation.geprin_sion.home.HomeFragment.Companion.position
import com.chcreation.geprin_sion.jemaat.ManageJemaatActivity
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.remaja.RemajaFragment
import com.chcreation.geprin_sion.util.*
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
import kotlinx.android.synthetic.main.activity_jemaat_detail.*
import kotlinx.android.synthetic.main.activity_new_jemaat.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.io.File
import java.io.IOException
import java.util.*

class AddContentActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: HomePresenter
    private lateinit var storage: StorageReference
    private lateinit var spTypeAdapter: ArrayAdapter<String>
    private lateinit var spChannelAdapter: ArrayAdapter<String>
    private var channelItems = arrayListOf<String>(EChannel.All.toString(),EChannel.Umum.toString(),EChannel.Remaja.toString())
    private var typeItems = arrayListOf<String>(EContentType.File.toString(),EContentType.Pengumuman.toString(),
        EContentType.Streaming.toString(),EContentType.Warta.toString())
    private var selectedType = ""
    private var selectedChannel = ""
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var contentId = ""
    private var isPosting = false
    private var message = "Create"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_content)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = HomePresenter(this,mAuth,mDatabase, this)
        storage = FirebaseStorage.getInstance().reference

        spTypeAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,typeItems)
        spTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spContentType.adapter = spTypeAdapter
        spContentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedType = typeItems[position]
            }

        }
        spContentType.gravity = Gravity.CENTER

        spChannelAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,channelItems)
        spChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spContentChannel.adapter = spChannelAdapter
        spContentChannel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedChannel = channelItems[position]
            }

        }
        spContentChannel.gravity = Gravity.CENTER

        contentId = generateContentId()

        if (editContent){
            message = "Update"
            fetchData()
        }

        tvAddContentUserName.text = getName(this)
        if (getImage(this) != "")
            Glide.with(this).load(getImage(this)).into(ivAddContentUserImage)

        supportActionBar?.title = "$message Post"
    }

    override fun onStart() {
        super.onStart()

        tvAddContentAddImage.onClick {
            tvAddContentAddImage.startAnimation(normalClickAnimation())

            selectImage()
        }
    }

    override fun onBackPressed() {
        if (filePath != null || etContent.text.toString() != "" && !isPosting){
            alert ("Do You Want to Discard?"){
                title = "${this@AddContentActivity.message} Post"
                yesButton {
                    super.onBackPressed()
                }
                noButton {

                }
            }.show()
        }else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.content, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_post -> {
                if (isPosting)
                    toast("Posting on Progress")
                else{
                    val caption = etContent.text.toString().trim()
                    if (caption == "")
                        etContent.error = "Please Fill the Field"
                    else{

                        alert ("Are You Sure Want to $message?"){
                            title = this@AddContentActivity.message
                            yesButton {
                                toast("Your Content Will be ${this@AddContentActivity.message} Shortly . . .")
                                finish()
                                isPosting = true

                                when {
                                    filePath != null -> uploadImage(editContent)
                                    editContent -> updateContent("")
                                    else -> createContent("")
                                }
                            }
                            noButton {

                            }
                        }.show()
                    }
                }
                true
            }
            android.R.id.home ->{
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    var currentContent = Content()
    private fun fetchData(){
        if (RemajaFragment.editRemaja)
            currentContent = RemajaFragment.contentItems[position]
        else
            currentContent = contentItems[position]

        etContent.setText(currentContent.CAPTION)
        etAddContentLink.setText(currentContent.LINK)
        spContentType.setSelection(typeItems.indexOf(currentContent.TYPE))
        spContentChannel.setSelection(channelItems.indexOf(currentContent.CHANNEL))
        contentId = currentContent.KEY.toString()

        if (currentContent.IMAGE_CONTENT != ""){
            ivAddContentImage.visibility = View.VISIBLE
            Glide.with(this).load(currentContent.IMAGE_CONTENT).into(ivAddContentImage)
        }
    }

    private fun createContent(image: String){

        val caption = etContent.text.toString().trim()
        val link = etAddContentLink.text.toString().trim()

        presenter.createContent(Content(
            getImage(this), mAuth.currentUser!!.uid, getName(this@AddContentActivity),
            false,0,image,caption,selectedType,selectedChannel,link,contentId,EStatusCode.ACTIVE.toString(), dateFormat().format(Date()),
            dateFormat().format(Date()), mAuth.currentUser!!.uid, mAuth.currentUser!!.uid))
    }

    private fun updateContent(image: String){

        val caption = etContent.text.toString().trim()
        val link = etAddContentLink.text.toString().trim()

        val imageContent =
        if (image == "") currentContent.IMAGE_CONTENT
        else image

        presenter.updateContent(
            currentContent.KEY.toString(),Content(
            getImage(this), mAuth.currentUser!!.uid, getName(this@AddContentActivity),
            false,0,imageContent,caption,selectedType,selectedChannel,link,currentContent.KEY,
                currentContent.STATUS,currentContent.CREATED_DATE,
            dateFormat().format(Date()), currentContent.CREATED_BY, mAuth.currentUser!!.uid)){

        }
    }

    private fun uploadImage(isEdit: Boolean){
        if(filePath != null){
            val ref = storage.child(ETable.CONTENT.toString())
                .child(contentId)

            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        isPosting = false
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    if (isEdit)
                        updateContent(downloadUri.toString())
                    else
                        createContent(downloadUri.toString())
                } else {
                    isPosting = false
                    toast("Failed to Save Image")
                }
            }.addOnFailureListener{
                isPosting = false
                toast("Error : ${it.message}")
            }
        }else{
            isPosting = false
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
                    if (ContextCompat.checkSelfPermission(this@AddContentActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@AddContentActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@AddContentActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@AddContentActivity,
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
                    filePath = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                }
            }
        }
    }

    private fun openGallery(){
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra("test",true)
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
                ivAddContentImage.setImageBitmap(rotateImage(bitmap))
                ivAddContentImage.visibility = View.VISIBLE
            } catch (e: Exception) {
                filePath = null
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            val a = data.getBooleanExtra("test",false)
            filePath = data.data
            currentPhotoPath = getRealPathFromURI(filePath!!)
            try { // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                ivAddContentImage.setImageBitmap(rotateImage(bitmap))
                ivAddContentImage.visibility = View.VISIBLE
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

    private fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private fun generateContentId() : String{
        return "C${mDatabase.push().key.toString()}"
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            toast("Success Post")
            finish()
        }else
            toast(message)
        isPosting = false
    }
}
