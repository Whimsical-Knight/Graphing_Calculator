package com.example.cs3318_finalproject.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cs3318_finalproject.engine.CalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalculatorViewModel : ViewModel() {

    // initiate my math engine (for parsing math expressions)
    private val engine = CalculatorEngine()

    // reactive state container (trigger to update the UI when the expression value changes)
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression

    // reactive state container that is triggered by "=" sign
    // basically updates the UI
    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result

    // when button is pressed, it is appended to the existing expression
    fun onInput(value: String) {
        _expression.value += value
    }

    // condition to clear expression when user enters "C"
    fun onClear() {
        _expression.value = ""
        _result.value = ""
    }

    // call the calculator engine when the user presses "=" sign
    // i.e., try to run the calculation
    fun onEquals() {
        _result.value = engine.evaluate(_expression.value)
    }
}