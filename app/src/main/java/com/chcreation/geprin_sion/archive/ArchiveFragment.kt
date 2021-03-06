package com.chcreation.geprin_sion.archive

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.home.AddContentActivity
import com.chcreation.geprin_sion.home.Content
import com.chcreation.geprin_sion.home.ContentDetailActivity
import com.chcreation.geprin_sion.home.HomeFragment
import com.chcreation.geprin_sion.model.EContent
import com.chcreation.geprin_sion.model.EContentType
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.EStatusCode
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.geprin_sion.remaja.RemajaFragment
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_archive.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton

/**
 * A simple [Fragment] subclass.
 */
class ArchiveFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: HomePresenter
    private lateinit var rvAdapter: ArchiveRecylcerViewAdapter
    private var contentItems = mutableListOf<Content>()
    private var filteredContentItems = mutableListOf<Content>()
    private var selectedType = EContentType.All.toString()

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
        rvAdapter = ArchiveRecylcerViewAdapter(ctx,filteredContentItems){
            startActivity(intentFor<ArchiveViewImageActivity>(EContent.IMAGE_CONTENT.toString() to filteredContentItems[it].IMAGE_CONTENT))
        }

        llArchiveFilter.onClick {
            llArchiveFilter.startAnimation(normalClickAnimation())

            val options = mutableListOf(EContentType.All.toString(), EContentType.Pengumuman.toString(),
                EContentType.Warta.toString(),EContentType.Streaming.toString(),EContentType.File.toString())
            selector("Filter By",options) {
                    dialogInterface, i ->
                selectedType = options[i]
                tvArchiveFilterTitle.text = options[i]
                fetchData()
            }
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

    private fun fetchData(){
        filteredContentItems.clear()

        for (data in contentItems){
            if (selectedType == EContentType.All.toString() || selectedType == data.TYPE){
                filteredContentItems.add(data)
            }
        }

        rvAdapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_CONTENT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    contentItems.clear()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Content::class.java)
                        if (item != null && item.IMAGE_CONTENT != "" && item.STATUS == EStatusCode.ACTIVE.toString()){
                            contentItems.add(item)
                        }
                    }
                }
                contentItems.reverse()
                fetchData()
            }
            srArchive.isRefreshing = false
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
