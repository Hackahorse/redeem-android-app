package com.example.life365.util.validator

import com.example.life365.util.validator.CharSequenceValidator

/**
 * [CharSequenceValidator] which validates text by regular expression
 */
open class RegexValidator(pattern: String) : CharSequenceValidator {
    private val regex = Regex(pattern)

    override fun isValid(sequence: CharSequence?): Boolean {
        sequence ?: return false
        return regex.matches(sequence)
    }
}