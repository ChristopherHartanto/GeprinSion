package com.chcreation.geprin_sion.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EContent
import kotlinx.android.synthetic.main.activity_content_detail.*
import java.lang.Exception

class ContentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_detail)

        supportActionBar?.title = "Image"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val link = intent.extras!!.getString(EContent.IMAGE_CONTENT.toString(),"")
        if (link != "")
            Glide.with(this).load(link).listener(object : RequestListener<String, GlideDrawable>{
                override fun onException(
                    e: Exception?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    tvContentLoadFailed.visibility = View.VISIBLE
                    pbContent.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: GlideDrawable?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFromMemoryCache: Boolean,
                    isFirstResource: Boolean
                ): Boolean {
                    pbContent.visibility = View.GONE
                    return false
                }

            }).into(zvContent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
