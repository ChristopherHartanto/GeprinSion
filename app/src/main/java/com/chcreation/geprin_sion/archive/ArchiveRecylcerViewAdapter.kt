package com.chcreation.geprin_sion.archive

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EContentType
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.showError
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
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

        fun bindItem(content: Content, listener: (position: Int) -> Unit, position: Int,context: Context) {
            Glide.with(context).load(content.IMAGE_CONTENT).into(image)

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())

                listener(position)
            }
        }

    }

}

data class Content(
    var USER_IMAGE: String? = "",
    var USER_CODE: String? = "",
    var USER_NAME: String? = "",
    var LIKE: Boolean? = false,
    var TOTAL_LIKE: Int? = 0,
    var IMAGE_CONTENT: String? = "",
    var CAPTION: String? = "",
    var TYPE: String? = EContentType.Pengumuman.toString(),
    var LINK: String? = "",
    var KEY: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)