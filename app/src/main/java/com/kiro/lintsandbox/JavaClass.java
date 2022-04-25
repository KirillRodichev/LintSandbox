package com.kiro.lintsandbox;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JavaClass extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void setWebView() {
        WebView webView = (WebView) findViewById(R.id.webview);
        Intent i = new Intent();
        String str = i.getStringExtra("reg_url");
        webView.loadUrl(str);
        webView.loadUrl(i.getStringExtra("reg_url"));
    }
}
