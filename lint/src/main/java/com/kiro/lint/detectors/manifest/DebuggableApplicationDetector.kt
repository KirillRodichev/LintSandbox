package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class DebuggableApplicationDetector : Detector(), XmlScanner {

    override fun getApplicableElements() = listOf(SdkConstants.TAG_APPLICATION)

    override fun visitElement(context: XmlContext, element: Element) {
        val debuggable = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_DEBUGGABLE)

        if (debuggable != null && debuggable.value == SdkConstants.VALUE_TRUE) {
            reportUsage(context, debuggable)
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attribute: Attr,
    ) {
        val quickfixData = LintFix.create()
            .name("**Disable** debuggable for App")
            .set()
            .android()
            .attribute(SdkConstants.ATTR_DEBUGGABLE)
            .value(SdkConstants.VALUE_FALSE)
            .build()

        context.report(
            issue = ISSUE,
            location = context.getLocation(attribute),
            "**Debug** is Enabled For App `[android:debuggable=true]`",
            quickfixData,
        )
    }

    companion object {
        private const val DebuggableApplicationId = "DebuggableApplicationId"
        private const val DebuggableApplicationBriefDescription = "The app is debuggable."
        private const val DebuggableApplicationExplanation = """
            Debugging was enabled on the app which makes it easier for reverse engineers to hook a debugger to it. \
            This allows dumping a stack trace and accessing debugging helper classes.
        """

        private val IMPLEMENTATION = Implementation(
            DebuggableApplicationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = DebuggableApplicationId,
            briefDescription = DebuggableApplicationBriefDescription,
            explanation = DebuggableApplicationExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING, // TODO: Severity.ERROR
            implementation = IMPLEMENTATION,
        )
    }
}