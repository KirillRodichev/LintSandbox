package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class TestOnlyApplicationDetector : Detector(), XmlScanner {

    override fun getApplicableElements() = listOf(SdkConstants.TAG_APPLICATION)

    override fun visitElement(context: XmlContext, element: Element) {
        val testOnlyMode = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_TEST_ONLY)

        if (testOnlyMode != null && testOnlyMode.value == SdkConstants.VALUE_TRUE) {
            reportUsage(context, testOnlyMode)
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attribute: Attr,
    ) {
        val quickfixData = LintFix.create()
            .name("**Disable** test mode for App")
            .set()
            .android()
            .attribute(SdkConstants.ATTR_TEST_ONLY)
            .value(SdkConstants.VALUE_FALSE)
            .build()

        context.report(
            issue = ISSUE,
            location = context.getLocation(attribute),
            "**TestOnly mode** is Enabled For App `[android:testOnly=true]`",
            quickfixData,
        )
    }

    companion object {
        private const val TestOnlyApplicationId = "TestOnlyApplicationId"
        private const val TestOnlyApplicationBriefDescription = "Application is in Test Mode `[android:testOnly=true]`"
        private const val TestOnlyApplicationExplanation = """
            It may expose functionality or data outside of itself that would cause a security hole.
        """

        private val IMPLEMENTATION = Implementation(
            TestOnlyApplicationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = TestOnlyApplicationId,
            briefDescription = TestOnlyApplicationBriefDescription,
            explanation = TestOnlyApplicationExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING, // TODO: Severity.ERROR
            implementation = IMPLEMENTATION,
        )
    }
}