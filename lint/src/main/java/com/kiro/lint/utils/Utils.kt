package com.kiro.lint.utils

import com.android.SdkConstants
import com.android.utils.subtag
import org.w3c.dom.Element

class Utils {
    companion object {
        private const val MAIN_ACTION = "android.intent.action.MAIN"
        private const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"

        fun isLaunchable(intentFilterTag: Element?) =
            intentFilterTag
                ?.subtag(SdkConstants.TAG_ACTION)
                ?.getAttributeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME)
                ?.equals(MAIN_ACTION) == true &&
                    intentFilterTag
                        .subtag(SdkConstants.TAG_CATEGORY)
                        ?.getAttributeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME)
                        ?.equals(CATEGORY_LAUNCHER) == true
    }
}