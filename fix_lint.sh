sed -i 's/private fun scheduleAlarm(/@android.annotation.SuppressLint("ScheduleExactAlarm")\n    private fun scheduleAlarm(/g' app/src/main/java/com/example/engines/alerts/VedicAlertEngine.kt
