package com.chcreation.geprin_sion.archive

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.home.ContentDetailActivity
import com.chcreation.geprin_sion.model.EContent
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_archive.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.onRefresh

/**
 * A simple [Fragment] subclass.
 */
class ArchiveFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: HomePresenter
    private lateinit var rvAdapter: ArchiveRecylcerViewAdapter
    private var contentItems = mutableListOf<Content>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = HomePresenter(this,mAuth,mDatabase, ctx)

        srArchive.setColorSchemeColors(Color.BLUE, Color.RED)
        rvAdapter = ArchiveRecylcerViewAdapter(ctx,contentItems){
            startActivity(intentFor<ArchiveViewImageActivity>(EContent.IMAGE_CONTENT.toString() to contentItems[it].IMAGE_CONTENT))
        }

        rvArchive.apply {
            adapter = rvAdapter
            layoutManager = GridLayoutManager(activity,2)
        }

        srArchive.onRefresh {
            presenter.retrieveContent()
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveContent()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_CONTENT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    contentItems.clear()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Content::class.java)
                        if (item != null && item.IMAGE_CONTENT != ""){
                            contentItems.add(item)
                        }
                    }
                }
                contentItems.reverse()
                rvAdapter.notifyDataSetChanged()
            }
            srArchive.isRefreshing = false
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
