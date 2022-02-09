package com.zs.crossword.data

import android.util.Log
import java.util.*

class Grid(val rows: Int, val cols: Int) : Iterable<Square> {
    private var grid = generate2dArrayOfEmptySquares(cols = cols, rows = rows)
    var selectedSquareCoordinates: Coordinates? = null

    val hasSelection: Boolean
        get() {
            return selectedSquareCoordinates != null
        }

    fun print() {
        for (y in 0 until rows) {
            print("$y\t")
            for (x in 0 until cols) {
                if (get(x = x, y = y)?.letter == null) {
                    print("[ ]")
                } else {
                    print("[${get(x = x, y = y)?.letter}]".uppercase(Locale.getDefault()))
                }
            }
            print("\n")
        }
    }

    fun getSelectedSquare(): Square?{
        if(selectedSquareCoordinates != null){
            return get(selectedSquareCoordinates!!)
        }
        return null
    }

    fun moveSelectionTo(coordinates: Coordinates) {
        if(hasSelection){
            clearSelection()
        }
        get(coordinates)?.isSelected = true
        selectedSquareCoordinates = coordinates
    }

    fun moveSelectionForward(): Boolean {
        if(!hasSelection){
            return false
        }
        val frontSquare = get(x = selectedSquareCoordinates!!.x + 1, y = selectedSquareCoordinates!!.y)
        if(frontSquare == null || !frontSquare.isActive){
            return false
        }
        moveSelectionTo(frontSquare.coordinates)
        return true
    }

    fun moveSelectionDown(): Boolean {
        if(!hasSelection){
            return false
        }
        val bottomSquare = get(x = selectedSquareCoordinates!!.x, y = selectedSquareCoordinates!!.y + 1)
        if(bottomSquare == null || !bottomSquare.isActive){
            return false
        }
        moveSelectionTo(bottomSquare.coordinates)
        return true
    }

    fun clearSelection() {
        if(hasSelection){
          get(selectedSquareCoordinates!!)?.isSelected = false
        }
        selectedSquareCoordinates = null
    }

    private fun generate2dArrayOfEmptySquares(cols: Int, rows: Int): Array<Array<Square>> {
        Log.d("Gridf", "Generating array rows = $rows, cols = $cols")
        var result: Array<Array<Square>> = arrayOf()
        for (x in 0 until cols) {
            var line: Array<Square> = arrayOf()
            for (y in 0 until rows) {
                line += Square(null, coordinates = Coordinates(x = x, y = y))
            }
            result += line
        }

        for (x in 0 until cols) {
            for (y in 0 until rows) {
                result[x][y].coordinates = Coordinates(x = x, y = y)
            }
        }
        return result
    }

    override fun iterator(): Iterator<Square> {
        return GridIterator(grid)
    }

    fun get(x: Int, y: Int): Square? {
        if (x in 0 until cols && y in 0 until rows) {
            return grid[x][y]
        }
        return null
    }

    fun get(coordinates: Coordinates): Square? {
        if (coordinates.x in 0..cols && coordinates.y in 0..rows) {
            return grid[coordinates.x][coordinates.y]
        } else {
            return null
        }
    }

    fun getOptimizedGrid(): Grid {
        val topLeftCornerCoordinate =
            Coordinates(x = getCoordinateOfLeftest().x, y = getCoordinateOfHighestLetter().y)
        val bottomRightCornerCoordinate =
            Coordinates(x = getCoordinateOfRightest().x, y = getCoordinateOfLowestLetter().y)

        Log.d(
            "Grid",
            "Optimized grid top left : $topLeftCornerCoordinate  bottomRight $bottomRightCornerCoordinate"
        )

        val optimizedGrid = Grid(
            rows = bottomRightCornerCoordinate.y - topLeftCornerCoordinate.y + 1,
            cols = bottomRightCornerCoordinate.x - topLeftCornerCoordinate.x + 1
        )

        for (y in topLeftCornerCoordinate.y..bottomRightCornerCoordinate.y) {
            for (x in topLeftCornerCoordinate.x..bottomRightCornerCoordinate.x) {
                optimizedGrid.get(
                    x = x - topLeftCornerCoordinate.x,
                    y = y - topLeftCornerCoordinate.y
                )?.apply{
                    letter = grid[x][y].letter
                    isActive = grid[x][y].isActive
                    number = grid[x][y].number
                }
            }
        }

        return optimizedGrid
    }


    //Todo Optimize it! 1600 iterations is not ok!
    private fun getCoordinateOfHighestLetter(): Coordinates {
        for (y in grid.first().indices) {
            for (x in grid.indices) {
                if (grid[x][y].hasLetter()) {
                    return Coordinates(x = x, y = y)
                }
            }
        }
        return Coordinates(x = 0, y = 0)
    }

    private fun getCoordinateOfLowestLetter(): Coordinates {
        for (y in grid.first().size - 1 downTo 0) {
            for (x in grid.indices) {
                if (grid[x][y].hasLetter()) {
                    return Coordinates(x = x, y = y)
                }
            }
        }
        return Coordinates(x = 0, y = 0)
    }

    private fun getCoordinateOfLeftest(): Coordinates {
        for (x in grid.indices) {
            for (y in grid.first().indices) {
                if (grid[x][y].hasLetter()) {
                    return Coordinates(x = x, y = y)
                }
            }
        }
        return Coordinates(x = 0, y = 0)
    }

    private fun getCoordinateOfRightest(): Coordinates {
        for (x in grid.size - 1 downTo 0) {
            for (y in grid.first().indices) {
                if (grid[x][y].hasLetter()) {
                    return Coordinates(x = x, y = y)
                }
            }
        }
        return Coordinates(x = -1, y = -1)
    }

}