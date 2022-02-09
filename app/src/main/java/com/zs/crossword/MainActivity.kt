package com.zs.crossword

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zs.crossword.data.CrosswordField
import com.zs.crossword.data.CrosswordWord
import com.zs.crossword.data.WordOrientation
import com.zs.crossword.presentation.composables.Field

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val field = CrosswordField()

        field.addWord("Gomogay")
        field.addWord("Morgen")
        field.addWord("Africa")
        field.addWord("Gomontron")
        field.addWord("Knopochka")

        //field.print()
        //openKeyboard()
        setContent {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                MainScreen(field = field)
            }
        }
    }


    private fun openKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(field: CrosswordField) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        sheetContent = {
            WordsBottomSheetContent(
                words = listOf(
                    CrosswordWord(
                        text = "First",
                        number = 1,
                        isSelected = false,
                        orientation = WordOrientation.Horizontal
                    ),
                    CrosswordWord(
                        text = "Second",
                        number = 2,
                        isSelected = false,
                        orientation = WordOrientation.Horizontal
                    ),
                    CrosswordWord(
                        text = "Third",
                        number = 3,
                        isSelected = false,
                        orientation = WordOrientation.Vertical
                    ),
                    CrosswordWord(
                        text = "fourth",
                        number = 4,
                        isSelected = false,
                        orientation = WordOrientation.Vertical
                    ),
                    CrosswordWord(
                        text = "Fifth",
                        number = 5,
                        isSelected = false,
                        orientation = WordOrientation.Horizontal
                    )
                )
            )
        },
        scaffoldState = bottomSheetScaffoldState
    ) {


        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Field(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp),
                field = field
            )
        }
    }
}


@Composable
fun WordsBottomSheetContent(words: List<CrosswordWord>) {
    var selectedWordIndex by remember { mutableStateOf(0) }

    var selectedWord by remember {
        mutableStateOf(words[selectedWordIndex])
    }

    val selectNextWord = {
        selectedWordIndex++
        selectedWordIndex %= words.size
        selectedWord = words[selectedWordIndex]
        Unit
    }

    val selectPreviousWord = {
        selectedWordIndex--
        if (selectedWordIndex < 0) {
            selectedWordIndex = words.lastIndex
        }
        selectedWord = words[selectedWordIndex]
        Unit
    }


    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f)
    ) {
        Column(Modifier.fillMaxSize()) {

            BottomSheetTopPart(
                word = selectedWord.text,
                onClickNext = selectNextWord,
                onClickPrevious = selectPreviousWord
            )

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {

                WordsList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 8.dp),
                    header = "Vertical",
                    words = words.filter { it.orientation == WordOrientation.Vertical },
                    selectedWordNumber = selectedWord.number,
                    onClickWord = { selectedWord = it }
                )

                WordsList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp),
                    header = "Horizontal",
                    words = words.filter { it.orientation == WordOrientation.Horizontal },
                    selectedWordNumber = selectedWord.number,
                    onClickWord = { selectedWord = it }
                )
            }

        }
    }
}

@Composable
fun BottomSheetTopPart(word: String, onClickNext: () -> Unit, onClickPrevious: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically

    ) {
        IconButton(onClick = onClickPrevious, modifier = Modifier.weight(1f)) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
        Text(
            text = word,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onClickNext, modifier = Modifier.weight(1f)) {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

    }
}

@Composable
fun WordsList(
    modifier: Modifier = Modifier,
    words: List<CrosswordWord>,
    header: String,
    selectedWordNumber: Int,
    onClickWord: (word: CrosswordWord) -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(text = header, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        for (word in words) {
            Text(
                text = "${word.number}. ${word.text}",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth()
                    .clickable {
                        onClickWord(word)
                    },
                style = TextStyle(textDecoration = if (selectedWordNumber == word.number) TextDecoration.Underline else TextDecoration.None),
                textAlign = TextAlign.Start
            )
        }
    }
}
