package com.kiro.lint.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.google.common.net.InetAddresses
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression

@Suppress("UnstableApiUsage")
class IPAddressDisclosureDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(ULiteralExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler =
        object : UElementHandler() {
            override fun visitLiteralExpression(node: ULiteralExpression) {
                if (node.isString && InetAddresses.isInetAddress(node.evaluate().toString())) {
                    reportUsage(context, node)
                }
            }
        }

    private fun reportUsage(
        context: JavaContext,
        node: ULiteralExpression,
    ) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = ISSUE.getBriefDescription(TextFormat.TEXT),
        )
    }

    companion object {
        private const val IPAddressDisclosureIssueIssueId = "IPAddressDisclosureIssueId"
        private const val IPAddressDisclosureIssueIssueDescription = """
            The app exposes ip address
        """
        private val IMPLEMENTATION = Implementation(
            IPAddressDisclosureDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-200: Information Exposure
         * OWASP MASVS: MSTG-CODE-2
         * url: https://cwe.mitre.org/data/definitions/200.html
         */
        val ISSUE = Issue.create(
            id = IPAddressDisclosureIssueIssueId,
            briefDescription = IPAddressDisclosureIssueIssueDescription,
            explanation = IPAddressDisclosureIssueIssueDescription,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}