package com.kiro.lint.detectors

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
class SQLInjectionDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String>? =
        listOf("execSQL")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (context.evaluator.isMemberInClass(method, "android.database.sqlite.SQLiteDatabase")) {
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
            //scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = false,
                includeArguments = false
            ),
            message = ISSUE.getBriefDescription(TextFormat.TEXT),
        )
    }

    companion object {
        private const val SQLInjectionIssueIssueId = "SQLInjectionIssueId"
        private val SQLInjectionIssueIssueDescription = """
            The software constructs all or part of an SQL command using externally-influenced input 
            from an upstream component, but it does not neutralize or incorrectly neutralizes 
            special elements that could modify the intended SQL command when it is sent to a 
            downstream component
        """
        private val SQLInjectionIssueIssueExplanation = """
            Without sufficient removal or quoting of SQL syntax in user-controllable inputs, the 
            generated SQL query can cause those inputs to be interpreted as SQL instead of ordinary 
            user data. This can be used to alter query logic to bypass security checks, or to 
            insert additional statements that modify the back-end database, possibly including 
            execution of system commands.
        """

        private val IMPLEMENTATION = Implementation(
            SQLInjectionDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-89: Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection')
         * OWASP Top 10: M7: Client Code Quality
         * url: https://cwe.mitre.org/data/definitions/89.html
         */
        val ISSUE = Issue.create(
            id = SQLInjectionIssueIssueId,
            briefDescription = SQLInjectionIssueIssueDescription,
            explanation = SQLInjectionIssueIssueExplanation,
            category = Category.SECURITY,
            priority = 3,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )
    }
}