import re

with open("wear/src/main/java/com/example/wear/WatchFaceModel.kt", "r") as f:
    content = f.read()

# Update generateDialModel signature
sig_old = """    fun generateDialModel(
        style: WatchFaceStyle,
        primaryColorHex: Long = 0xFFFFD166,
        accentColorHex: Long = 0xFFF43F5E,
        isRahuActive: Boolean = false
    ): ProceduralWatchFaceModel {"""

sig_new = """    fun generateDialModel(
        style: WatchFaceStyle,
        syncData: SyncData
    ): ProceduralWatchFaceModel {
        val pColor = syncData.primaryColorHex
        val aColor = syncData.accentColorHex
        val isRahuActive = syncData.isRahuActive
"""

content = content.replace(sig_old, sig_new)

# Update internal variable assignments that we just replaced
vars_old = """        val elements = mutableListOf<VectorDialElement>()
        val pColor = primaryColorHex
        val aColor = accentColorHex
        val bgHex = 0xFF000000"""

vars_new = """        val elements = mutableListOf<VectorDialElement>()
        val bgHex = 0xFF000000"""

content = content.replace(vars_old, vars_new)

# Replace YANTRA and VASTU generators
yantra_old = """            WatchFaceStyle.YANTRA -> {
                elements.add(VectorDialElement.CircleRing(0.95f, pColor, 2.0f))
                elements.add(VectorDialElement.CircleRing(0.90f, 0x44FFD166, 1.0f))
                elements.add(VectorDialElement.TickMarks(24, 0.88f, 0.94f, pColor, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.74f, 0x55FFD166, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.58f, 0x77FFD166, 1.2f))
                val upTriangle = listOf(
                    0.0f to -0.65f,
                    0.56f to 0.32f,
                    -0.56f to 0.32f
                )
                val downTriangle = listOf(
                    0.0f to 0.65f,
                    0.56f to -0.32f,
                    -0.56f to -0.32f
                )
                elements.add(VectorDialElement.PolygonShape(upTriangle, pColor, 1.5f))
                elements.add(VectorDialElement.PolygonShape(downTriangle, aColor, 1.5f))
                elements.add(VectorDialElement.GlyphMarker(0f, 0.1f, "ॐ", pColor, 20f))
            }
            WatchFaceStyle.VASTU -> {
                elements.add(VectorDialElement.CircleRing(0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.CircleRing(0.92f, aColor, 1.0f))
                elements.add(VectorDialElement.TickMarks(8, 0.88f, 0.94f, pColor, 3.0f))
                elements.add(VectorDialElement.TickMarks(32, 0.90f, 0.94f, 0x77FFD166, 1.0f))
                
                // Outer Vastu Square Mandala (rotated)
                val square = listOf(
                    0.0f to -0.75f,
                    0.75f to 0.0f,
                    0.0f to 0.75f,
                    -0.75f to 0.0f
                )
                elements.add(VectorDialElement.PolygonShape(square, pColor, 2.0f))
                val innerSquare = listOf(
                    -0.53f to -0.53f,
                    0.53f to -0.53f,
                    0.53f to 0.53f,
                    -0.53f to 0.53f
                )
                elements.add(VectorDialElement.PolygonShape(innerSquare, aColor, 1.5f))
                
                // Directions
                elements.add(VectorDialElement.GlyphMarker(0f, 0.82f, "N", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.82f, "E", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(180f, 0.82f, "S", pColor, 12f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.82f, "W", pColor, 12f))
            }"""

