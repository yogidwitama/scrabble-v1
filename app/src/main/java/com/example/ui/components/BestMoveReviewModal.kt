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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.CandidateMove
import com.example.model.BestMoveReviewSettings
import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.TurnReviewRecord
import com.example.ui.theme.CenterStarGold
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.ScrabbleGreenDark
import com.example.ui.theme.ScrabbleGreenMid
import com.example.ui.theme.TripleWordRed

@Composable
fun BestMoveReviewModal(
    reviews: List<TurnReviewRecord>,
    currentHint: CandidateMove?,
    settings: BestMoveReviewSettings,
    isPostGame: Boolean = false,
    onUpdateSettings: (BestMoveReviewSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (isPostGame || reviews.isNotEmpty()) 0 else 1) }
    var selectedTurnIndex by remember { mutableIntStateOf(if (reviews.isNotEmpty()) reviews.size - 1 else 0) }
    var showBoardPreview by remember { mutableStateOf(false) }
    var viewBestMoveOnBoard by remember { mutableStateOf(false) }

    // Filter reviews based on checkboxes
    val filteredReviews = reviews.filter { review ->
        (review.isAi && settings.analyzeAiMoves) || (!review.isAi && settings.analyzePlayerMoves)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ScrabbleGreenDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BEST MOVE REVIEW & ANALISIS",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = GoldAccent
                            )
                            Text(
                                text = if (isPostGame) "Evaluasi Move Pasca Permainan" else "Review & Evaluasi Move vs AI",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Review Log / Best Move Hint / Settings
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    contentColor = GoldAccent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Log Review (${filteredReviews.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    if (!isPostGame && settings.reviewInGame) {
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Best Move Giliran Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Pengaturan Ceklis", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    // TAB 0: MOVE REVIEW LOG
                    0 -> {
                        if (filteredReviews.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada catatan move yang sesuai dengan filter ceklis.\nMainkan beberapa giliran atau periksa pengaturan ceklis box.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(filteredReviews) { index, rev ->
                                    val isOptimal = rev.isOptimal
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isOptimal) Color(0xFF1B402B) else Color.White.copy(alpha = 0.07f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isOptimal) GoldAccent.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (rev.isAi) Icons.Default.SmartToy else Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = if (rev.isAi) GoldAccentLight else Color(0xFF00E676),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Giliran ${rev.turnNumber} • ${rev.playerName}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isOptimal) GoldAccent else TripleWordRed)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isOptimal) "★ OPTIMAL" else "-${rev.pointsDiff} PTS",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isOptimal) Color.Black else Color.White
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Comparison row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // Actual Move
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("DIPASANG:", fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "\"${rev.playedWord}\"",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Color.White
                                                    )
                                                    Text("${rev.playedPoints} Poin${if (rev.playedBingo) " (Bingo!)" else ""}", fontSize = 11.sp, color = GoldAccent)
                                                }

                                                // Best Move
                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                                    Text("BEST MOVE TEORETIS:", fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "\"${rev.bestWord ?: "-"}\"",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = if (isOptimal) GoldAccent else GoldAccentLight
                                                    )
                                                    Text("${rev.bestPoints ?: 0} Poin${if (rev.bestBingo) " (Bingo!)" else ""}", fontSize = 11.sp, color = GoldAccentLight)
                                                }
                                            }

                                            if (!isOptimal && rev.bestWord != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "💡 Best Move bisa menghasilkan ${rev.bestPoints} poin (${rev.pointsDiff} poin lebih banyak) dengan kata \"${rev.bestWord}\".",
                                                    fontSize = 10.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    lineHeight = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: CURRENT TURN BEST MOVE HINT
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (currentHint != null) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                                        .border(2.dp, GoldAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "BEST MOVE UNTUK GILIRAN INI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )

                                Text(
                                    text = "\"${currentHint.primaryWord}\"",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )

                                Text(
                                    text = "${currentHint.score} POIN${if (currentHint.isBingo) " (BINGO! +50 PTS BONUS)" else ""}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccentLight
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Posisi Ubin: Baris ${currentHint.placedTiles.firstOrNull()?.row?.plus(1)}, Kolom ${currentHint.placedTiles.firstOrNull()?.col?.plus(1)}",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Semua Kata Terbentuk: ${currentHint.allWords.joinToString(", ")}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Tidak ditemukan langkah kata yang valid untuk rak ubin saat ini (pertimbangkan tukar ubin atau pass).",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // TAB 2: PENGATURAN CEKLIS BOX
                    2 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "Pengaturan Ceklis Box (Pilih salah satu atau keduanya):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // 1. Ceklis: Saat Bermain
                            item {
                                SettingCheckboxItem(
                                    title = "Tampilkan Best Move Saat Bermain",
                                    subtitle = "Player dapat mereview saran best move secara live selama pertandingan berjalan",
                                    checked = settings.reviewInGame,
                                    onCheckedChange = { onUpdateSettings(settings.copy(reviewInGame = it)) }
                                )
                            }

                            // 2. Ceklis: Setelah Bermain
                            item {
                                SettingCheckboxItem(
                                    title = "Review Best Move Setelah Bermain",
                                    subtitle = "Tampilkan analisis lengkap best move di layar hasil akhir Game Over",
                                    checked = settings.reviewPostGame,
                                    onCheckedChange = { onUpdateSettings(settings.copy(reviewPostGame = it)) }
                                )
                            }

                            // 3. Ceklis: Catatan Move Pemain
                            item {
                                SettingCheckboxItem(
                                    title = "Catat & Analisis Move Pemain (Human)",
                                    subtitle = "Mencatat setiap kata yang dipasang pemain dan membandingkannya dengan move teoretis terbaik",
                                    checked = settings.analyzePlayerMoves,
                                    onCheckedChange = { onUpdateSettings(settings.copy(analyzePlayerMoves = it)) }
                                )
                            }

                            // 4. Ceklis: Catatan Move AI
                            item {
                                SettingCheckboxItem(
                                    title = "Catat & Analisis Move AI",
                                    subtitle = "Mencatat kata yang dimainkan AI dan menghitung akurasi langkahnya",
                                    checked = settings.analyzeAiMoves,
                                    onCheckedChange = { onUpdateSettings(settings.copy(analyzeAiMoves = it)) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingCheckboxItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) GoldAccent else Color.White.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = GoldAccent,
                    checkmarkColor = Color.Black,
                    uncheckedColor = Color.White.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(text = subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), lineHeight = 14.sp)
            }
        }
    }
}
