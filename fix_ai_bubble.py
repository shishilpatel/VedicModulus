import os

path = "app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt"
with open(path, "r") as f:
    content = f.read()

target_bubble = """@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 0.dp,
                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                ))
                .background(bgColor)
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor)
        }
    }
}"""

replacement_bubble = """@Composable
fun ChatBubble(message: ChatMessage) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Bottom) {
            if (!message.isUser) {
                IconButton(
                    onClick = {
                        android.widget.Toast.makeText(context, "Thank you. AI response reported for review.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.run { androidx.compose.material.icons.filled.Flag },
                        contentDescription = "Report AI message",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    ))
                    .background(bgColor)
                    .padding(12.dp)
            ) {
                Text(text = message.text, color = textColor)
            }
        }
    }
}"""

if target_bubble in content:
    content = content.replace(target_bubble, replacement_bubble)
    print("Bubble Replaced.")
else:
    print("Bubble not found!")

with open(path, "w") as f:
    f.write(content)
