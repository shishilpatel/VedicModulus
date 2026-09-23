        // AI Watch Face Horoscope
        var aiHoroscope by remember { mutableStateOf<String?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CosmicGoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = CosmicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.ai_horoscope),
                        style = MaterialTheme.typography.titleSmall.copy(color = CosmicGoldPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = CosmicGoldPrimary, modifier = Modifier.size(16.dp))
                }

                if (aiHoroscope != null) {
                    Text(
                        text = aiHoroscope!!,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            val celestialData = "Lagna: ${personalizedProfile.natalLagna.sanskritName}, Moon: ${personalizedProfile.natalChandraRashi.sanskritName}, Nakshatra: ${personalizedProfile.natalNakshatra.sanskritName}, Current Choghadiya: ${dailyProfile.currentChoghadiya.type.displayName}, Day: ${dailyProfile.panchanga.vara}"
                            aiHoroscope = generateHoroscope(celestialData)
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicGoldPrimary, contentColor = CosmicOnGold),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CosmicOnGold, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.generating_horoscope), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.generate_ai_horoscope), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
