package com.kiro.lint.detectors.code

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression

@Suppress("UnstableApiUsage")
class InsecureFileOperatingModeDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("openFileOutput")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (context.evaluator.isMemberInClass(method, "android.content.Context")) {
            val argument = node.valueArguments[1]
            val argumentValue = argument.evaluate()

            if (argumentValue == MODE_WORLD_READABLE || argumentValue == MODE_WORLD_WRITEABLE) {
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
        const val MODE_WORLD_READABLE = 1
        const val MODE_WORLD_WRITEABLE = 2

        private const val InsecureFileOperatingModeIssueId = "InsecureFileOperatingModeIssueId"
        private val InsecureFileOperatingModeIssueDescription = """
            The file is World Readable or Writable. Any App can read/write to the file. \
            **CWE-276: Incorrect Default Permissions** https://cwe.mitre.org/data/definitions/276.html
        """.trimIndent()

        private val IMPLEMENTATION = Implementation(
            InsecureFileOperatingModeDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-276: Incorrect Default Permissions
         * OWASP Top 10: M2: Insecure Data Storage
         * OWASP MASVS: MSTG-STORAGE-2
         * url: https://cwe.mitre.org/data/definitions/276.html
         */
        val ISSUE = Issue.create(
            id = InsecureFileOperatingModeIssueId,
            briefDescription = InsecureFileOperatingModeIssueDescription,
            explanation = InsecureFileOperatingModeIssueDescription,
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}