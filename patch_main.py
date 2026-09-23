with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

target = """                MyApplicationTheme(themePalette = currentTheme) {
                    var isEditingOnboarding by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()
                    
                    Crossfade("""

replacement = """                MyApplicationTheme(themePalette = currentTheme) {
                    var isEditingOnboarding by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()
                    var hasShownAdSession by remember { mutableStateOf(false) }
                    
                    if (!hasShownAdSession) {
                        com.example.ui.screens.CustomAdScreen(
                            onAdFinished = { hasShownAdSession = true }
                        )
                    } else {
                        Crossfade("""

if target in text:
    text = text.replace(target, replacement)
    
    # Need to also close the else bracket right before the end of the CompositionLocalProvider block
    target2 = """                        }
                    }
                }
            }
        }
    }"""
    
    replacement2 = """                        }
                    }
                    } // close else block
                }
            }
        }
    }"""
    text = text.replace(target2, replacement2)
    
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(text)
    print("Patched successfully")
else:
    print("Target not found")
