package com.kiro.lint.detectors

import com.android.SdkConstants
import com.android.sdklib.AndroidVersion
import com.android.tools.lint.detector.api.*
import com.android.utils.subtag
import com.kiro.lint.utils.Utils
import org.w3c.dom.Attr
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class InsecureApplicationComponentsDetector : Detector(), XmlScanner {

    private val permissionTags = listOf(
        SdkConstants.TAG_USES_PERMISSION,
        SdkConstants.TAG_USES_PERMISSION_SDK_23,
        SdkConstants.TAG_USES_PERMISSION_SDK_M,
    )
    private val componentTags = listOf(
        SdkConstants.TAG_ACTIVITY,
        SdkConstants.TAG_ACTIVITY_ALIAS,
        SdkConstants.TAG_SERVICE,
        SdkConstants.TAG_RECEIVER,
    )
    private val listedApplicationPermissions: HashSet<String> = HashSet()

    override fun getApplicableElements() = concatenate(componentTags)

    override fun visitElement(context: XmlContext, element: Element) {
        val elementName = element.tagName
        if (permissionTags.contains(elementName)) {
            val permissionName = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME).value
            listedApplicationPermissions.add(permissionName)
        } else {
            val intentFilter = element.subtag(SdkConstants.TAG_INTENT_FILTER)
            val exported = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_EXPORTED)
            val componentName = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.ATTR_NAME).value
            val permission = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, SdkConstants.TAG_PERMISSION)

            // TODO: these rules may only be applicable to targetSdk >= S
            val isInsecurelyExported =
                intentFilter != null && exported == null || // exported implicitly
                exported.value == SdkConstants.VALUE_TRUE && !Utils.isLaunchable(intentFilter) // exported explicitly

            if (permission != null && !listedApplicationPermissions.contains(permission.value) && isInsecurelyExported) {
                reportUsage(context, permission, getMessageByComponentType(
                    permissionName = permission.value,
                    componentName = elementName,
                    componentNameAttr = componentName,
                ))
            }
        }
    }

    private fun reportUsage(
        context: XmlContext,
        attr: Attr,
        message: String,
    ) {
        context.report(
            issue = InsecureWebViewImplementationDetector.ISSUE,
            location = context.getLocation(attr),
            message,
        )
    }

    override fun filterIncident(context: Context, incident: Incident, map: LintMap): Boolean {
        if (context.mainProject.targetSdk >= AndroidVersion.VersionCodes.S) {
            incident.overrideSeverity(Severity.ERROR)
        }
        return true
    }

    companion object {
        fun <T> concatenate(vararg lists: List<T>): List<T> {
            return listOf(*lists).flatten()
        }

        private const val InsecureApplicationComponentsId = "InsecureApplicationComponentsId"

        private fun getMessageByComponentType(
            permissionName: String,
            componentNameAttr: String,
            componentName: String = "component",
        ): String {
            return """
                **${componentName.replaceFirstChar { it.uppercase() }}** ($componentNameAttr) is Protected by a \
                permission, but the protection level of the permission should be checked. \
                **Permission:** $permissionName [android:exported=true]
            """.trimIndent()
        }

        private fun getDescriptionByComponentName(componentName: String = "component"): String {
            return """
                ${componentName.replaceFirstChar { it.uppercase() }} is found to be shared with other apps on the \
                device therefore leaving it accessible to any other application on the device. It is protected by a \
                permission which is not defined in the analysed application. As a result, the protection level of the \
                permission should be checked where it is defined. If it is set to normal or dangerous, a malicious \
                application can request and obtain the permission and interact with the component. If it is set to \
                signature, only applications signed with the same certificate can obtain the permission.
            """.trimIndent()
        }

        private val IMPLEMENTATION = Implementation(
            InsecureApplicationComponentsDetector::class.java,
            Scope.MANIFEST_SCOPE,
        )

        // TODO: Java doc
        @JvmField
        val ISSUE = Issue.create(
            id = InsecureApplicationComponentsId,
            briefDescription = getDescriptionByComponentName(),
            explanation = getDescriptionByComponentName(),
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }

}