package com.zs.crossword.data

class Square(
    var letter: Char?,
    var coordinates: Coordinates,
    var isSelected: Boolean = false,
    var number: Int? = null,
    var isActive: Boolean = false
) {
    fun hasLetter(): Boolean = letter != null

}