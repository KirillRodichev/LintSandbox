package com.kiro.lint.detectors

import com.android.SdkConstants
import com.android.sdklib.AndroidVersion.VersionCodes.S
import com.android.tools.lint.detector.api.*
import com.android.utils.subtag
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class NotProtectedApplicationConfigurationDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = listOf(
        SdkConstants.TAG_ACTIVITY,
        SdkConstants.TAG_ACTIVITY_ALIAS,
        SdkConstants.TAG_SERVICE,
        SdkConstants.TAG_RECEIVER,
    )

    override fun visitElement(context: XmlContext, element: Element) {
        val intentFilter = element.subtag(SdkConstants.TAG_INTENT_FILTER)
        val exported = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_EXPORTED)

        if (intentFilter != null && exported == null) {
            val incident = Incident(
                ISSUE,
                element,
                context.getNameLocation(element),
                ISSUE.getBriefDescription(TextFormat.TEXT),
                createSetToFalseFix("Add android:exported=\"false\"")
            )
            context.report(incident, map())
        } else if (exported.value == SdkConstants.VALUE_TRUE && !isLaunchable(intentFilter)) {
            val incident = Incident(
                ISSUE,
                exported,
                context.getLocation(exported),
                ISSUE.getBriefDescription(TextFormat.TEXT),
                createSetToFalseFix("Replace android:exported=\"true\"")
            )
            context.report(incident, map())
        }
    }

    override fun filterIncident(context: Context, incident: Incident, map: LintMap): Boolean {
        if (context.mainProject.targetSdk >= S) {
            incident.overrideSeverity(Severity.ERROR)
        }
        return true
    }

    private fun isLaunchable(intentFilterTag: Element?) =
        intentFilterTag
            ?.subtag(SdkConstants.TAG_ACTION)
            ?.getAttributeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME)
            ?.equals(MAIN_ACTION) == true &&
        intentFilterTag
            .subtag(SdkConstants.TAG_CATEGORY)
            ?.getAttributeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME)
            ?.equals(CATEGORY_LAUNCHER) == true

    private fun createSetToFalseFix(displayName: String) = fix()
        .name(displayName)
        .set()
        .android()
        .attribute(SdkConstants.ATTR_EXPORTED)
        .value(SdkConstants.VALUE_FALSE)
        .build()

    companion object {
        private const val MAIN_ACTION = "android.intent.action.MAIN"
        private const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"

        private const val NotProtectedApplicationConfigurationId = "NotProtectedApplicationConfigurationId"
        private const val NotProtectedApplicationConfigurationDescription = """
            A Broadcast Receiver is found to be shared with other apps on the device therefore \
            leaving it accessible to any other application on the device.
        """
        private val IMPLEMENTATION = Implementation(
            NotProtectedApplicationConfigurationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = NotProtectedApplicationConfigurationId,
            briefDescription = NotProtectedApplicationConfigurationDescription,
            explanation = NotProtectedApplicationConfigurationDescription,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}