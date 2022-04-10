package com.kiro.lint.detectors

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression

@Suppress("UnstableApiUsage")
class InsufficientCryptographyDetector : Detector(), SourceCodeScanner {
    private enum class CipherAlgorithmNamesEnum(val algorithmName: String) {
        AES("AES"),
        AES_WRAP("AESWrap"),
        ARCFOUR("ARCFOUR"),
        BLOWFISH("Blowfish"),
        CCM("CCM"),
        DES("DES"),
        DES_EDE("DESede"),
        DES_EDE_WRAP("DESedeWrap"),
        ECIES("ECIES"),
        GCM("GCM"),
        RC2("RC2"),
        RC4("RC4"),
        RC5("RC5"),
        RSA("RSA"),
    }

    override fun getApplicableMethodNames(): List<String>? =
        listOf("getInstance")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (context.evaluator.isMemberInClass(method, "javax.crypto.Cipher")) {
            val argument = node.valueArguments[0]
            val value = ConstantEvaluator.evaluate(context, argument)
            if (value is String) {
                val algorithmName = value.split("/")[0]
                if (algorithmName != CipherAlgorithmNamesEnum.AES.algorithmName) {
                    reportUsage(context, node, argument, value)
                }
            }
        }
    }

    private fun reportUsage(
        context: JavaContext,
        node: UCallExpression,
        argument: UExpression,
        replaceText: String
    ) {
        val quickfixData = LintFix.create()
            .name("Replace with AES")
            .replace()
            .text("DES/CBC/NoPadding")
            .with("AES/CBC/NoPadding")
            /*.robot(true) // Can be applied automatically.
            .independent(true) // Does not conflict with other auto-fixes.*/
            .build()

        context.report(
            issue = ISSUE,
            scope = argument,
            location = context.getLocation(argument),
            message = ISSUE.getBriefDescription(TextFormat.TEXT),
            quickfixData = quickfixData,
        )
    }

    companion object {
        private const val InsufficientCryptographyIssueId = "InsufficientCryptographyIssueId"
        private const val InsufficientCryptographyIssueDescription = """
            The use of a broken or risky cryptographic algorithm is an unnecessary risk that may 
            result in the exposure of sensitive information.
        """
        private const val InsufficientCryptographyIssueExplanation = """
            The use of a non-standard algorithm is dangerous because a determined attacker may be 
            able to break the algorithm and compromise whatever data has been protected. 
            Well-known techniques may exist to break the algorithm.
        """

        private val IMPLEMENTATION = Implementation(
            InsufficientCryptographyDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-327: Use of a Broken or Risky Cryptographic Algorithm
         * OWASP Top 10: M5: Insufficient Cryptography
         * OWASP MASVS: MSTG-CRYPTO-4
         * url: https://cwe.mitre.org/data/definitions/327.html
         */
        val ISSUE = Issue.create(
            id = InsufficientCryptographyIssueId,
            briefDescription = InsufficientCryptographyIssueDescription,
            explanation = InsufficientCryptographyIssueExplanation,
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}