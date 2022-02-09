package com.zs.crossword.data

sealed class Keyboard{
    object Open : Keyboard()
    object Closed : Keyboard()
}
