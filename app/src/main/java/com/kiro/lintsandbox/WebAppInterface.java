package com.kiro.lintsandbox;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
        this.mContext = c;
    }

    @JavascriptInterface
    public String getUserToken() {
        return JavaClass.getUserToken();
    }
}
