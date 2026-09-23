sed -i '/    var currentTime by remember { mutableStateOf(LocalTime.now()) }/d' app/src/main/java/com/example/ui/screens/tabs/WatchFaceTab.kt
sed -i '/    LaunchedEffect(Unit) {/,/    }/d' app/src/main/java/com/example/ui/screens/tabs/WatchFaceTab.kt
sed -i 's/    isAmbientMode: Boolean,/    currentTime: LocalTime,\n    isAmbientMode: Boolean,/g' app/src/main/java/com/example/ui/screens/tabs/WatchFaceTab.kt
