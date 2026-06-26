package com.example

import kotlin.math.*

class CalculatorEvaluator {
    /**
     * Evaluates a mathematical expression string and returns the double result.
     * Throws an exception if the expression is invalid or mathematically undefined.
     */
    fun evaluate(str: String, useRadians: Boolean = false): Double {
        // Prepare clean expression string for parsing
        val expr = str.replace("×", "*")
            .replace("÷", "/")
            .replace("π", "3.141592653589793")
            .replace("e", "2.718281828459045")
            .replace(" ", "")

        if (expr.isEmpty()) return 0.0
        return Parser(expr, useRadians).parse()
    }

    private class Parser(private val expr: String, private val useRadians: Boolean) {
        private var pos = -1
        private var ch = -1

        fun parse(): Double {
            nextChar()
            val result = parseExpression()
            if (pos < expr.length) {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar() + " at position " + pos)
            }
            return result
        }

        private fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) {
                    x += parseTerm()
                } else if (eat('-'.code)) {
                    x -= parseTerm()
                } else {
                    return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) {
                    x *= parseFactor()
                } else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else if (eat('%'.code)) {
                    x /= 100.0
                } else {
                    return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // Unary plus
            if (eat('-'.code)) return -parseFactor() // Unary minus

            var x: Double
            val startPos = pos
            if (eat('('.code)) { // Parentheses
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // Numbers
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    nextChar()
                }
                val numStr = expr.substring(startPos, pos)
                x = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number")
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // Functions
                while (ch >= 'a'.code && ch <= 'z'.code) {
                    nextChar()
                }
                val func = expr.substring(startPos, pos)
                if (eat('('.code)) {
                    val arg = parseExpression()
                    eat(')'.code)
                    x = when (func) {
                        "sin" -> if (useRadians) sin(arg) else sin(Math.toRadians(arg))
                        "cos" -> if (useRadians) cos(arg) else cos(Math.toRadians(arg))
                        "tan" -> if (useRadians) tan(arg) else tan(Math.toRadians(arg))
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        "sqrt" -> {
                            if (arg < 0) throw ArithmeticException("Domain error")
                            sqrt(arg)
                        }
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                } else {
                    throw IllegalArgumentException("Expected '(' after function name")
                }
            } else {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            }

            if (eat('^'.code)) {
                x = x.pow(parseFactor()) // Exponentiation
            }

            return x
        }
    }
}
