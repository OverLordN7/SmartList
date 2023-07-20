package com.example.smartlist.ui.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class SwipeAction(
    val onSwipe: ()->Unit,
    val icon: @Composable ()->Unit,
    val background: Color,
    val weight: Double = 1.0,
    val isUndo: Boolean = false
) {

    init {
        // require() checks if all pre defined attributes in correct scope, and throw exception if not.
        require(weight >0.0) {"invalid weight $weight; must be greater than zero"}
    }

}