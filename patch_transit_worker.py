import sys

with open("app/src/main/java/com/example/workers/TransitNotificationWorker.kt", "r") as f:
    content = f.read()

content = content.replace("transit.targetRashi", 'transit.targetRashi') # no change needed here since targetRashi is String

# actually, it looks perfectly fine!
