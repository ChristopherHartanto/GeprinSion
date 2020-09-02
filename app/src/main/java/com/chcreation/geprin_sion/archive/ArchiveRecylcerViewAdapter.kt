package com.chcreation.geprin_sion.archive

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.home.Content
import com.chcreation.geprin_sion.model.EContentType
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.showError
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Exception
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class ArchiveRecylcerViewAdapter(private val context: Context,
                                 private val items: List<Content>,
                                 private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<ArchiveRecylcerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_archive_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowArchive)
        private val loading = view.findViewById<ProgressBar>(R.id.pbRowArchiveLoading)
        private val loadFailed = view.findViewById<TextView>(R.id.tvRowArchiveLoadFailed)

        fun bindItem(content: Content, listener: (position: Int) -> Unit, position: Int,context: Context) {
            Glide.with(context).load(content.IMAGE_CONTENT).listener(object :RequestListener<String,GlideDrawable>{
                override fun onException(
                    e: Exception?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadFailed.visibility = View.VISIBLE
                    loading.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: GlideDrawable?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFromMemoryCache: Boolean,
                    isFirstResource: Boolean
                ): Boolean {
                    loading.visibility = View.GONE
                   return false
                }

            }).into(image)

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())

                listener(position)
            }
        }

    }

}
