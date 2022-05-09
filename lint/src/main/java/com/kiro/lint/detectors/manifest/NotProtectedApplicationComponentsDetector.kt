package com.kiro.lint.detectors.manifest

import com.android.SdkConstants
import com.android.sdklib.AndroidVersion.VersionCodes.S
import com.android.tools.lint.detector.api.*
import com.android.utils.subtag
import com.kiro.lint.utils.Utils
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class NotProtectedApplicationComponentsDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = listOf(
        SdkConstants.TAG_ACTIVITY,
        SdkConstants.TAG_ACTIVITY_ALIAS,
        SdkConstants.TAG_SERVICE,
        SdkConstants.TAG_RECEIVER,
    )

    override fun visitElement(context: XmlContext, element: Element) {
        val intentFilter = element.subtag(SdkConstants.TAG_INTENT_FILTER)
        val exported = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_EXPORTED)
        val componentName = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME).value

        // TODO: consider excluding components with permissions
        if (intentFilter != null && exported == null) {
            val incident = Incident(
                ISSUE,
                element,
                context.getNameLocation(element),
                getMessageByComponentType(componentName, element.tagName),
                createSetToFalseFix("Add android:exported=\"false\"")
            )
            context.report(incident, map())
        } else if (exported.value == SdkConstants.VALUE_TRUE && !Utils.isLaunchable(intentFilter)) {
            val incident = Incident(
                ISSUE,
                exported,
                context.getLocation(exported),
                getMessageByComponentType(componentName, element.tagName),
                createSetToFalseFix("Replace android:exported=\"true\"")
            )
            context.report(incident, map())
        }
    }

    override fun filterIncident(context: Context, incident: Incident, map: LintMap): Boolean {
        /*if (context.mainProject.targetSdk >= S) {
            incident.overrideSeverity(Severity.ERROR)
        }*/
        return true
    }

    private fun createSetToFalseFix(displayName: String) = fix()
        .name(displayName)
        .set()
        .android()
        .attribute(SdkConstants.ATTR_EXPORTED)
        .value(SdkConstants.VALUE_FALSE)
        .build()

    companion object {
        private const val NotProtectedApplicationComponentsId = "NotProtectedApplicationComponentsId"

        private fun getMessageByComponentType(
            componentNameAttr: String,
            componentName: String = "component",
        ): String {
            return """
                **${componentName.replaceFirstChar { it.uppercase() }}** ($componentNameAttr) is not Protected \
                [android:exported=true]
            """.trimIndent()
        }

        private fun getDescriptionByComponentName(componentName: String = "component"): String {
            return """
                **${componentName.replaceFirstChar { it.uppercase() }}** is found to be shared with other apps on the \
                device therefore leaving it accessible to any other application on the device.
            """.trimIndent()
        }

        private val IMPLEMENTATION = Implementation(
            NotProtectedApplicationComponentsDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks/ExportedFlagDetector.kt;l=116?q=IntentFilterExportedReceiver
        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = NotProtectedApplicationComponentsId,
            briefDescription = getDescriptionByComponentName(),
            explanation = getDescriptionByComponentName(),
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}