package com.kiro.lint.detectors

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import com.kiro.lint.constants.Cryptography
import com.kiro.lint.constants.Cryptography.*
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression

@Suppress("UnstableApiUsage")
class InsufficientCryptographyDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String>? =
        listOf("getInstance")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.CIPHER.packageName)) {
            val argument = node.valueArguments[0]
            val value = ConstantEvaluator.evaluate(context, argument)
            if (value is String) {
                val algorithmName = value.split("/")[0]
                if (algorithmName != CipherAlgorithmNamesEnum.AES.algorithmName &&
                    algorithmName != CipherAlgorithmNamesEnum.RSA.algorithmName
                ) {
                    reportUsage(context, argument, value, "AES/CBC/NoPadding")
                }
            }
        } else if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.MESSAGE_DIGEST.packageName)) {
            checkPackage(
                context,
                node,
                JavaCryptoPackagesEnum.MESSAGE_DIGEST.packageName,
                MessageDigestAlgorithmNamesEnum.SHA_256.algorithmName,
            )
        } else if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.KEY_GENERATOR.packageName)) {
            checkPackage(
                context,
                node,
                JavaCryptoPackagesEnum.KEY_GENERATOR.packageName,
                KeyGeneratorAlgorithmNamesEnum.AES.algorithmName,
            )
        }
    }

    private fun checkPackage(
        context: JavaContext,
        node: UCallExpression,
        packageName: String,
        replaceWith: String,
    ) {
        val argument = node.valueArguments[0]
        val value = ConstantEvaluator.evaluate(context, argument)
        val insufficientAlgorithmNames = Cryptography.packageNameToInsufficientAlgorithmsMap[packageName]
        if (value is String && insufficientAlgorithmNames != null) {
            reportIfIsInsufficientAlgorithm(
                insufficientAlgorithmNames,
                value,
                context,
                argument,
                value,
                replaceWith,
            )
        }
    }

    private fun reportIfIsInsufficientAlgorithm(
        insufficientAlgorithmNames: List<String>,
        algorithm: String,
        context: JavaContext,
        node: UExpression,
        replaceText: String,
        replaceWith: String,
    ) {
        if (insufficientAlgorithmNames.contains(algorithm)) {
            reportUsage(context, node, replaceText, replaceWith)
        }
    }

    private fun reportUsage(
        context: JavaContext,
        node: UExpression,
        replaceText: String,
        replaceWith: String,
    ) {
        val quickfixData = LintFix.create()
            .name("Replace with a sufficient algorithm")
            .replace()
            .text(replaceText)
            .with(replaceWith)
            .robot(true)
            .independent(true)
            .build()

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
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