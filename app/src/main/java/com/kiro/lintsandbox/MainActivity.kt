package com.kiro.lintsandbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val myWebView: WebView = findViewById(R.id.webview)
        val intent = Intent(this, MainActivity::class.java)
        val str = intent.getStringExtra("reg_url") ?: ""
        myWebView.loadUrl(str)*/
    }
}