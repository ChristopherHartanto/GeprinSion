package com.chcreation.geprin_sion.remaja.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.Absent
import com.chcreation.geprin_sion.model.EAbsentType
import com.chcreation.geprin_sion.presenter.AbsentPresenter
import com.chcreation.geprin_sion.remaja.statistic.StatiscticRemajaActivity.Companion.absentItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_statistic_absent_remaja.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor

/**
 * A simple [Fragment] subclass.
 */
class StatisticAbsentRemajaFragment : Fragment() {

    private lateinit var rvAdapter: AbsentHistoryRecyclerViewAdapter
    private var filteredAbsentItems = mutableListOf<Absent>()

    companion object{
        var currentAbsent = Absent()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistic_absent_remaja, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapter = AbsentHistoryRecyclerViewAdapter(ctx,filteredAbsentItems){
            currentAbsent = filteredAbsentItems[it]
            startActivity(intentFor<ManageAbsentRemajaActivity>())
        }
        rvAbsentRemajaHistory.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(ctx)
        }
        fetchData()
    }

    private fun fetchData(){
        filteredAbsentItems.clear()
        for (data in absentItems){
            if (data.TYPE == EAbsentType.All.toString())
                filteredAbsentItems.add(data)
        }
        rvAdapter.notifyDataSetChanged()
    }
}