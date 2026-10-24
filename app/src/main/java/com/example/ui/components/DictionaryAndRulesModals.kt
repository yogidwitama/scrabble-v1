package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dictionary.CSW24Dictionary
import com.example.model.Tile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ScrabbleGreenDark
import com.example.ui.theme.TileBorder
import com.example.ui.theme.TileWood
import com.example.ui.theme.TripleWordRed

@Composable
fun DictionaryLookupModal(
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ScrabbleGreenDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CSW24 DICTIONARY LOOKUP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Black.copy(alpha = 0.25f),
                    contentColor = GoldAccent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Word Checker", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("2-Letter Words", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it.filter { c -> c.isLetter() }.take(15) },
                        label = { Text("Enter word to verify (CSW24)", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searchQuery.isNotEmpty()) {
                        val wordUpper = searchQuery.trim().uppercase()
                        val isValid = CSW24Dictionary.isValid(wordUpper)
                        val wordScore = wordUpper.sumOf { Tile.letterScore(it) }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isValid) Color(0xFF1B5E20) else TripleWordRed.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Clear,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = " \"$wordUpper\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "$wordScore PTS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = GoldAccent
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isValid) "VALID in Collins Scrabble Words 2024 (CSW24)" else "INVALID in CSW24 tournament rules",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )

                                if (isValid) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CSW24Dictionary.getDefinition(wordUpper),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Type any word above to check tournament legality, point values, and definitions.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else {
                    Text(
                        text = "All 107 Official CSW24 Two-Letter Words:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(CSW24Dictionary.TWO_LETTER_WORDS.toList().sorted()) { word ->
                            val score = word.sumOf { Tile.letterScore(it) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TileWood)
                                    .border(1.dp, TileBorder, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = word,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "$score pt",
                                        fontSize = 9.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RulesModal(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ScrabbleGreenDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OFFICIAL SCRABBLE RULES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GoldAccent
                )

                Text(
                    text = "Reference: playscrabble.com & CSW24",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        RuleSection(
                            title = "1. Game Objective",
                            content = "Score the most points by forming interconnected words crossword-style on the 15x15 board using letter tiles."
                        )
                    }
                    item {
                        RuleSection(
                            title = "2. First Play",
                            content = "The first player must place at least 2 tiles horizontally or vertically, and one tile MUST cover the center star (★) square. Center star doubles the word score!"
                        )
                    }
                    item {
                        RuleSection(
                            title = "3. Subsequent Plays",
                            content = "All plays must connect with existing tiles on the board in a single continuous line. Words read left-to-right or top-to-bottom. All newly created cross-words must also be valid."
                        )
                    }
                    item {
                        RuleSection(
                            title = "4. Premium Squares",
                            content = "• DL (Double Letter) & TL (Triple Letter): Multiply letter value.\n• DW (Double Word) & TW (Triple Word): Multiply entire word value.\nMultipliers apply only on the turn the tile is placed on the premium square."
                        )
                    }
                    item {
                        RuleSection(
                            title = "5. BINGO BONUS (+50 PTS)",
                            content = "Playing all 7 tiles from your rack in a single turn earns a massive +50 points bonus on top of your word score!"
                        )
                    }
                    item {
                        RuleSection(
                            title = "6. Blank Tiles",
                            content = "Two blank tiles exist with 0 points. You declare what letter it represents when playing. The letter remains that choice for the rest of the game."
                        )
                    }
                    item {
                        RuleSection(
                            title = "7. Multiplayer Challenges",
                            content = "When an opponent plays a word, players may Challenge or Skip. If challenged and word is in CSW24, play stands and challenger suffers penalty. If invalid, the word is recalled and player loses their turn."
                        )
                    }
                    item {
                        RuleSection(
                            title = "8. Exchanging & Passing",
                            content = "You may pass your turn or exchange 1 to 7 tiles from your rack if at least 7 tiles remain in the bag."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RuleSection(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 15.sp
            )
        }
    }
}
