package com.kiro.lintsandbox;

import android.content.Context;
import android.webkit.JavascriptInterface;

import java.io.FileNotFoundException;

public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
        this.mContext = c;
        // c.openFileOutput("afsdf.sdf", Context.MODE_WORLD_WRITEABLE);
    }

    @JavascriptInterface
    public String getUserToken() {
        return JavaClass.getUserToken();
    }
}
