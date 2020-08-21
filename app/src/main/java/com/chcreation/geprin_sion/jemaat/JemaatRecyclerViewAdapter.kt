package com.chcreation.geprin_sion.jemaat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.Jemaat
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource
import java.util.*

class JemaatRecyclerViewAdapter(private val context: Context, private val items: List<Jemaat>, private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<JemaatRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_jemaat_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowJemaatImage)
        private val name = view.findViewById<TextView>(R.id.tvRowJemaatName)

        fun bindItem(jemaat: Jemaat, listener: (position: Int) -> Unit, position: Int,context: Context) {
            if (jemaat.IMAGE != ""){
                image.visibility = View.VISIBLE
                Glide.with(context).load(jemaat.IMAGE).into(image)
            }
            else
                image.imageResource = R.drawable.default_image

            name.text = jemaat.NAMA
            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}