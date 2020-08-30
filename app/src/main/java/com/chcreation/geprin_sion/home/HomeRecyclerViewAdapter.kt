package com.chcreation.geprin_sion.home

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
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.geprin_sion.util.showError
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.net.MalformedURLException
import java.net.URL
import java.util.*


class HomeRecyclerViewAdapter(private val context: Context,
                              private val fragmentActivity: FragmentActivity,
                              private val items: List<Content>,
                              private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_home_content_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],fragmentActivity,listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val userImage = view.findViewById<ImageView>(R.id.ivRowHomeUserImage)
        private val contentImage = view.findViewById<ImageView>(R.id.ivRowHomeContentImage)
        private val userName = view.findViewById<TextView>(R.id.tvRowHomeUserName)
        private val createdDate = view.findViewById<TextView>(R.id.tvRowHomeCreatedDate)
        private val like = view.findViewById<ImageView>(R.id.ivRowHomeLike)
        private val totalLike = view.findViewById<TextView>(R.id.tvRowhHomeTotalLike)
        private val caption = view.findViewById<TextView>(R.id.tvRowHomeCaption)
        private val link = view.findViewById<TextView>(R.id.tvRowHomeLink)

        fun bindItem(content: Content, fragmentActivity: FragmentActivity, listener: (position: Int) -> Unit, position: Int,context: Context) {
            if (content.USER_IMAGE != ""){
                userImage.visibility = View.VISIBLE
                Glide.with(context).load(content.USER_IMAGE).into(userImage)
            }
            else
                userImage.imageResource = R.drawable.default_image

            if (content.IMAGE_CONTENT!!.toLowerCase(Locale.getDefault()).contains("youtu")){
                contentImage.visibility = View.VISIBLE
                Glide.with(context).load(extractYoutubeId(content.IMAGE_CONTENT)).into(contentImage)
            }
            else if (content.IMAGE_CONTENT != ""){
                contentImage.visibility = View.VISIBLE
                Glide.with(context).load(content.IMAGE_CONTENT).into(contentImage)
            }
            else{
                contentImage.visibility = View.GONE
            }

            if (content.LIKE!!){
                like.imageResource = R.drawable.like
            }
            else
                like.imageResource = R.drawable.unlike

            if (content.TOTAL_LIKE!! > 0){
                totalLike.visibility = View.VISIBLE
                totalLike.text = "${content.TOTAL_LIKE} likes"
            }else{
                totalLike.visibility = View.GONE
            }

            if (content.LINK == "")
                link.visibility = View.GONE
            else{
                link.visibility = View.VISIBLE
                link.text = content.LINK
            }

            userName.text = content.USER_NAME
            createdDate.text = content.CREATED_DATE
            caption.text = content.CAPTION

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }

            like.onClick {
                like.startAnimation(normalClickAnimation())
                listener(position)
            }

            link.onClick {
                openWebsite(content.LINK.toString(),context,fragmentActivity)
            }
        }

        private fun openWebsite(url: String, context: Context,fragmentActivity: FragmentActivity){
            try {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                fragmentActivity.startActivity(i)
            }catch (e:Exception){
                e.printStackTrace()
                showError(context,e.message.toString())
            }
        }
        @Throws(MalformedURLException::class)
        fun extractYoutubeId(url: String?): String? {
            val query: String = URL(url).query
            val param = query.split("&").toTypedArray()
            var id: String? = null
            for (row in param) {
                val param1 = row.split("=").toTypedArray()
                if (param1[0] == "v") {
                    id = param1[1]
                }
            }
            return id
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
    var TYPE: String? = "",
    var LINK: String? = "",
    var KEY: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)