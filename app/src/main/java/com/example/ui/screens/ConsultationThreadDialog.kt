package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.model.ConsultationMessage
import com.example.core.model.ConsultationQuery
import com.example.core.model.ConsultationStatus
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MaroonPrimary
import kotlinx.coroutines.delay

private val VedicDarkBackground = Color(0xFF160A0D)
private val VedicCardBackground = Color(0xFF231015)
private val GoldAccent = GoldPrimary

/**
 * Interactive 1-on-1 Consultation Thread Dialog featuring Scholar Voice Notes and Chat Thread.
 */
@Composable
fun ConsultationThreadDialog(
    query: ConsultationQuery,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var messageList by remember { mutableStateOf(query.messages) }
    var replyText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("consultation_thread_dialog"),
            colors = CardDefaults.cardColors(containerColor = VedicDarkBackground),
            border = BorderStroke(1.dp, MaroonPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1-on-1 Scholar Consultation",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(query.status.badgeColorHex).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(query.status.badgeColorHex)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = query.status.displayName,
                                    color = Color(query.status.badgeColorHex),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Assigned Scholar: ${query.assignedAstrologerName} • ₹${query.feeInr.toInt()}",
                            color = GoldAccent,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A151B))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VedicCardBackground),
                    border = BorderStroke(0.5.dp, Color(0xFF382026))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Topic: ${query.questionTopic}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = query.detailedQuestion,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages Chat Thread
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messageList) { msg ->
                        ConsultationMessageBubble(message = msg)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reply Composer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Ask follow-up question...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF332026),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                val newMsg = ConsultationMessage(
                                    senderName = query.seekerName,
                                    isAstrologer = false,
                                    textContent = replyText
                                )
                                messageList = messageList + newMsg
                                replyText = ""
                                Toast.makeText(context, "Follow-up question sent to scholar!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                            .testTag("send_consultation_reply_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultationMessageBubble(message: ConsultationMessage) {
    val isAstro = message.isAstrologer
    val align = if (isAstro) Alignment.Start else Alignment.End
    val bubbleColor = if (isAstro) Color(0xFF2A151B) else Color(0xFF0F172A)
    val borderColor = if (isAstro) MaroonPrimary else Color(0xFF334155)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Surface(
            color = bubbleColor,
            border = BorderStroke(1.dp, borderColor),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isAstro) 2.dp else 12.dp,
                bottomEnd = if (isAstro) 12.dp else 2.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAstro) "⭐ ${message.senderName}" else message.senderName,
                        color = if (isAstro) GoldPrimary else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message.getFormattedTime(),
                        color = Color(0xFF64748B),
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.textContent,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )

                // Render Scholar Audio Note Player if present
                message.audioNoteUrl?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    ScholarAudioNotePlayer(durationSeconds = message.audioDurationSeconds)
                }
            }
        }
    }
}

@Composable
private fun ScholarAudioNotePlayer(durationSeconds: Int) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentProgress < 1f) {
                delay(200)
                currentProgress += 0.05f
            }
            isPlaying = false
            currentProgress = 0f
        }
    }

    Surface(
        color = Color(0xFF1E0D12),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaroonPrimary)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Voice Note",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Scholar Voice Guidance",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "0:${(durationSeconds * currentProgress).toInt().toString().padStart(2, '0')} / 0:$durationSeconds",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = GoldPrimary,
                    trackColor = Color(0xFF332026)
                )
            }
        }
    }
}
