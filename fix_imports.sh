for file in app/src/main/java/com/example/workers/VedicTransitionSchedulerWorker.kt \
            app/src/main/java/com/example/ui/components/VedicCurrentDateCalendarComponent.kt \
            app/src/main/java/com/example/ui/screens/tabs/ChoghadiyaHoraTab.kt \
            app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt \
            app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt; do
    sed -i '/import com.example.engines.astronomy.MeeusAstronomicalEngine/a import com.example.engines.astronomy.CachedAstronomicalEngine' "$file"
done
