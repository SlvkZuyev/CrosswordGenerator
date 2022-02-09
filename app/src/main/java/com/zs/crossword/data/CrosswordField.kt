package com.zs.crossword.data

import android.util.Log
import java.util.*

//Todo add randomisation
class CrosswordField(val rows: Int = 20, val cols: Int = 20) {
    var grid = Grid(cols = cols, rows = rows)
    private var isEmpty: Boolean = true
    private var wordsCounter = 0

    fun print() {
        for (y in 0 until grid.rows) {
            print("$y\t")
            for (x in 0 until grid.cols) {
                if (grid.get(x = x, y = y)?.letter == null) {
                    print("[ ]")
                } else {
                    print("[${grid.get(x = x, y = y)?.letter}]".uppercase(Locale.getDefault()))
                }
            }
            print("\n")
        }
    }

    fun addWord(word: String): PlacementResult {
        if (gridIsEmpty()) {
            placeCenter(word)
            isEmpty = false
            return PlacementResult.Success
        } else {
            val intersectionCoordinates = getIntersectionCoordinates(word)
            Log.d(
                "Placement info",
                "Intersectiuon coordinates for word $word: $intersectionCoordinates"
            )
            for (coordinates in intersectionCoordinates) {
                val result = tryToPlaceWithIntersection(
                    word = word,
                    intersectionCoordinates = coordinates
                )
                Log.d(
                    "Placement info",
                    "placement result for word: $word on coordinates $coordinates is $result"
                )
                if (result is PlacementResult.Success) {
                    return result
                }
            }
            return PlacementResult.Error
        }
    }

    private fun getIntersectionCoordinates(word: String): List<Coordinates> {
        val result: MutableList<Coordinates> = mutableListOf()
        for (square in grid) {
            if (square.letter != null && word.contains(square.letter!!)) {
                result.add(square.coordinates)
            }
        }
        return result
    }


    private fun tryToPlaceWithIntersection(
        word: String,
        intersectionCoordinates: Coordinates
    ): PlacementResult {
        val intersectionLetter = grid.get(intersectionCoordinates)?.letter ?: ' '
        val distancesToLetter =
            getDistancesToLetter(word = word, letter = intersectionLetter)

        for (distance in distancesToLetter) {
            if (!hasLetterBeforeOrAfter(intersectionCoordinates)) {
                return tryToPlaceWordHorizontally(
                    word,
                    Coordinates(
                        y = intersectionCoordinates.y,
                        x = intersectionCoordinates.x - distance
                    )
                )
            } else if (!hasLetterAboveOrBelow(intersectionCoordinates)) {
                return tryToPlaceWordVertically(
                    word,
                    Coordinates(
                        y = intersectionCoordinates.y - distance,
                        x = intersectionCoordinates.x
                    )
                )
            }
        }

        return PlacementResult.Error
    }

    private fun coordinatesAreValid(coordinates: Coordinates): Boolean {
        val x = coordinates.x
        val y = coordinates.y
        return x >= 0 && y >= 0 && x < grid.cols && y < grid.rows
    }

    private fun tryToPlaceWordHorizontally(
        word: String,
        startCoordinates: Coordinates
    ): PlacementResult {
        if (!coordinatesAreValid(startCoordinates)) {
            return PlacementResult.Error
        }

        if (canBePlacedHorizontally(word = word, startCoordinates = startCoordinates)) {
            placeHorizontally(word = word, startFrom = startCoordinates)
            return PlacementResult.Success
        }
        return PlacementResult.Error

    }

    private fun tryToPlaceWordVertically(
        word: String,
        startCoordinates: Coordinates
    ): PlacementResult {
        var result: PlacementResult = PlacementResult.Success

        Log.d(
            "Placement info",
            "Trying to place word $word with start position x = ${startCoordinates.x} y = ${startCoordinates.y}"
        )
        if (!coordinatesAreValid(startCoordinates)) {
            return PlacementResult.Error
        }

        if (canBePlacedVertically(word = word, startCoordinates = startCoordinates)) {
            placeVertically(word = word, startFrom = startCoordinates)
            return PlacementResult.Success
        }
        return PlacementResult.Error

    }

