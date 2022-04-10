@file:Suppress("UnstableApiUsage")

package com.kiro.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.*
import com.kiro.lint.detectors.IncorrectDefaultPermissionsDetector
import com.kiro.lint.detectors.InsufficientlyRandomValuesDetector
import com.kiro.lint.detectors.SQLInjectionDetector

class IssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            IncorrectDefaultPermissionsDetector.ISSUE,
            SQLInjectionDetector.ISSUE,
            InsufficientlyRandomValuesDetector.ISSUE,
        )

    override val api: Int = CURRENT_API
}