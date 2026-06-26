package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat

enum class CalculatorTheme {
    PROFESSIONAL_POLISH,
    MIDNIGHT_VELVET,
    EARTHY_FOREST,
    CYBER_NEON,
    CLASSIC_M3
}

class CalculatorViewModel(private val repository: CalculationRepository) : ViewModel() {

    private val evaluator = CalculatorEvaluator()
    private val decimalFormat = DecimalFormat("#.##########")

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _useRadians = MutableStateFlow(false)
    val useRadians: StateFlow<Boolean> = _useRadians.asStateFlow()

    private val _currentTheme = MutableStateFlow(CalculatorTheme.PROFESSIONAL_POLISH)
    val currentTheme: StateFlow<CalculatorTheme> = _currentTheme.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    val historyList: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleScientific() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleRadians() {
        _useRadians.value = !_useRadians.value
        evaluateCurrent()
    }

    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
    }

    fun toggleHistory() {
        _showHistory.value = !_showHistory.value
    }

    fun selectTheme(theme: CalculatorTheme) {
        _currentTheme.value = theme
    }

    fun onKeyPress(key: String) {
        when (key) {
            "C" -> {
                _expression.value = ""
                _result.value = ""
            }
            "⌫" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    val endings = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
                    var deleted = false
                    for (ending in endings) {
                        if (current.endsWith(ending)) {
                            _expression.value = current.substring(0, current.length - ending.length)
                            deleted = true
                            break
                        }
                    }
                    if (!deleted) {
                        _expression.value = current.substring(0, current.length - 1)
                    }
                    evaluateCurrent()
                }
            }
            "=" -> {
                val expr = _expression.value
                if (expr.isNotBlank()) {
                    try {
                        val evalResult = evaluator.evaluate(expr, _useRadians.value)
                        val formatted = if (evalResult.isNaN() || evalResult.isInfinite()) {
                            "Error"
                        } else {
                            decimalFormat.format(evalResult)
                        }
                        _result.value = formatted
                        // Save to database
                        viewModelScope.launch {
                            repository.insert(expr, formatted)
                        }
                    } catch (e: Exception) {
                        _result.value = "Error"
                    }
                }
            }
            "±" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    val lastNumberIndex = current.indexOfLast { !it.isDigit() && it != '.' }
                    if (lastNumberIndex == -1) {
                        _expression.value = if (current.startsWith("-")) current.substring(1) else "-$current"
                    } else if (current[lastNumberIndex] == '-') {
                        if (lastNumberIndex == 0 || !current[lastNumberIndex - 1].isDigit()) {
                            _expression.value = current.substring(0, lastNumberIndex) + current.substring(lastNumberIndex + 1)
                        } else {
                            _expression.value = current + "-"
                        }
                    } else {
                        val lastChar = current[lastNumberIndex]
                        if (lastChar == '+' || lastChar == '×' || lastChar == '÷' || lastChar == '(') {
                            _expression.value = current.substring(0, lastNumberIndex + 1) + "-" + current.substring(lastNumberIndex + 1)
                        } else {
                            _expression.value = current + "-"
                        }
                    }
                    evaluateCurrent()
                } else {
                    _expression.value = "-"
                }
            }
            "( )" -> {
                val current = _expression.value
                val openCount = current.count { it == '(' }
                val closeCount = current.count { it == ')' }
                val lastChar = current.lastOrNull()

                if (current.isEmpty() || lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '(') {
                    _expression.value += "("
                } else if (openCount > closeCount) {
                    _expression.value += ")"
                } else {
                    _expression.value += "×("
                }
                evaluateCurrent()
            }
            "sin", "cos", "tan", "log", "ln", "sqrt" -> {
                val current = _expression.value
                val prefix = if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) "×" else ""
                _expression.value += "$prefix$key("
                evaluateCurrent()
            }
            "π", "e" -> {
                val current = _expression.value
                val prefix = if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) "×" else ""
                _expression.value += "$prefix$key"
                evaluateCurrent()
            }
            "+", "-", "×", "÷", "^", "%" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    val lastChar = current.last()
                    if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '^' || lastChar == '%') {
                        _expression.value = current.substring(0, current.length - 1) + key
                    } else {
                        _expression.value += key
                    }
                } else if (key == "-") {
                    _expression.value = "-"
                }
            }
            "." -> {
                val current = _expression.value
                val lastToken = current.split('+', '-', '×', '÷', '(', ')', '^').lastOrNull() ?: ""
                if (!lastToken.contains(".")) {
                    _expression.value += if (lastToken.isEmpty()) "0." else "."
                }
            }
            else -> {
                val current = _expression.value
                val lastChar = current.lastOrNull()
                val prefix = if (lastChar == 'π' || lastChar == 'e' || lastChar == ')') "×" else ""
                _expression.value += "$prefix$key"
                evaluateCurrent()
            }
        }
    }

    private fun evaluateCurrent() {
        val expr = _expression.value
        if (expr.isNotBlank()) {
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }
            var balancedExpr = expr
            if (openCount > closeCount) {
                balancedExpr += ")".repeat(openCount - closeCount)
            }
            try {
                if (balancedExpr == "-") return
                val evalResult = evaluator.evaluate(balancedExpr, _useRadians.value)
                if (!evalResult.isNaN() && !evalResult.isInfinite()) {
                    _result.value = decimalFormat.format(evalResult)
                }
            } catch (e: Exception) {
                // Ignore silent evaluation failures for preview
            }
        } else {
            _result.value = ""
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun selectHistoryItem(item: CalculationHistory) {
        _expression.value = item.expression
        _result.value = item.result
        _showHistory.value = false
    }
}

class CalculatorViewModelFactory(private val repository: CalculationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
