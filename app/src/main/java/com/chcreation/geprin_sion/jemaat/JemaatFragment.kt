package com.chcreation.geprin_sion.jemaat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.Jemaat
import com.chcreation.geprin_sion.presenter.JemaatPresenter
import com.chcreation.geprin_sion.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_jemaat.*
import kotlinx.android.synthetic.main.fragment_jemaat.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 */
class JemaatFragment : Fragment(), MainView {

    private lateinit var adapter: JemaatRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: JemaatPresenter
    private var jemaatItems = mutableListOf<Jemaat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jemaat, container, false)
    }

    companion object{
        var active = false // to end application
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = JemaatPresenter(this,mAuth,mDatabase, ctx)

        adapter = JemaatRecyclerViewAdapter(ctx,jemaatItems){

        }

        rvJemaat.adapter = adapter
        rvJemaat.layoutManager = LinearLayoutManager(ctx)

        fabJemaat.onClick {
            val options = mutableListOf("Add New","Export XLR")
            selector("Options",options){dialogInterface, i ->
                when(i){
                    0->{
                        startActivity<NewJemaatActivity>()
                    }
                    1->{}
                }
            }
        }

        srJemaat.onRefresh {
            presenter.retrieveJemaats()
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveJemaats()
    }

    override fun onResume() {
        super.onResume()
        active = true
    }
    override fun onPause() {
        super.onPause()
        active = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_JEMAAT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    jemaatItems.clear()
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Jemaat::class.java)

                        if (item != null) {
                            jemaatItems.add(item)
                        }
                    }
                    jemaatItems.reverse()
                    srJemaat.isRefreshing = false
                    adapter.notifyDataSetChanged()
                }
            }
            srJemaat.isRefreshing = false
        }
    }

    override fun response(message: String) {
    }
}
