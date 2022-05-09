package com.kiro.lintsandbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

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

        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setVisibility(View.INVISIBLE);
    }

    public static String getUserToken() {
        return UUID.randomUUID().toString();
    }
}
