import re

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "r") as f:
    content = f.read()

button_old = """                // Primary Unlock / Proceed Button
                Button(
                    onClick = {
                        if (isRewardEarned) {
                            onAdFinished()
                        }
                    },
                    enabled = isRewardEarned,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("proceed_to_app_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF64748B)
                    )
                ) {
                    if (isRewardEarned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Continue to Vedic Dashboard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Enter Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Waiting for Ad Completion... (${remainingSeconds}s)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }"""

button_new = """                // Primary Unlock / Proceed Button
                Button(
                    onClick = {
                        if (!hasUserOptedInToAd) {
                            hasUserOptedInToAd = true
                            adManager.startPlayback(scope)
                        } else if (isRewardEarned) {
                            onAdFinished()
                        }
                    },
                    enabled = !hasUserOptedInToAd || isRewardEarned,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("proceed_to_app_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF64748B)
                    )
                ) {
                    if (!hasUserOptedInToAd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Ad",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Ad to Unlock App",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isRewardEarned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Continue to Vedic Dashboard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Enter Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Waiting for Ad Completion... (${remainingSeconds}s)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }"""
                
content = content.replace(button_old, button_new)
content = content.replace("Icons.Default.ArrowForward", "Icons.AutoMirrored.Filled.ArrowForward")

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "w") as f:
    f.write(content)
