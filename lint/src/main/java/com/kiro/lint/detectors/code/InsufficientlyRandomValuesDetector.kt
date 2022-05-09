package com.kiro.lint.detectors.code

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
class InsufficientlyRandomValuesDetector : Detector(), SourceCodeScanner {
    override fun getApplicableConstructorTypes(): List<String>?
        = listOf("java.util.Random")

    override fun visitConstructor(
        context: JavaContext,
        node: UCallExpression,
        constructor: PsiMethod
    ) {
        reportUsage(context, node, constructor)
    }

    private fun reportUsage(
        context: JavaContext,
        node: UCallExpression,
        constructor: PsiMethod
    ) {
        val quickfixData = LintFix.create()
            .name("Use java.security.SecureRandom")
            .replace()
            .text(constructor.name)
            .with("SecureRandom")
            .robot(true) // Can be applied automatically.
            .independent(true) // Does not conflict with other auto-fixes.
            .build()

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = false,
                includeArguments = false
            ),
            message = ISSUE.getBriefDescription(TextFormat.TEXT),
            quickfixData = quickfixData,
        )
    }

    companion object {
        private const val InsufficientlyRandomValuesIssueId = "InsufficientlyRandomValuesIssueId"
        private const val InsufficientlyRandomValuesIssueDescription = """
            Use of java.util.Random is prohibited
        """
        private const val InsufficientlyRandomValuesIssueExplanation = """
            When software generates predictable values in a context requiring unpredictability, it 
            may be possible for an attacker to guess the next value that will be generated, and 
            use this guess to impersonate another user or access sensitive information.
        """

        private val IMPLEMENTATION = Implementation(
            InsufficientlyRandomValuesDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-330: Use of Insufficiently Random Values
         * OWASP Top 10: M5: Insufficient Cryptography
         * OWASP MASVS: MSTG-CRYPTO-6
         * url: https://cwe.mitre.org/data/definitions/330.html
         * explanation: https://stackoverflow.com/questions/11051205/difference-between-java-util-random-and-java-security-securerandom
         */
        val ISSUE = Issue.create(
            id = InsufficientlyRandomValuesIssueId,
            briefDescription = InsufficientlyRandomValuesIssueDescription,
            explanation = InsufficientlyRandomValuesIssueExplanation,
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}