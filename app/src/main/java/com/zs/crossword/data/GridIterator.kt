package com.zs.crossword.data

class GridIterator(var grid: Array<Array<Square>>): Iterator<Square> {
    var i = 0
    var j = 0
    var maxI = grid.size
    var maxJ = grid[0].size
    override fun hasNext(): Boolean {
        return !(i == maxI - 1 && j == maxJ - 1)
    }

    override fun next(): Square {
        j++
        if(j == maxJ){
            i++
            j = 0
        }
        return grid[i][j]
    }
}