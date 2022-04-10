package com.kiro.lint.detectors

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
class IncorrectDefaultPermissionsDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String>? =
        listOf("getExternalStorageDirectory")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (context.evaluator.isMemberInClass(method, "android.os.Environment")) {
            reportUsage(context, node, method)
        }
    }

    private fun reportUsage(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = false,
                includeArguments = false
            ),
            message = ISSUE.getBriefDescription(TextFormat.TEXT),
        )
    }

    companion object {
        private const val IncorrectDefaultPermissionsIssueId = "IncorrectDefaultPermissionsIssueId"
        private const val IncorrectDefaultPermissionsIssueDescription =
            "App can read/write to External Storage. Any App can read data written to External Storage."

        private val IMPLEMENTATION = Implementation(
            IncorrectDefaultPermissionsDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-276: Incorrect Default Permissions
         * OWASP Top 10: M2: Insecure Data Storage
         * OWASP MASVS: MSTG-STORAGE-2
         * url: https://cwe.mitre.org/data/definitions/276.html
         */
        val ISSUE = Issue.create(
            id = IncorrectDefaultPermissionsIssueId,
            briefDescription = IncorrectDefaultPermissionsIssueDescription,
            explanation = IncorrectDefaultPermissionsIssueDescription,
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}