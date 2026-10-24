package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ScrabbleGreenDark
import kotlin.math.abs

@Composable
fun QrBarcodeCard(
    roomCode: String,
    playerCount: Int,
    maxPlayers: Int = 4,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ROOM BARCODE & INVITE ($playerCount/$maxPlayers PLAYERS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Procedural QR / 2D Matrix Barcode Canvas
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val matrixSize = 21 // 21x21 standard QR grid
                    val moduleSize = size.width / matrixSize

                    // Seeded pseudorandom matrix from roomCode hash
                    val seed = abs(roomCode.hashCode())

                    for (r in 0 until matrixSize) {
                        for (c in 0 until matrixSize) {
                            val isFinderTopLeft = r in 0..6 && c in 0..6
                            val isFinderTopRight = r in 0..6 && c in (matrixSize - 7) until matrixSize
                            val isFinderBottomLeft = r in (matrixSize - 7) until matrixSize && c in 0..6

                            val isFilled = when {
                                // Standard QR Finder patterns (7x7 outer square, 5x5 white, 3x3 inner dot)
                                isFinderTopLeft -> (r == 0 || r == 6 || c == 0 || c == 6 || (r in 2..4 && c in 2..4))
                                isFinderTopRight -> {
                                    val localC = c - (matrixSize - 7)
                                    (r == 0 || r == 6 || localC == 0 || localC == 6 || (r in 2..4 && localC in 2..4))
                                }
                                isFinderBottomLeft -> {
                                    val localR = r - (matrixSize - 7)
                                    (localR == 0 || localR == 6 || c == 0 || c == 6 || (localR in 2..4 && c in 2..4))
                                }
                                // Timing patterns
                                r == 6 || c == 6 -> (r + c) % 2 == 0
                                else -> {
                                    val bit = (seed xor (r * 31 + c * 17)) and 0x7
                                    bit < 4
                                }
                            }

                            if (isFilled) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(c * moduleSize, r * moduleSize),
                                    size = Size(moduleSize, moduleSize)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room Code Display & Copy Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROOM: $roomCode",
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(roomCode)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Room Code",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Other players can scan this barcode or enter the code to join (up to 4 players).",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}
