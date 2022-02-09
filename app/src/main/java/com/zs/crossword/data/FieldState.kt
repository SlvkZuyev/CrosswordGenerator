package com.zs.crossword.data

sealed class FieldState {
    class FieldChanged(val hasKeyboard: Boolean, val grid: Grid) : FieldState()
    object CrosswordFinished : FieldState()
}