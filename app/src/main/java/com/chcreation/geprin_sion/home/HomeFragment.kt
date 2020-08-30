package com.chcreation.geprin_sion.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.geprin_sion.R
import com.chcreation.geprin_sion.model.EMessageResult
import com.chcreation.geprin_sion.model.Like
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
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), MainView {

    companion object{
        var active = false // to end application
        var contentItems = mutableListOf<Content>()
        var likeItems = mutableListOf<Like>()
        var likeKeyItems = mutableListOf<String>()
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: HomePresenter
    private lateinit var rvAdapter: HomeRecyclerViewAdapter

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

        rvAdapter = HomeRecyclerViewAdapter(ctx,requireActivity(),contentItems){
            contentItems[it].LIKE = !contentItems[it].LIKE!!
            rvAdapter.notifyDataSetChanged()

            val like = contentItems[it].LIKE
            if (!like!!){
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
                        contentItems[it].LIKE = !contentItems[it].LIKE!!
                        rvAdapter.notifyDataSetChanged()
                    }
                }
            }
            else if (like){
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
                        contentItems[it].LIKE = !contentItems[it].LIKE!!
                        rvAdapter.notifyDataSetChanged()
                    }
                }
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
            GlobalScope.launch {
                presenter.retrieveLikes() // dapatin like baru content
            }
        }

    }

    override fun onStart() {
        super.onStart()
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
            if (response == EMessageResult.FETCH_CONTENT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    contentItems.clear()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Content::class.java)
                        if (item != null){
                            for (likeItem in likeItems){
                                if (likeItem.CONTENT_ID == item.KEY)
                                    item.LIKE = true
                            }
                            contentItems.add(item)
                        }
                    }
                    contentItems.reverse()
                    rvAdapter.notifyDataSetChanged()
                    srHome.isRefreshing = false
                }
            }
            if (response == EMessageResult.FETCH_LIKE_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    likeItems.clear()
                    likeKeyItems.clear()
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