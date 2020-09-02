package com.chcreation.geprin_sion.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.*
import com.chcreation.geprin_sion.presenter.HomePresenter
import com.chcreation.geprin_sion.util.normalClickAnimation
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), MainView {

    companion object{
        var active = false // to end application
        var contentItems = mutableListOf<Content>()
        var likeItems = mutableListOf<Like>()
        var likeKeyItems = mutableListOf<String>()
        var position = 0
        var editContent = false
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: HomePresenter
    private lateinit var rvAdapter: HomeRecyclerViewAdapter
    private var onLike = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = HomePresenter(this,mAuth,mDatabase, ctx)

        srHome.setColorSchemeColors(Color.BLUE, Color.RED)

        rvAdapter = HomeRecyclerViewAdapter(ctx,
            mAuth.currentUser!!.uid,requireActivity(),contentItems){ it, link, edit->
            if (edit){
                val options = mutableListOf("Edit", "Delete")


                selector("Options",options) {
                        dialogInterface, i ->
                    when(i){
                        0 -> {
                            editContent = true
                            position = it
                            startActivity(intentFor<AddContentActivity>())
                        }
                        1 -> {
                            alert("Are You Sure Want to Delete"){
                                title = "Delete"
                                yesButton {  di->
                                    presenter.deleteContent(contentItems[it].KEY.toString()){success->
                                        if (success){
                                            toast("Delete Success")
                                            presenter.retrieveLikes()
                                        }else{
                                            toast("Failed to Delete")
                                        }
                                    }
                                }
                                noButton {  }
                            }.show()
                        }
                    }
                }
            }
            else if (link == ""){
                if (onLike){
                    onLike = false
                    contentItems[it].LIKE = !contentItems[it].LIKE!!

                    val like = contentItems[it].LIKE
                    if (!like!!){
                        contentItems[it].TOTAL_LIKE = contentItems[it].TOTAL_LIKE!! - 1
                        rvAdapter.notifyDataSetChanged()

                        val index = likeItems.indexOf(Like(contentItems[it].KEY))
                        if (index >= 0)
                            presenter.updateTotalLikes(contentItems[it].KEY.toString(),-1){totalLike ->
                                if (totalLike != -99){
                                    presenter.deleteLike(likeKeyItems[index]){success ->
                                        if (success){
                                            contentItems[it].TOTAL_LIKE = totalLike
                                            rvAdapter.notifyDataSetChanged()
                                        }else{
                                            contentItems[it].LIKE = !contentItems[it].LIKE!!
                                            rvAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }else{
                                    toast("Error Like")
                                    contentItems[it].LIKE = !contentItems[it].LIKE!!
                                    rvAdapter.notifyDataSetChanged()
                                }
                                onLike = true
                            }
                    }
                    else if (like){
                        contentItems[it].TOTAL_LIKE = contentItems[it].TOTAL_LIKE!! + 1
                        rvAdapter.notifyDataSetChanged()

                        presenter.updateTotalLikes(contentItems[it].KEY.toString(),1){totalLike ->
                            if(totalLike != -99){
                                presenter.createLike(contentItems[it].KEY.toString()){ success ->
                                    if (success){
                                        contentItems[it].TOTAL_LIKE = totalLike
                                        rvAdapter.notifyDataSetChanged()
                                    }else{
                                        contentItems[it].LIKE = !contentItems[it].LIKE!!
                                        rvAdapter.notifyDataSetChanged()
                                    }
                                }
                            }else{
                                toast("Error Like")
                                contentItems[it].LIKE = !contentItems[it].LIKE!!
                                rvAdapter.notifyDataSetChanged()
                            }
                            onLike = true
                        }
                    }
                }
            }else{
                startActivity(intentFor<ContentDetailActivity>(EContent.IMAGE_CONTENT.toString() to link))
            }
        }
        rvHome.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(ctx)
        }

        fbHome.onClick {
            fbHome.startAnimation(normalClickAnimation())
            startActivity<AddContentActivity>()
        }

        srHome.onRefresh {
            if (onLike){
                presenter.retrieveLikes() // dapatin like baru content
            }else{
                srHome.isRefreshing = false
            }
        }

    }

    override fun onStart() {
        super.onStart()
        presenter.retrieveLikes()
    }

    override fun onResume() {
        super.onResume()

        editContent = false
        position = 0
        active = true
    }
    override fun onPause() {
        super.onPause()
        active = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_CONTENT_SUCCESS.toString()){
                contentItems.clear()
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Content::class.java)
                        if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()
                            && (item.CHANNEL == EChannel.All.toString() || item.CHANNEL == EChannel.Umum.toString())){
                            for (likeItem in likeItems){
                                if (likeItem.CONTENT_ID == item.KEY)
                                    item.LIKE = true
                            }
                            contentItems.add(item)
                        }
                    }
                }
                contentItems.reverse()
                rvAdapter.notifyDataSetChanged()
                srHome.isRefreshing = false
            }
            if (response == EMessageResult.FETCH_LIKE_SUCCESS.toString()){
                likeItems.clear()
                likeKeyItems.clear()

                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Like::class.java)
                        if (item != null){
                            likeItems.add(item)
                            likeKeyItems.add(data.key.toString())
                        }
                    }
                }
                presenter.retrieveContent()
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
