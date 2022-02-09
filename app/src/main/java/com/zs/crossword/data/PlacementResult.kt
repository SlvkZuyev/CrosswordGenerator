package com.zs.crossword.data

sealed class PlacementResult{
    object Success: PlacementResult()
    object Error: PlacementResult()
}
