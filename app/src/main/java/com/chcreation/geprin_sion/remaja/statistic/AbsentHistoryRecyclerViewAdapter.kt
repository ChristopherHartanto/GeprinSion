package com.chcreation.geprin_sion.remaja.statistic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.Absent
import com.chcreation.geprin_sion.model.AbsentDetail
import com.chcreation.geprin_sion.model.Jemaat
import com.chcreation.geprin_sion.model.Remaja
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource
import java.util.*

class AbsentHistoryRecyclerViewAdapter(private val context: Context, private val items: List<Absent>,
                                      private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<AbsentHistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_absent_history_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val dateAbsent = view.findViewById<TextView>(R.id.tvRowAbsentHistoryDate)

        fun bindItem(absent: Absent, listener: (position: Int) -> Unit, position: Int, context: Context) {

            dateAbsent.text = absent.ABSENT_DATE

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}