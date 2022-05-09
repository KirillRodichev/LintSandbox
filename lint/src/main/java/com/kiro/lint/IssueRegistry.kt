@file:Suppress("UnstableApiUsage")

package com.kiro.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.*
import com.kiro.lint.detectors.code.*
import com.kiro.lint.detectors.complex.InsecureWebViewImplementationDetector
import com.kiro.lint.detectors.manifest.*

class IssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            IncorrectDefaultPermissionsDetector.ISSUE,
            SQLInjectionDetector.ISSUE,
            InsufficientlyRandomValuesDetector.ISSUE,
            InsufficientCryptographyDetector.ISSUE,
            InsecureWebViewImplementationDetector.ISSUE,
            IPAddressDisclosureDetector.ISSUE,
            NotProtectedApplicationComponentsDetector.ISSUE,
            InsecureApplicationComponentsDetector.ISSUE,
            CleartextTrafficUsageDetector.ISSUE,
            DebuggableApplicationDetector.ISSUE,
            TestOnlyApplicationDetector.ISSUE,
            AllowBackupApplicationDetector.ISSUE,
            AndroidSecretCodeUsageDetector.ISSUE,
            HiddenElementsDetector.ISSUE,
        )

    override val api: Int = CURRENT_API
}