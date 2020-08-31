package com.chcreation.geprin_sion.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EContent
import kotlinx.android.synthetic.main.activity_content_detail.*

class ContentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_detail)

        supportActionBar?.title = "Image"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val link = intent.extras!!.getString(EContent.IMAGE_CONTENT.toString(),"")
        if (link != "")
            Glide.with(this).load(link).into(zvContent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
