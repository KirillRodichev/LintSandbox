package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element
import org.w3c.dom.Node

@Suppress("UnstableApiUsage")
class AllowBackupApplicationDetector : Detector(), XmlScanner {

    override fun getApplicableElements() = listOf(SdkConstants.TAG_APPLICATION)

    override fun visitElement(context: XmlContext, element: Element) {
        val allowBackup = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_ALLOW_BACKUP)

        if (allowBackup == null) {
            reportUsage(context, element, false)
        } else if (allowBackup.value == SdkConstants.VALUE_TRUE) {
            reportUsage(context, allowBackup, true)
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attribute: Node,
        isEnabled: Boolean? = false,
    ) {
        val fixName = if (isEnabled == true) AllowBackupEnabledFixName else AllowBackupNotProvidedFixName
        val reportMessage = if (isEnabled == true) AllowBackupEnabledMessage else AllowBackupNotProvidedMessage

        val quickfixData = LintFix.create()
            .name(fixName)
            .set()
            .android()
            .attribute(SdkConstants.ATTR_ALLOW_BACKUP)
            .value(SdkConstants.VALUE_FALSE)
            .build()

        context.report(
            issue = ISSUE,
            location = context.getLocation(attribute),
            reportMessage,
            quickfixData,
        )
    }

    companion object {
        const val AllowBackupEnabledFixName = "**Disable** back up for Application"
        const val AllowBackupEnabledMessage = "**AllowBackup** is Enabled For App `[android:testOnly=true]`"

        const val AllowBackupNotProvidedFixName = "**Add** `[android:allowBackup=false]` to Application configuration"
        const val AllowBackupNotProvidedMessage =
            "Application Data can be Backed up by default. `[android:allowBackup]` flag is missing."

        private const val AllowBackupApplicationId = "AllowBackupApplicationId"
        private const val AllowBackupApplicationBriefDescription =
            "Application Data can be Backed up `[android:allowBackup=true]`"
        private const val AllowBackupApplicationExplanation = """
            This flag allows anyone to backup your application data via adb. It allows users who have enabled USB \
            debugging to copy application data off of the device.
        """

        private val IMPLEMENTATION = Implementation(
            AllowBackupApplicationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = AllowBackupApplicationId,
            briefDescription = AllowBackupApplicationBriefDescription,
            explanation = AllowBackupApplicationExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}