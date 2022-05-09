package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class AndroidSecretCodeUsageDetector : Detector(), XmlScanner {

    override fun getApplicableElements() = listOf(SdkConstants.TAG_DATA)

    override fun visitElement(context: XmlContext, element: Element) {
        val scheme = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_SCHEME)

        if (scheme.value == VALUE_ANDROID_SECRET_CODE) {
            reportUsage(context, scheme)
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attribute: Attr,
    ) {
        context.report(
            issue = ISSUE,
            location = context.getLocation(attribute),
            "Dailer Code: Found `[android:scheme=\"android_secret_code\"]`",
        )
    }

    companion object {
        const val VALUE_ANDROID_SECRET_CODE = "android_secret_code"

        private const val AndroidSecretCodeUsageId = "AndroidSecretCodeUsageId"
        private const val AndroidSecretCodeUsageBriefDescription = "Dailer Code: Found `[android:scheme=\"android_secret_code\"]`"
        private const val AndroidSecretCodeUsageExplanation = """
            A secret code was found in the manifest. These codes, when entered into the dialer grant access to hidden \
            content that may contain sensitive information.
        """

        private val IMPLEMENTATION = Implementation(
            AndroidSecretCodeUsageDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = AndroidSecretCodeUsageId,
            briefDescription = AndroidSecretCodeUsageBriefDescription,
            explanation = AndroidSecretCodeUsageExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}