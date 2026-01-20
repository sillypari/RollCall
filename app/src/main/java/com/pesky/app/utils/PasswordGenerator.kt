package com.pesky.app.utils

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates cryptographically secure random passwords.
 */
@Singleton
class PasswordGenerator @Inject constructor() {
    
    companion object {
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "!@#\$%^&*()_+-=[]{}|;:,.<>?"
        private const val AMBIGUOUS = "0O1lI"
        
        const val DEFAULT_LENGTH = 16
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 128
        
        // Common words for passphrase generation
        private val COMMON_WORDS = listOf(
            "apple", "banana", "cherry", "dragon", "eagle", "falcon", "galaxy",
            "hammer", "island", "jungle", "kernel", "lemon", "mountain", "nebula",
            "orange", "planet", "quantum", "river", "sunset", "thunder", "unicorn",
            "valley", "whisper", "xenon", "yellow", "zephyr", "anchor", "bridge",
            "castle", "diamond", "emerald", "forest", "glacier", "harbor", "ivory",
            "jasmine", "knight", "lunar", "meadow", "north", "ocean", "phoenix",
            "quartz", "rainbow", "silver", "temple", "ultra", "violet", "winter",
            "crystal", "bronze", "cosmic", "digital", "eternal", "frozen", "golden",
            "horizon", "infinite", "journey", "kingdom", "legend", "mystic", "noble"
        )
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generates a random password with the specified options.
     */
    fun generate(options: PasswordGeneratorOptions = PasswordGeneratorOptions()): String {
        val length = options.length.coerceIn(MIN_LENGTH, MAX_LENGTH)
        
        // Build character pool
        val charPool = buildString {
            if (options.includeUppercase) append(UPPERCASE)
            if (options.includeLowercase) append(LOWERCASE)
            if (options.includeNumbers) append(NUMBERS)
            if (options.includeSymbols) append(SYMBOLS)
        }.let { pool ->
            if (options.excludeAmbiguous) {
                pool.filterNot { it in AMBIGUOUS }
            } else {
                pool
            }
        }
        
        if (charPool.isEmpty()) {
            // Fallback to lowercase if nothing selected
            return generate(options.copy(includeLowercase = true))
        }
        
        // Generate password ensuring at least one char from each selected category
        val password = StringBuilder(length)
        val requiredChars = mutableListOf<Char>()
        
        // Add one character from each required category
        if (options.includeUppercase) {
            val chars = if (options.excludeAmbiguous) UPPERCASE.filterNot { it in AMBIGUOUS } else UPPERCASE
            requiredChars.add(chars[secureRandom.nextInt(chars.length)])
        }
        if (options.includeLowercase) {
            val chars = if (options.excludeAmbiguous) LOWERCASE.filterNot { it in AMBIGUOUS } else LOWERCASE
            requiredChars.add(chars[secureRandom.nextInt(chars.length)])
        }
        if (options.includeNumbers) {
            val chars = if (options.excludeAmbiguous) NUMBERS.filterNot { it in AMBIGUOUS } else NUMBERS
            requiredChars.add(chars[secureRandom.nextInt(chars.length)])
        }
        if (options.includeSymbols) {
            requiredChars.add(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])
        }
        
        // Fill remaining length with random characters
        val remainingLength = length - requiredChars.size
        for (i in 0 until remainingLength) {
            password.append(charPool[secureRandom.nextInt(charPool.length)])
        }
        
        // Insert required characters at random positions
        for (char in requiredChars) {
            val position = secureRandom.nextInt(password.length + 1)
            password.insert(position, char)
        }
        
        return password.toString()
    }
    
    /**
     * Generates a passphrase from random words.
     */
    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        capitalize: Boolean = true
    ): String {
        val words = (0 until wordCount).map {
            val word = COMMON_WORDS[secureRandom.nextInt(COMMON_WORDS.size)]
            if (capitalize) word.replaceFirstChar { char -> char.uppercase() } else word
        }
        return words.joinToString(separator)
    }
}

/**
 * Options for password generation.
 */
data class PasswordGeneratorOptions(
    val length: Int = PasswordGenerator.DEFAULT_LENGTH,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = false
)
