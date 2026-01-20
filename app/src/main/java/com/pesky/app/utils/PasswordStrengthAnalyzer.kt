package com.pesky.app.utils

import com.pesky.app.data.models.PasswordStrength
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes password strength using a zxcvbn-style algorithm.
 * Considers entropy, common patterns, and character variety.
 */
@Singleton
class PasswordStrengthAnalyzer @Inject constructor() {
    
    companion object {
        // Common password patterns to penalize
        private val COMMON_PASSWORDS = setOf(
            "password", "123456", "12345678", "qwerty", "abc123", "monkey", "1234567",
            "letmein", "trustno1", "dragon", "baseball", "iloveyou", "master", "sunshine",
            "ashley", "bailey", "passw0rd", "shadow", "123123", "654321", "superman",
            "qazwsx", "michael", "football", "password1", "password123", "welcome",
            "jesus", "ninja", "mustang", "password1234", "admin", "root", "login"
        )
        
        private val KEYBOARD_PATTERNS = listOf(
            "qwerty", "qwertz", "azerty", "asdfgh", "zxcvbn", "1234567890",
            "qazwsx", "qazedc", "1qaz2wsx", "!qaz@wsx"
        )
        
        private val SEQUENTIAL = "abcdefghijklmnopqrstuvwxyz0123456789"
    }
    
    /**
     * Analyzes a password and returns detailed strength information.
     */
    fun analyze(password: String): PasswordAnalysisResult {
        if (password.isEmpty()) {
            return PasswordAnalysisResult(
                strength = PasswordStrength.VERY_WEAK,
                score = 0,
                entropy = 0.0,
                feedback = listOf("Password is empty")
            )
        }
        
        val feedback = mutableListOf<String>()
        var score = 0.0
        
        // Calculate base entropy
        val charsetSize = calculateCharsetSize(password)
        val entropy = password.length * kotlin.math.log2(charsetSize.toDouble())
        
        // Base score from length
        score += password.length * 4
        
        // Bonus for character variety
        if (password.any { it.isUpperCase() }) score += 10
        if (password.any { it.isLowerCase() }) score += 10
        if (password.any { it.isDigit() }) score += 10
        if (password.any { !it.isLetterOrDigit() }) score += 15
        
        // Bonus for mixed case
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) {
            score += 10
        }
        
        // Penalty for common passwords
        if (COMMON_PASSWORDS.contains(password.lowercase())) {
            score -= 50
            feedback.add("This is a commonly used password")
        }
        
        // Penalty for keyboard patterns
        val lowerPassword = password.lowercase()
        if (KEYBOARD_PATTERNS.any { lowerPassword.contains(it) }) {
            score -= 20
            feedback.add("Avoid keyboard patterns")
        }
        
        // Penalty for sequential characters
        if (hasSequentialChars(password, 3)) {
            score -= 15
            feedback.add("Avoid sequential characters")
        }
        
        // Penalty for repeated characters
        if (hasRepeatedChars(password, 3)) {
            score -= 15
            feedback.add("Avoid repeated characters")
        }
        
        // Penalty for all same case
        if (password.all { it.isUpperCase() } || password.all { it.isLowerCase() }) {
            score -= 10
            feedback.add("Mix uppercase and lowercase letters")
        }
        
        // Penalty for numbers only
        if (password.all { it.isDigit() }) {
            score -= 20
            feedback.add("Don't use numbers only")
        }
        
        // Length-based feedback
        if (password.length < 8) {
            feedback.add("Password is too short (minimum 8 characters)")
        } else if (password.length < 12) {
            feedback.add("Consider using a longer password")
        }
        
        // Character type feedback
        if (!password.any { it.isUpperCase() }) {
            feedback.add("Add uppercase letters")
        }
        if (!password.any { it.isLowerCase() }) {
            feedback.add("Add lowercase letters")
        }
        if (!password.any { it.isDigit() }) {
            feedback.add("Add numbers")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            feedback.add("Add special characters")
        }
        
        // Normalize score to 0-100
        val normalizedScore = score.coerceIn(0.0, 100.0).toInt()
        
        // Determine strength
        val strength = when {
            normalizedScore < 20 -> PasswordStrength.VERY_WEAK
            normalizedScore < 40 -> PasswordStrength.WEAK
            normalizedScore < 60 -> PasswordStrength.FAIR
            normalizedScore < 80 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
        
        return PasswordAnalysisResult(
            strength = strength,
            score = normalizedScore,
            entropy = entropy,
            feedback = feedback.take(3) // Limit to top 3 suggestions
        )
    }
    
    private fun calculateCharsetSize(password: String): Int {
        var size = 0
        if (password.any { it.isLowerCase() }) size += 26
        if (password.any { it.isUpperCase() }) size += 26
        if (password.any { it.isDigit() }) size += 10
        if (password.any { !it.isLetterOrDigit() }) size += 32
        return if (size == 0) 1 else size
    }
    
    private fun hasSequentialChars(password: String, minLength: Int): Boolean {
        val lower = password.lowercase()
        for (i in 0 until lower.length - minLength + 1) {
            val substring = lower.substring(i, i + minLength)
            if (SEQUENTIAL.contains(substring)) return true
            if (SEQUENTIAL.reversed().contains(substring)) return true
        }
        return false
    }
    
    private fun hasRepeatedChars(password: String, minLength: Int): Boolean {
        if (password.length < minLength) return false
        
        for (i in 0 until password.length - minLength + 1) {
            val char = password[i]
            var repeated = true
            for (j in 1 until minLength) {
                if (password[i + j] != char) {
                    repeated = false
                    break
                }
            }
            if (repeated) return true
        }
        return false
    }
}

/**
 * Result of password strength analysis.
 */
data class PasswordAnalysisResult(
    val strength: PasswordStrength,
    val score: Int, // 0-100
    val entropy: Double,
    val feedback: List<String>
) {
    val strengthPercentage: Float = score / 100f
}
