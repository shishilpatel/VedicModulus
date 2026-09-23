with open('app/src/main/java/com/example/data/room/VedicDatabase.kt', 'r') as f:
    text = f.read()

text = text.replace(
    "@Database(entities = [UserProfileEntity::class, MeditationSessionEntity::class], version = 2, exportSchema = false)",
    "@Database(entities = [UserProfileEntity::class, MeditationSessionEntity::class, PredictionCacheEntity::class], version = 3, exportSchema = false)"
)

if 'abstract fun predictionCacheDao(): PredictionCacheDao' not in text:
    text = text.replace(
        "abstract fun meditationSessionDao(): MeditationSessionDao",
        "abstract fun meditationSessionDao(): MeditationSessionDao\n    abstract fun predictionCacheDao(): PredictionCacheDao"
    )

with open('app/src/main/java/com/example/data/room/VedicDatabase.kt', 'w') as f:
    f.write(text)
