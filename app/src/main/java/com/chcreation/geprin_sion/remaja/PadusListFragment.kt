package com.chcreation.geprin_sion.remaja

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.ERemaja
import com.chcreation.geprin_sion.model.EStatusCode
import com.chcreation.geprin_sion.model.Remaja
import com.chcreation.geprin_sion.remaja.AbsentActivity.Companion.remajaItems
import kotlinx.android.synthetic.main.fragment_padus_list.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor

/**
 * A simple [Fragment] subclass.
 */
class PadusListFragment : Fragment() {

    private lateinit var rvAdapter: RemajaRecyclerViewAdapter
    private var filteredRemajaItems = mutableListOf<Remaja>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_padus_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapter = RemajaRecyclerViewAdapter(ctx, filteredRemajaItems){
            startActivity(intentFor<RemajaDetailActivity>(ERemaja.ID.toString() to filteredRemajaItems[it].ID))
        }
        rvPadusList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = rvAdapter
        }

        fetchData()
    }

    private fun fetchData(){
        filteredRemajaItems.clear()
        for (data in remajaItems){
            if (data.STATUS == EStatusCode.ACTIVE.toString() && data.IS_PADUS!!){
                filteredRemajaItems.add(data)
            }
        }
        rvAdapter.notifyDataSetChanged()
    }

}
