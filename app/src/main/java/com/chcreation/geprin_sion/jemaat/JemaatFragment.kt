package com.chcreation.geprin_sion.jemaat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.chcreation.geprin_sion.R
import kotlinx.android.synthetic.main.fragment_jemaat.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 */
class JemaatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jemaat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabJemaat.onClick {
            val options = mutableListOf("Add New","Export XLR")
            selector("Option",options){dialogInterface, i ->
                when(i){
                    0->{
                        startActivity<NewJemaatActivity>()
                    }
                    1->{}
                }
            }
        }
    }

}
