import re

with open("wear/src/main/java/com/example/wear/WatchFaceModel.kt", "r") as f:
    content = f.read()

# Update generateDialModel to use the numerology color mapper and VastuGeometryGenerator
target_old = """    fun generateDialModel(
        style: WatchFaceStyle,
        syncData: SyncData
    ): ProceduralWatchFaceModel {
        val pColor = syncData.primaryColorHex
        val aColor = syncData.accentColorHex
        val isRahuActive = syncData.isRahuActive"""

target_new = """    fun generateDialModel(
        style: WatchFaceStyle,
        syncData: SyncData
    ): ProceduralWatchFaceModel {
        // Apply Numerology Auspicious Color Palette
        val palette = NumerologyColorMapper.getPaletteForLifePathNumber(syncData.destinyNumber)
        val pColor = palette.primaryColorHex
        val aColor = palette.accentColorHex
        val isRahuActive = syncData.isRahuActive"""

content = content.replace(target_old, target_new)

vastu_old = """            WatchFaceStyle.VASTU -> {
                // Personalize Vastu Mandala based on Birth Number (1-9)
                val birthNum = if (syncData.birthNumber in 1..9) syncData.birthNumber else 8
                
                elements.add(VectorDialElement.CircleRing(0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.CircleRing(0.92f, aColor, 1.0f))
                
                // Vastu typically uses 8 or 32 directions (pada)
                val padaCount = if (birthNum % 2 == 0) 32 else 24
                elements.add(VectorDialElement.TickMarks(8, 0.88f, 0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.TickMarks(padaCount, 0.90f, 0.94f, 0x77FFD166, 1.0f))
                
                // Outer Vastu Square Mandala (rotated dynamically based on Lagna element logic, here simplified to birthNum)
                val rotationAngle = (birthNum * 10f) * (Math.PI / 180.0)
                val r = 0.75f
                val square = listOf(
                    Pair((r * cos(rotationAngle - Math.PI/2)).toFloat(), (r * sin(rotationAngle - Math.PI/2)).toFloat()),
                    Pair((r * cos(rotationAngle)).toFloat(), (r * sin(rotationAngle)).toFloat()),
                    Pair((r * cos(rotationAngle + Math.PI/2)).toFloat(), (r * sin(rotationAngle + Math.PI/2)).toFloat()),
                    Pair((r * cos(rotationAngle + Math.PI)).toFloat(), (r * sin(rotationAngle + Math.PI)).toFloat())
                )
                elements.add(VectorDialElement.PolygonShape(square, pColor, 2.0f))
                
                val innerR = 0.53f
                val innerSquare = listOf(
                    Pair(-innerR, -innerR),
                    Pair(innerR, -innerR),
                    Pair(innerR, innerR),
                    Pair(-innerR, innerR)
                )
                elements.add(VectorDialElement.PolygonShape(innerSquare, aColor, 1.5f))
                
                // Brahmasthan grid (internal 3x3)
                val gridR = 0.3f
                val bSquare = listOf(Pair(-gridR, -gridR), Pair(gridR, -gridR), Pair(gridR, gridR), Pair(-gridR, gridR))
                elements.add(VectorDialElement.PolygonShape(bSquare, pColor, 1.0f, isDashed = true))
                
                // Directions
                elements.add(VectorDialElement.GlyphMarker(0f, 0.82f, "N", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.82f, "E", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(180f, 0.82f, "S", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.82f, "W", pColor, 12f))
            }"""

vastu_new = """            WatchFaceStyle.VASTU -> {
                val birthNum = if (syncData.birthNumber in 1..9) syncData.birthNumber else 8
                val rotationDegrees = birthNum * 10f
                
                elements.add(VectorDialElement.CircleRing(0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.CircleRing(0.92f, aColor, 1.0f))
                
                val padaCount = if (birthNum % 2 == 0) 32 else 24
                elements.add(VectorDialElement.TickMarks(8, 0.88f, 0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.TickMarks(padaCount, 0.90f, 0.94f, aColor, 1.0f))
                
                // Add the 9x9 Paramasayika Vastu Purusha Mandala
                elements.addAll(VastuGeometryGenerator.generate9x9Mandala(
                    radius = 1.0f,
                    rotationDegrees = rotationDegrees,
                    primaryColor = pColor,
                    accentColor = aColor
                ))
                
                // Directions (Aligned to watch face normally, or rotated if user prefers. Keeping aligned here for readability)
                elements.add(VectorDialElement.GlyphMarker(0f, 0.90f, "N", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.90f, "E", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(180f, 0.90f, "S", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.90f, "W", pColor, 12f))
            }"""

content = content.replace(vastu_old, vastu_new)

with open("wear/src/main/java/com/example/wear/WatchFaceModel.kt", "w") as f:
    f.write(content)
