package me.mirimomekiku.safepass.helpers

import com.nulabinc.zxcvbn.AttackTimes
import com.nulabinc.zxcvbn.Feedback
import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.matchers.Match
import java.security.SecureRandom

object Password {

    private val zxcvbn = Zxcvbn()
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?/"
    private val SIMILAR_CHARS = "Il1Lo0O"

    private val secureRandom = SecureRandom()

    data class Result(
        val score: Int,
        val percent: Int,
        val label: String,
        val feedback: Feedback?,
        val crackTimes: AttackTimes.CrackTimesDisplay?,
        val guesses: Double?,
        val sequence: List<Match>
    )

    fun generate(
        length: Int = 16,
        useLowercase: Boolean = true,
        useUppercase: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true,
        excludeSimilar: Boolean = false,
        excludeSequential: Boolean = false,
        excludeRepeated: Boolean = false,
        startWithLetter: Boolean = false
    ): String {
        require(length > 0) { "Password length must be positive" }

        val charSets = mutableListOf<String>()
        if (useLowercase) charSets.add(LOWERCASE)
        if (useUppercase) charSets.add(UPPERCASE)
        if (useNumbers) charSets.add(NUMBERS)
        if (useSymbols) charSets.add(SYMBOLS)

        require(charSets.isNotEmpty()) { "At least one character set must be enabled!" }

        var allChars = charSets.joinToString("")
        if (excludeSimilar) {
            allChars = allChars.filter { !SIMILAR_CHARS.contains(it) }
        }

        val passwordChars = mutableListOf<Char>()

        if (startWithLetter) {
            val letters = (LOWERCASE + UPPERCASE).filter { allChars.contains(it) }
            passwordChars.add(letters.random(secureRandom))
        }

        while (passwordChars.size < length) {
            val nextChar = allChars.random(secureRandom)

            if (excludeRepeated && passwordChars.lastOrNull() == nextChar) continue
            if (excludeSequential && passwordChars.size >= 2) {
                val last = passwordChars.takeLast(2)
                val seqForward =
                    last[0].code + 1 == last[1].code && last[1].code + 1 == nextChar.code
                val seqBackward =
                    last[0].code - 1 == last[1].code && last[1].code - 1 == nextChar.code
                if (seqForward || seqBackward) continue
            }

            passwordChars.add(nextChar)
        }

        if (!startWithLetter) passwordChars.shuffle(secureRandom)

        return passwordChars.joinToString("")
    }

    fun analyze(password: String): Result {
        if (password.isEmpty()) {
            return Result(
                score = 0,
                percent = 0,
                label = "Empty",
                feedback = null,
                crackTimes = null,
                guesses = null,
                sequence = emptyList()
            )
        }

        val result = zxcvbn.measure(password)
        val score = result.score
        val percent = (score * 25)

        val label = when (score) {
            4 -> "Very Strong"
            3 -> "Strong"
            2 -> "Medium"
            1 -> "Weak"
            else -> "Very Weak"
        }

        return Result(
            score = score,
            percent = percent,
            label = label,
            feedback = result.feedback,
            crackTimes = result.crackTimesDisplay,
            guesses = result.guesses,
            sequence = result.sequence
        )
    }

    private fun String.random(random: SecureRandom) = this[random.nextInt(this.length)]
}

object CardHelper {
    fun detectCardType(number: String): String {
        val clean = number.filter { it.isDigit() }

        return when {
            clean.startsWith("4") -> "Visa"
            clean.matches(Regex("^5[1-5].*")) -> "Mastercard"
            clean.matches(Regex("^2(2[2-9]|[3-6]\\d|7[01]|720).*")) -> "Mastercard"
            clean.matches(Regex("^3[47].*")) -> "American Express"
            clean.matches(Regex("^3(0[0-5]|[68]).*")) -> "Diners Club"
            clean.matches(Regex("^(6011|65|64[4-9]).*")) -> "Discover"
            clean.matches(Regex("^35(2[89]|[3-8][0-9]).*")) -> "JCB"
            clean.matches(Regex("^62.*")) -> "UnionPay"
            clean.matches(Regex("^5[06-9].*")) -> "Maestro"
            clean.matches(Regex("^6[0-9]{12,19}")) -> "RuPay"
            clean.matches(Regex("^220[0-4].*")) -> "MIR"
            else -> "Unknown"
        }
    }
}