yantra_new = """            WatchFaceStyle.YANTRA -> {
                // Personalize Yantra based on Destiny Number (default 9 triangles if not provided, but customized here)
                val destiny = if (syncData.destinyNumber in 1..9) syncData.destinyNumber else 9
                
                elements.add(VectorDialElement.CircleRing(0.95f, pColor, 2.0f))
                elements.add(VectorDialElement.CircleRing(0.90f, 0x44FFD166, 1.0f))
                elements.add(VectorDialElement.TickMarks(24, 0.88f, 0.94f, pColor, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.74f, 0x55FFD166, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.58f, 0x77FFD166, 1.2f))
                
                // Draw multiple interlocking triangles based on destiny number
                val numTriangles = (destiny % 3) + 3 // 3, 4, or 5 primary interlocking triangles
                for (i in 0 until numTriangles) {
                    val angleOffset = i * (Math.PI / numTriangles)
                    val r = 0.65f - (i * 0.08f)
                    
                    val p1 = Pair((r * cos(angleOffset - Math.PI/2)).toFloat(), (r * sin(angleOffset - Math.PI/2)).toFloat())
                    val p2 = Pair((r * cos(angleOffset + Math.PI/6)).toFloat(), (r * sin(angleOffset + Math.PI/6)).toFloat())
                    val p3 = Pair((r * cos(angleOffset + 5*Math.PI/6)).toFloat(), (r * sin(angleOffset + 5*Math.PI/6)).toFloat())
                    
                    val triangleColor = if (i % 2 == 0) pColor else aColor
                    elements.add(VectorDialElement.PolygonShape(listOf(p1, p2, p3), triangleColor, 1.5f))
                    
                    // Inverse triangle
                    val ip1 = Pair((r * cos(angleOffset + Math.PI/2)).toFloat(), (r * sin(angleOffset + Math.PI/2)).toFloat())
                    val ip2 = Pair((r * cos(angleOffset - Math.PI/6)).toFloat(), (r * sin(angleOffset - Math.PI/6)).toFloat())
                    val ip3 = Pair((r * cos(angleOffset - 5*Math.PI/6)).toFloat(), (r * sin(angleOffset - 5*Math.PI/6)).toFloat())
                    elements.add(VectorDialElement.PolygonShape(listOf(ip1, ip2, ip3), triangleColor, 1.5f))
                }
                
                val centerGlyph = syncData.natalNakshatra.take(1).ifEmpty { "ॐ" }
                elements.add(VectorDialElement.GlyphMarker(0f, 0.08f, centerGlyph, pColor, 18f))
            }
            WatchFaceStyle.VASTU -> {
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

content = content.replace(yantra_old, yantra_new)

# PolygonShape requires isDashed in the class definition now if we want to use it
shape_old = """    data class PolygonShape(
        val normalizedVertices: List<Pair<Float, Float>>,
        val colorHex: Long,
        val strokeWidthDp: Float,
        val isFilled: Boolean = false
    ) : VectorDialElement()"""

shape_new = """    data class PolygonShape(
        val normalizedVertices: List<Pair<Float, Float>>,
        val colorHex: Long,
        val strokeWidthDp: Float,
        val isFilled: Boolean = false,
        val isDashed: Boolean = false
    ) : VectorDialElement()"""

content = content.replace(shape_old, shape_new)

with open("wear/src/main/java/com/example/wear/WatchFaceModel.kt", "w") as f:
    f.write(content)


with open("wear/src/main/java/com/example/wear/VedicWatchFaceCanvas.kt", "r") as f:
    content2 = f.read()

# Update call site
call_old = """    val dialModel = remember(style, syncData.primaryColorHex, syncData.accentColorHex, syncData.isRahuActive) {
        generator.generateDialModel(
            style = style,
            primaryColorHex = syncData.primaryColorHex,
            accentColorHex = syncData.accentColorHex,
            isRahuActive = syncData.isRahuActive
        )
    }"""

call_new = """    val dialModel = remember(style, syncData) {
        generator.generateDialModel(
            style = style,
            syncData = syncData
        )
    }"""

content2 = content2.replace(call_old, call_new)

# Update renderDialElements to handle dashed PolygonShape
poly_old = """            is VectorDialElement.PolygonShape -> {
                if (elem.normalizedVertices.size >= 3) {
                    val path = Path()
                    elem.normalizedVertices.forEachIndexed { idx, vertex ->
                        val vx = center.x + radius * vertex.first
                        val vy = center.y + radius * vertex.second
                        if (idx == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
                    }
                    path.close()
                    val strokeStyle = if (elem.isFilled) Fill else Stroke(width = elem.strokeWidthDp.dp.toPx())
                    drawPath(
                        path = path,
                        color = Color(elem.colorHex),
                        style = strokeStyle
                    )
                }
            }"""

poly_new = """            is VectorDialElement.PolygonShape -> {
                if (elem.normalizedVertices.size >= 3) {
                    val path = Path()
                    elem.normalizedVertices.forEachIndexed { idx, vertex ->
                        val vx = center.x + radius * vertex.first
                        val vy = center.y + radius * vertex.second
                        if (idx == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
                    }
                    path.close()
                    val strokeStyle = if (elem.isFilled) Fill else {
                        if (elem.isDashed) Stroke(width = elem.strokeWidthDp.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                        else Stroke(width = elem.strokeWidthDp.dp.toPx())
                    }
                    drawPath(
                        path = path,
                        color = Color(elem.colorHex),
                        style = strokeStyle
                    )
                }
            }"""

content2 = content2.replace(poly_old, poly_new)

with open("wear/src/main/java/com/example/wear/VedicWatchFaceCanvas.kt", "w") as f:
    f.write(content2)
