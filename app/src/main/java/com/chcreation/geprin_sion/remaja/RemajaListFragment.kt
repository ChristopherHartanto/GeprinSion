package com.chcreation.geprin_sion.remaja

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.jemaat.JemaatRecyclerViewAdapter
import com.chcreation.geprin_sion.remaja.AbsentActivity.Companion.remajaItems
import kotlinx.android.synthetic.main.fragment_remaja_list.*
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class RemajaListFragment : Fragment() {

    private lateinit var adapter: RemajaRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_remaja_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RemajaRecyclerViewAdapter(ctx,remajaItems){

        }
        rvRemajaList.layoutManager = LinearLayoutManager(ctx)
        rvRemajaList.adapter = adapter
    }

}
