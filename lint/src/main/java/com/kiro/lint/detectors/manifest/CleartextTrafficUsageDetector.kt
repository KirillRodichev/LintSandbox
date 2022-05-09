package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class CleartextTrafficUsageDetector : Detector(), XmlScanner {

    override fun getApplicableElements() = listOf(SdkConstants.TAG_APPLICATION)

    override fun visitElement(context: XmlContext, element: Element) {
        val usesCleartextTraffic = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, ATTR_USES_CLEARTEXT_TRAFFIC)

        if (usesCleartextTraffic != null && usesCleartextTraffic.value == SdkConstants.VALUE_TRUE) {
            reportUsage(context, usesCleartextTraffic)
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attribute: Attr,
    ) {
        val quickfixData = LintFix.create()
            .name("**Disable** clear text traffic usage")
            .set()
            .android()
            .attribute(ATTR_USES_CLEARTEXT_TRAFFIC)
            .value(SdkConstants.VALUE_FALSE)
            .build()

        context.report(
            issue = ISSUE,
            location = context.getLocation(attribute),
            "**Clear text traffic** is Enabled For App `[android:usesCleartextTraffic=true]`",
            quickfixData,
        )
    }

    companion object {
        const val ATTR_USES_CLEARTEXT_TRAFFIC = "usesCleartextTraffic"

        private const val CleartextTrafficUsageId = "CleartextTrafficUsageId"
        private const val CleartextTrafficUsageBriefDescription = "The app intends to use cleartext network traffic."
        private const val CleartextTrafficUsageExplanation = """
            The app intends to use cleartext network traffic, such as cleartext HTTP, FTP stacks, DownloadManager, \
            and MediaPlayer. The default value for apps that target API level 27 or lower is "true". Apps that target \
            API level 28 or higher default to "false". The key reason for avoiding cleartext traffic is the lack of \
            confidentiality, authenticity, and protections against tampering; a network attacker can eavesdrop on \
            transmitted data and also modify it without being detected.
        """

        private val IMPLEMENTATION = Implementation(
            CleartextTrafficUsageDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = CleartextTrafficUsageId,
            briefDescription = CleartextTrafficUsageBriefDescription,
            explanation = CleartextTrafficUsageExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING, // TODO: Severity.ERROR
            implementation = IMPLEMENTATION,
        )
    }
}