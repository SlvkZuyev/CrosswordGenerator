package com.zs.crossword.presentation.composables

import android.graphics.Rect
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zs.crossword.data.*
import com.zs.crossword.domain.FieldEngine
import java.util.*
import com.zs.crossword.data.Keyboard

@ExperimentalComposeUiApi
@Composable
fun Field(modifier: Modifier, field: CrosswordField) {
    Log.d("Field Composable", "Redraw!")
    val keyboardState by keyboardAsState()

    val optimizedGrid: Grid by remember {
        mutableStateOf(field.grid.getOptimizedGrid())
    }

    val engine: FieldEngine by remember {
        mutableStateOf(FieldEngine(optimizedGrid))
    }

    LaunchedEffect(key1 = keyboardState){
        when (keyboardState) {
            Keyboard.Closed -> {
                Log.d("SlvkLog", "KB Closed")
                engine.onKeyboardStateChanged(isOpen = false)
            }
            Keyboard.Open -> {
                engine.onKeyboardStateChanged(isOpen = true)
            }
        }
    }



    BackHandler {
        engine.onBackPressed()
    }

    val focusRequester = FocusRequester()
    val state: FieldState by engine.fieldState.collectAsState()


    val mState = state
    when (mState) {
        FieldState.CrosswordFinished -> {

        }
        is FieldState.FieldChanged -> {
            val hasKeyBoard = mState.hasKeyboard
            LaunchedEffect(hasKeyBoard, engine.keyboard) {
                if (hasKeyBoard) {
                    focusRequester.requestFocus()
                }
            }

            if (mState.hasKeyboard) {

                TextField(
                    value = "",
                    onValueChange = {
                        engine.onLetterInserted(it.last())
                    },
                    modifier = Modifier
                        .border(width = 2.dp, color = Color.Green)
                        .width(0.dp)
                        .height(0.dp)
                        .focusRequester(focusRequester)
                        .alpha(0f)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Back) {
                                engine.onBackPressed()
                            }
                            true
                        },
                    keyboardActions = KeyboardActions(
                        onNext = { engine.onClickNext() }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            val grid = mState.grid
            Box(modifier = modifier.border(width = 2.dp, color = Color.Green)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    for (y in 0 until grid.rows) {
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            for (x in 0 until grid.cols) {
                                Square(
                                    modifier = Modifier.weight(1f),
                                    square = grid.get(x = x, y = y)!!,
                                    onClick = { engine.onSquareClick(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState: MutableState<Keyboard> = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current

    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Open
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}


@Composable
fun Square(modifier: Modifier, square: Square, onClick: (Square) -> Unit = {}) {
    val letter = square.letter?.toString()?.uppercase(Locale.getDefault()) ?: ""
    val border: BorderStroke =
        if (letter == "") BorderStroke(width = 0.dp, color = Color.Transparent) else BorderStroke(
            width = 2.dp,
            Color.LightGray
        )


    Box(
        modifier = modifier
            .padding(all = 1.dp)
            .aspectRatio(1f)
            .border(border = border)
            .background(if (square.isSelected) Color(0xFFDFDFDF) else Color(0xFFFFFFFF))
            .clickable {
                if (square.isActive) {
                    onClick(square)
                }
            },
        contentAlignment = Alignment.TopStart

    ) {
        if (square.number != null) {
            Text(
                textAlign = TextAlign.Center,
                text = square.number.toString(),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 1.dp, start = 3.dp)
            )
        }
        Text(
            textAlign = TextAlign.Center,
            text = letter,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
        )
    }
}

