with open("app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt", "r") as f:
    content = f.read()

btn_code = """
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* TODO: Open KalaChakra fullscreen or navigate */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View KalaChakra Visualization")
        }
    }
}
"""

content = content.replace("    }\n}\n\n@Composable\nprivate fun ScoreCircle", btn_code + "\n@Composable\nprivate fun ScoreCircle")

with open("app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt", "w") as f:
    f.write(content)
