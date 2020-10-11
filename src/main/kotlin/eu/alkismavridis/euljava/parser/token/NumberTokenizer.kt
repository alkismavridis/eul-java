package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.FloatLiteral
import eu.alkismavridis.euljava.core.ast.expressions.tokens.IntegerLiteral
import java.lang.Double.parseDouble
import java.lang.Long.parseLong

/** Returns either IntegerLiteral or FloatLiteral */
class NumberTokenizer(
    private val logger: EulLogger,
    private val source: CharacterSource,
    private val options: CompileOptions
) {
    private val builder = StringBuilder()

    fun getNextNumberToken(startingLine: Int, startingColumn: Int): EulToken {
        this.builder.setLength(0)
        val endingChar = this.addAllDecimalDigits()

        when (endingChar) {
            '.' -> return this.parseFloatingPointPart(startingLine, startingColumn)
            'u' -> return this.parseSizePart(false, startingLine, startingColumn)
            's' -> return this.parseSizePart(true, startingLine, startingColumn)
            'f' -> return this.parseSizePartForFloat(startingLine, startingColumn)

            else -> {
                this.source.rollBackCharacter(endingChar)
                val integerValue = this.convertBufferToLong(startingLine, startingColumn)
                return IntegerLiteral(integerValue, this.options.defaultIntSizeBits, true, startingLine, startingColumn)
            }
        }
    }

    private fun parseFloatingPointPart(startingLine: Int, startingColumn: Int): FloatLiteral {
        this.builder.append('.')
        val endingChar = this.addAllDecimalDigits()

        if (endingChar == 'f') {
            return this.parseSizePartForFloat(startingLine, startingColumn)
        } else {
            this.source.rollBackCharacter(endingChar)
            val doubleValue = this.convertBufferToDouble(startingLine, startingColumn)
            return FloatLiteral(doubleValue, this.options.defaultFloatSizeBits, startingLine, startingColumn)
        }
    }

    private fun parseSizePart(isSigned: Boolean, startingLine: Int, startingColumn: Int): IntegerLiteral {
        val integerValue = this.convertBufferToLong(startingLine, startingColumn)
        val endingChar = this.addAllDecimalDigits()
        this.source.rollBackCharacter(endingChar)

        val desiredSize = if (this.builder.isEmpty()) this.options.defaultIntSizeBits
        else this.convertBufferToLong(startingLine, startingColumn).toInt()

        return IntegerLiteral(integerValue, desiredSize, isSigned, startingLine, startingColumn)
    }

    private fun parseSizePartForFloat(startingLine: Int, startingColumn: Int): FloatLiteral {
        val integerValue = this.convertBufferToDouble(startingLine, startingColumn)
        val endingChar = this.addAllDecimalDigits()
        this.source.rollBackCharacter(endingChar)

        val desiredSize = if (this.builder.isEmpty()) this.options.defaultFloatSizeBits
        else this.convertBufferToLong(startingLine, startingColumn).toInt()

        return FloatLiteral(integerValue, desiredSize, startingLine, startingColumn)
    }

    /** Returns the first non-decimal character found */
    private fun addAllDecimalDigits(): Char {
        while (true) {
            val nextChar = this.source.getNextChar()
            if (!CharMetadata.isDecimalDigit(nextChar)) {
                return nextChar
            }
            this.builder.append(nextChar)
        }
    }

    private fun convertBufferToLong(startingLine: Int, startingColumn: Int): Long {
        val valueString = this.builder.toString()
        this.builder.setLength(0)

        try {
            return parseLong(valueString)
        } catch (e: NumberFormatException) {
            throw TokenizerException(startingLine, startingColumn, "Invalid integer number: $valueString")
        }
    }

    private fun convertBufferToDouble(startingLine: Int, startingColumn: Int): Double {
        val valueString = this.builder.toString()
        this.builder.setLength(0)

        try {
            return parseDouble(valueString)
        } catch (e: NumberFormatException) {
            throw TokenizerException(startingLine, startingColumn, "Invalid float number: $valueString")
        }
    }
}
