package com.chcreation.geprin_sion.remaja

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import kotlinx.android.synthetic.main.fragment_padus_list.*
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class PadusListFragment : Fragment() {

    private lateinit var rvAdapter: RemajaRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_padus_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapter = RemajaRecyclerViewAdapter(ctx, AbsentActivity.remajaItems){

        }
        rvPadusList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = rvAdapter
        }
    }

}
