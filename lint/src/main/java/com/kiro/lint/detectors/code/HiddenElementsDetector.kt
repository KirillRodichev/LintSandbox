package com.kiro.lint.detectors.code

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression

@Suppress("UnstableApiUsage")
class HiddenElementsDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("setVisibility")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (context.evaluator.isMemberInClass(method, "android.view.View")) {
            val argument = node.valueArguments[0]
            val argumentValue = argument.evaluate()

            if (argumentValue == VIEW_GONE || argumentValue == VIEW_INVISIBLE) {
                reportUsage(context, argument)
            }
        }
    }

    private fun reportUsage(
        context: JavaContext,
        expression: UExpression,
    ) {
        context.report(
            issue = ISSUE,
            location = context.getLocation(expression),
            message = ISSUE.getBriefDescription(TextFormat.RAW)
        )
    }

    companion object {
        const val VIEW_GONE = 8
        const val VIEW_INVISIBLE = 4

        private const val HiddenElementsIssueId = "HiddenElementsIssueId"
        private val HiddenElementsIssueDescription = """
            Hidden elements in view can be used to hide data from user. But this data can be leaked. \
            **CWE-919:  Weaknesses in Mobile Applications** https://cwe.mitre.org/data/definitions/919.html
        """.trimIndent()

        private val IMPLEMENTATION = Implementation(
            HiddenElementsDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-919: Weaknesses in Mobile Applications
         * OWASP Top 10: M1: Improper Platform Usage
         * OWASP MASVS: MSTG-STORAGE-7
         * url: https://cwe.mitre.org/data/definitions/919.html
         */
        val ISSUE = Issue.create(
            id = HiddenElementsIssueId,
            briefDescription = HiddenElementsIssueDescription,
            explanation = HiddenElementsIssueDescription,
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}