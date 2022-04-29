@file:Suppress("UnstableApiUsage")

package com.kiro.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.*
import com.kiro.lint.detectors.*

class IssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            IncorrectDefaultPermissionsDetector.ISSUE,
            SQLInjectionDetector.ISSUE,
            InsufficientlyRandomValuesDetector.ISSUE,
            InsufficientCryptographyDetector.ISSUE,
            InsecureWebViewImplementationDetector.ISSUE,
            IPAddressDisclosureDetector.ISSUE,
        )

    override val api: Int = CURRENT_API
}