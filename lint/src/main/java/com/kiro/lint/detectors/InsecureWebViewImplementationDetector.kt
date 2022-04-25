package com.kiro.lint.detectors

import com.android.SdkConstants.*
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.utils.forEach
import com.intellij.psi.JavaTokenType.IDENTIFIER
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.JavaElementType.METHOD_CALL_EXPRESSION
import com.intellij.psi.impl.source.tree.JavaElementType.REFERENCE_EXPRESSION
import org.jetbrains.uast.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UnstableApiUsage")
class InsecureWebViewImplementationDetector : Detector(), SourceCodeScanner, XmlScanner {

    private var stringExtraVariableName: String = ""
    private var stringExtraCallerInstanceName: String = ""
    private var isCallerMatched: Boolean = false
    private var isVariableMatched: Boolean = false
    private var exportedActivityNames: ArrayList<String> = ArrayList()

    override fun getApplicableElements() = listOf(TAG_MANIFEST)

    /**
     * Fills the array of exported activities
     *
     * The activity is exported if
     * - it explicitly declares the “exported=true” attribute
     * - it has intent filters & no “exported=false” attribute
     */
    override fun visitElement(context: XmlContext, element: Element) {
        val document = context.client.getMergedManifest(context.project)
        val activities = document?.getElementsByTagName(TAG_ACTIVITY)
        activities?.forEach { activity ->
            val name = activity.attributes.getNamedItemNS(ANDROID_NS, ATTRIBUTE_NAME)?.textContent
            if (name != null) {
                val exportedAttribute = activity.attributes.getNamedItemNS(ANDROID_NS, ATTRIBUTE_EXPORTED)?.textContent
                val intentFilter = activity.childNodes.find { node ->
                    node.nodeName == TAG_INTENT_FILTER
                }
                if (
                    exportedAttribute == "true" || // exported explicitly
                    intentFilter != null && exportedAttribute != "false" // exported implicitly
                ) {
                    exportedActivityNames.add(name.drop(1)) // removing leading "."
                }
            }
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? =
        listOf(UDeclarationsExpression::class.java)

    /**
     * Walks through the declaration expressions
     * if a value is assigned to the result of "getStringExtra" method call then
     * - the resulting variable is written to "stringExtraVariableName" and
     * - the "getStringExtra" caller is written to "stringExtraCallerInstanceName"
     */
    override fun createUastHandler(context: JavaContext): UElementHandler? =
        object : UElementHandler() {
            override fun visitDeclarationsExpression(node: UDeclarationsExpression) {
                val declaration = node.declarations[0] // var str: java.lang.String = i.getStringExtra("reg_url")
                val variableIdentifier = declaration.node.findChildByType(IDENTIFIER) // PsiIdentifier:str
                val valueMethodCallExpression = declaration.node.findChildByType(METHOD_CALL_EXPRESSION) // PsiMethodCallExpression:i.getStringExtra("reg_url")
                val valueInstanceMethodCallRef = valueMethodCallExpression?.findChildByType(REFERENCE_EXPRESSION) // PsiReferenceExpression:i.getStringExtra
                val instanceMethodCallName = valueInstanceMethodCallRef?.findChildByType(IDENTIFIER)?.text // getStringExtra

                if (instanceMethodCallName == "getStringExtra") {
                    val valueInstanceRef = valueInstanceMethodCallRef.findChildByType(REFERENCE_EXPRESSION) // PsiReferenceExpression:i
                    val instanceName = valueInstanceRef?.findChildByType(IDENTIFIER)?.text // i
                    stringExtraVariableName = variableIdentifier?.text ?: "" // str
                    stringExtraCallerInstanceName = instanceName ?: ""
                }
            }
        }

    override fun getApplicableMethodNames(): List<String>? =
        listOf("getStringExtra", "loadUrl")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        // TODO: improve location check: file -> class
        val nodeFileName = context.getNameLocation(node).file.name
        if (exportedActivityNames.contains(nodeFileName.dropLast(5))) {
            val evaluator = context.evaluator
            if (evaluator.isMemberInClass(method, "android.webkit.WebView")) {
                val argument = node.valueArguments[0]
                val argumentMethodName = argument.sourcePsi.findContaining(UCallExpression::class.java)?.methodName

                if (argument.toString() == stringExtraVariableName) {
                    // in case of the getStringExtra result was declared as a separate variable
                    isCallerMatched = true
                } else if (argumentMethodName == "getStringExtra") {
                    // in case of the getStringExtra was passed directly to the loadUrl method
                    reportUsage(context, node, method)
                    return
                }
            } else if (evaluator.isMemberInClass(method, "android.content.Intent")) {
                if (stringExtraCallerInstanceName == node.receiver.toString()) {
                    isVariableMatched = true
                }
            }
            if (isCallerMatched && isVariableMatched) {
                reportUsage(context, node, method)
            }
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
        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

        private const val ATTRIBUTE_NAME = "name"
        private const val ATTRIBUTE_EXPORTED = "exported"

        fun NodeList?.find(fn: (node: Node) -> Boolean): Node? {
            if (this == null) {
                return null
            } else {
                for (i in 0 until this.length) {
                    val node = this.item(i)
                    if (fn(node)) {
                        return node
                    }
                }
                return null
            }
        }

        private const val InsecureWebViewImplementationIssueId = "InsecureWebViewImplementationIssueId"
        private const val InsecureWebViewImplementationIssueDescription = """
            The software provides an Applications Programming Interface (API) or similar interface 
            for interaction with external actors, but the interface includes a dangerous method or 
            function that is not properly restricted.
        """
        private const val InsecureWebViewImplementationIssueExplanation = """
            This weakness can lead to a wide variety of resultant weaknesses, depending on the 
            behavior of the exposed method. It can apply to any number of technologies and 
            approaches, such as ActiveX controls, Java functions, IOCTLs, and so on.
        """

        private val IMPLEMENTATION = Implementation( // TODO: fix scope
            InsecureWebViewImplementationDetector::class.java,
            EnumSet.of(Scope.MANIFEST, Scope.JAVA_FILE),
            Scope.MANIFEST_SCOPE,
            Scope.JAVA_FILE_SCOPE
        )

        /**
         * CWE: CWE-749: Exposed Dangerous Method or Function
         * OWASP Top 10: M1: Improper Platform Usage
         * OWASP MASVS: MSTG-PLATFORM-7
         * url: https://cwe.mitre.org/data/definitions/749.html
         */
        val ISSUE = Issue.create(
            id = InsecureWebViewImplementationIssueId,
            briefDescription = InsecureWebViewImplementationIssueDescription,
            explanation = InsecureWebViewImplementationIssueExplanation, // TODO
            category = Category.SECURITY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION
        )
    }
}