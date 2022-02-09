package com.zs.crossword.domain

import android.util.Log
import androidx.compose.ui.input.key.Key
import  com.zs.crossword.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FieldEngine(private val grid: Grid) {

    private val selectedSquareCoordinates: Coordinates?
        get() {
            return grid.selectedSquareCoordinates
        }

    //todo rename it
    private var selectionWasMoved: MovementDirection = MovementDirection.UNKNOWN

    var keyboard: Keyboard = Keyboard.Closed
    private val hasKeyboard get() = keyboard is Keyboard.Open

    private val _fieldState: MutableStateFlow<FieldState> =
        MutableStateFlow(FieldState.FieldChanged(hasKeyboard, grid))
    val fieldState: StateFlow<FieldState> = _fieldState

    init {
        Log.d("FieldEngine", "engine created")
        grid.print()
    }

    fun onKeyboardStateChanged(isOpen: Boolean){
        Log.d("KeyboardState", "Changed to $isOpen")
        if(isOpen){
            onKeyboardOpened()
        } else {
            onKeyboardClosed()
        }

    }

    private fun onKeyboardOpened(){
        keyboard = Keyboard.Open

    }

    private fun onKeyboardClosed(){
        keyboard = Keyboard.Closed
        grid.clearSelection()
        notifyChanges()
    }

    fun onSquareClick(square: Square) {
        grid.moveSelectionTo(square.coordinates)
        keyboard = Keyboard.Open
        _fieldState.value = FieldState.FieldChanged(true, grid)
    }

    fun onClickNext(){
        moveSelectedSquare()
    }

    fun onLetterInserted(letter: Char) {
        grid.getSelectedSquare()?.letter = letter
        moveSelectedSquare()
        notifyChanges()
    }

    fun onBackPressed() {
        grid.clearSelection()
        keyboard = Keyboard.Closed
        notifyChanges()
    }

    private fun notifyChanges() {
        _fieldState.value = FieldState.FieldChanged(hasKeyboard, grid)
    }

    private fun moveSelectedSquare() {
        if (selectedSquareCoordinates == null) {
            return
        }

        selectionWasMoved = getSelectionMovementDirection()
        when (selectionWasMoved) {
            MovementDirection.HORIZONTALLY -> {
                Log.d("Selection Movement", "Moved Horizontally")
                grid.moveSelectionForward()
            }
            MovementDirection.VERTICALLY -> {
                Log.d("Selection Movement", "Moved VERTICALLY")
                grid.moveSelectionDown()
            }
            MovementDirection.MOVE_IS_IMPOSSIBLE -> {
                Log.d("Selection Movement", "Moved NO_POSSIBLE_DIRECTIONS")
                grid.clearSelection()
            }
        }
    }

    private fun selectionWasMovingBefore(): Boolean {
        return selectionWasMoved == MovementDirection.VERTICALLY ||
                selectionWasMoved == MovementDirection.HORIZONTALLY
    }

    private fun getSelectionMovementDirection(): MovementDirection {
        if (canKeepMovingAtSameDirection()) {
            Log.d("Selection move", "keeps moving at ${selectionWasMoved}")
            return selectionWasMoved
        }
        if(selectionWasMovingBefore()){
            return MovementDirection.MOVE_IS_IMPOSSIBLE
        }
        if (selectionSquareCanMoveDown()) {
            Log.d("Selection move", "now moves at ${MovementDirection.VERTICALLY}")
            return MovementDirection.VERTICALLY
        }
        if (selectionSquareCanMoveAhead()) {
            Log.d("Selection move", "now moves at ${MovementDirection.HORIZONTALLY}")
            return MovementDirection.HORIZONTALLY
        }
        return MovementDirection.MOVE_IS_IMPOSSIBLE
    }

    private fun canKeepMovingAtSameDirection(): Boolean {
        if (selectionWasMoved == MovementDirection.HORIZONTALLY) {
            return selectionSquareCanMoveAhead()
        }
        if (selectionWasMoved == MovementDirection.VERTICALLY) {
            return selectionSquareCanMoveDown()
        }
        return false
    }

    private fun selectionSquareCanMoveDown(): Boolean {
        if (selectedSquareCoordinates != null) {
            val bottomSquare = grid.get(
                x = selectedSquareCoordinates!!.x,
                y = selectedSquareCoordinates!!.y + 1
            )
            Log.d(
                "Selection",
                "for Bottom Square coords: ${bottomSquare?.coordinates} and active ${bottomSquare?.isActive} result: ${bottomSquare != null && bottomSquare.isActive}"
            )

            return bottomSquare != null && bottomSquare.isActive
        }
        return false
    }

    private fun selectionSquareCanMoveAhead(): Boolean {
        if (selectedSquareCoordinates != null) {
            val frontSquare = grid.get(
                x = selectedSquareCoordinates!!.x + 1,
                y = selectedSquareCoordinates!!.y
            )
            Log.d(
                "Selection",
                "for Ahead Square coords: ${frontSquare?.coordinates} and letter ${frontSquare?.letter} result: ${frontSquare != null && frontSquare.isActive}"
            )
            return frontSquare != null && frontSquare.isActive
        }
        return false
    }


}