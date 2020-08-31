package com.chcreation.geprin_sion.kidung

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

import com.chcreation.geprin_sion.R
import kotlinx.android.synthetic.main.fragment_kidung.*

/**
 * A simple [Fragment] subclass.
 */
class KidungFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kidung, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wvKidung.loadUrl("https://alkitab.app/KPPK")
        wvKidung.settings.javaScriptEnabled = true
        wvKidung.webViewClient = WebViewClient()
        wvKidung.webChromeClient = object : WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loading.visibility = View.VISIBLE
                loading.progress = newProgress
                if(newProgress == 100){
                    loading.visibility = View.GONE
                }
                super.onProgressChanged(view, newProgress)
            }
        }
    }

}
