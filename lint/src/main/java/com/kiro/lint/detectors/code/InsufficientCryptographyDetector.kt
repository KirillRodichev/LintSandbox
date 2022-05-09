package com.kiro.lint.detectors.code

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import com.kiro.lint.constants.Cryptography
import com.kiro.lint.constants.Cryptography.*
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression

@Suppress("UnstableApiUsage")
class InsufficientCryptographyDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("getInstance")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.CIPHER.packageName)) {
            val argument = node.valueArguments[0]
            val argumentValue = argument.evaluate()
            if (argumentValue is String) {
                val algorithmParts = argumentValue.split("/")
                val algorithmName = algorithmParts.getOrNull(0)
                val algorithmMode = algorithmParts.getOrNull(1)
                val algorithmPadding = algorithmParts.getOrNull(2)
                var reportMessage: String? = null

                val isInsufficientCypherUsed = Cryptography
                    .packageNameToInsufficientAlgorithmsMap[JavaCryptoPackagesEnum.CIPHER]
                    ?.contains(algorithmName) == true
                if (isInsufficientCypherUsed) {
                    reportMessage = packageSpecificReportMessagesMap[SpecificMessageKeysEnum.Generic]!!.format(algorithmName)
                } else if (
                    algorithmMode == "CBC" &&
                    (algorithmPadding == "PKCS5Padding" || algorithmPadding == "PKCS7Padding")
                ) {
                    reportMessage = packageSpecificReportMessagesMap[SpecificMessageKeysEnum.CBC]!!
                } else if (
                    algorithmMode == "ECB"
                ) {
                    reportMessage = packageSpecificReportMessagesMap[SpecificMessageKeysEnum.ECB]!!
                } else if (algorithmName == "AES" && algorithmMode == null && algorithmPadding == null) {
                    reportMessage = packageSpecificReportMessagesMap[SpecificMessageKeysEnum.AES]!!
                } else if (algorithmName == "RSA" && algorithmPadding == "NoPadding") {
                    reportMessage = packageSpecificReportMessagesMap[SpecificMessageKeysEnum.RSA]!!
                }
                if (reportMessage != null) {
                    reportUsage(context, argument, argumentValue, "AES/CBC/NoPadding", reportMessage)
                }
            }
        } else if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.MESSAGE_DIGEST.packageName)) {
            checkPackage(
                context,
                node,
                JavaCryptoPackagesEnum.MESSAGE_DIGEST,
                MessageDigestAlgorithmNamesEnum.SHA_256.algorithmName,
            )
        } else if (evaluator.isMemberInClass(method, JavaCryptoPackagesEnum.KEY_GENERATOR.packageName)) {
            checkPackage(
                context,
                node,
                JavaCryptoPackagesEnum.KEY_GENERATOR,
                KeyGeneratorAlgorithmNamesEnum.AES.algorithmName,
            )
        }
    }

    private fun checkPackage(
        context: JavaContext,
        node: UCallExpression,
        packageName: JavaCryptoPackagesEnum,
        replaceWith: String,
    ) {
        val argument = node.valueArguments[0]
        val argumentValue = argument.evaluate()
        val insufficientAlgorithmNames = Cryptography.packageNameToInsufficientAlgorithmsMap[packageName]
        if (argumentValue is String && insufficientAlgorithmNames != null) {
            reportIfIsInsufficientAlgorithm(
                insufficientAlgorithmNames,
                argumentValue,
                context,
                argument,
                argumentValue,
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
        message: String = ISSUE.getBriefDescription(TextFormat.TEXT),
    ) {
        val reportMessage = """
            $message \
            **CWE-327: Use of a Broken or Risky Cryptographic Algorithm** https://cwe.mitre.org/data/definitions/327.html
        """.trimIndent()

        val quickfixData = LintFix.create()
            .name("Replace with a sufficient algorithm: %s".format(replaceWith))
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
            message = reportMessage,
            quickfixData = quickfixData,
        )
    }

    companion object {
        enum class SpecificMessageKeysEnum {
            CBC,
            ECB,
            AES,
            RSA,
            Generic,
        }

        val packageSpecificReportMessagesMap = mapOf(
            SpecificMessageKeysEnum.CBC to """
                The App uses the encryption mode CBC with PKCS5/PKCS7 padding. \
                This configuration is vulnerable to padding oracle attacks.
            """.trimIndent(),
            SpecificMessageKeysEnum.ECB to "The ECB mode is known to be a weak",
            SpecificMessageKeysEnum.AES to """
                Calling `Cipher.getInstance("AES")` will return AES ECB mode by default. \
                ECB mode is known to be weak as it results in the same ciphertext for identical blocks of plaintext.
            """.trimIndent(),
            SpecificMessageKeysEnum.RSA to """
                This App uses RSA Crypto without OAEP padding. The purpose of the padding scheme is to prevent a \
                number of attacks on RSA that only work when the encryption is performed without padding.
            """.trimIndent(),
            SpecificMessageKeysEnum.Generic to "The %s is known to be a weak cryptographic algorithm."
        )

        private const val InsufficientCryptographyIssueId = "InsufficientCryptographyIssueId"
        private const val InsufficientCryptographyIssueDescription = """
            The use of a broken or risky cryptographic algorithm is an unnecessary risk that may \ 
            result in the exposure of sensitive information.
        """
        private const val InsufficientCryptographyIssueExplanation = """
            The use of a non-standard algorithm is dangerous because a determined attacker may be \
            able to break the algorithm and compromise whatever data has been protected. \
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