    private fun canBePlacedVertically(word: String, startCoordinates: Coordinates): Boolean {
        val x = startCoordinates.x
        for (y in startCoordinates.y until (startCoordinates.y + word.length)) {
            val cell = grid.get(x = x, y = y)

            if (cell == null) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed vertically because of cell is null on position x = $x y = $y"
                )
                return false
            } else if (cell.hasLetter() && cell.letter?.lowercaseChar() != word[y - startCoordinates.y].lowercaseChar()) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed vertically because cell has wrong letter on position x = $x y = $y, required ${word[x - startCoordinates.x]} found  ${cell.letter}"
                )
                return false
            } else if (!cell.hasLetter() && hasLetterBeforeOrAfter(cell.coordinates)) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed vertically because cell has word before or after on position x = $x y = $y"
                )
                return false
            }

        }
        return true
    }


    private fun canBePlacedHorizontally(word: String, startCoordinates: Coordinates): Boolean {
        val y = startCoordinates.y
        for (x in startCoordinates.x until (startCoordinates.x + word.length)) {
            val cell = grid.get(x = x, y = y)
            if (cell == null) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed horizontally because of cell is null on position x = $x y = $y"
                )
                return false
            } else if (cell.hasLetter() && cell.letter?.lowercaseChar() != word[x - startCoordinates.x].lowercaseChar()) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed horizontally because cell has wrong letter on position x = $x y = $y "
                )
                return false
            } else if (!cell.hasLetter() && hasLetterAboveOrBelow(cell.coordinates)) {
                Log.d(
                    "Placement Error",
                    "Word $word can't be placed horizontally because cell has word above or below on position x = $x y = $y"
                )
                return false
            }
        }
        return true
    }


    private fun hasLetterAboveOrBelow(coordinate: Coordinates): Boolean {

        val topSquareCoordinate = Coordinates(x = coordinate.x, y = coordinate.y - 1)
        val bottomSquareCoordinate = Coordinates(x = coordinate.x, y = coordinate.y + 1)
        Log.d(
            "Placement info",
            "for pos $coordinate letter above is ${grid.get(topSquareCoordinate)} below is ${
                grid.get(bottomSquareCoordinate)
            }"
        )

        return grid.get(topSquareCoordinate)?.hasLetter() ?: false
                || grid.get(bottomSquareCoordinate)?.hasLetter() ?: false
    }

    private fun hasLetterBeforeOrAfter(coordinate: Coordinates): Boolean {

        val beforeSquareCoordinate = Coordinates(x = coordinate.x - 1, y = coordinate.y)
        val afterSquareCoordinate = Coordinates(x = coordinate.x + 1, y = coordinate.y)

        Log.d(
            "Placement info",
            "for pos $coordinate letter before is ${grid.get(beforeSquareCoordinate)} after is ${
                grid.get(afterSquareCoordinate)
            }"
        )
        return grid.get(beforeSquareCoordinate)?.hasLetter() ?: false
                || grid.get(afterSquareCoordinate)?.hasLetter() ?: false
    }

    private fun getDistancesToLetter(word: String, letter: Char): List<Int> {
        var i = 0
        val distances = mutableListOf<Int>()
        for (ch in word) {
            if (ch == letter) {
                distances.add(i)
            }
            i++
        }
        return distances
    }

    private fun placeCenter(word: String) {
        val isPlacedHorizontally = false
        //Random().nextBoolean()
        if (isPlacedHorizontally) {
            val y = rows / 2
            val x = (cols - word.length) / 2
            placeHorizontally(word, Coordinates(x = x, y = y))
        } else {
            val y = (rows - word.length) / 2
            val x = cols / 2
            placeVertically(word, Coordinates(x = x, y = y))
        }
    }

    private fun placeHorizontally(word: String, startFrom: Coordinates) {
        for (i in startFrom.x until (startFrom.x + word.length)) {
            grid.get(x = i, y = startFrom.y)?.letter = word[i - startFrom.x]
            grid.get(x = i, y = startFrom.y)?.isActive = true
        }
        wordsCounter++
        grid.get(x = startFrom.x, y = startFrom.y)?.number = wordsCounter
    }

    private fun placeVertically(word: String, startFrom: Coordinates) {
        for (y in startFrom.y until (startFrom.y + word.length)) {
            grid.get(x = startFrom.x, y = y)?.letter = word[y - startFrom.y]
            grid.get(x = startFrom.x, y = y)?.isActive = true
        }
        wordsCounter++
        grid.get(x = startFrom.x, y = startFrom.y)?.number = wordsCounter
    }

    fun clear() {
        isEmpty = true
    }

    private fun gridIsEmpty(): Boolean {
        for (square in grid) {
            if (square.letter != null) {
                return false
            }
        }
        return true
    }

    fun getCenterCoordinates(): Coordinates {
        return Coordinates(x = 5, y = 5)
    }
}