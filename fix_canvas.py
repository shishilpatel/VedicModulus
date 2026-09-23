import re

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "r") as f:
    content = f.read()

# 1. Add measuredGlyphs remember block after val textMeasurer = rememberTextMeasurer()
remember_block = """    val textMeasurer = rememberTextMeasurer()

    val measuredGlyphs = remember(dialModel, config) {
        dialModel.elements.filterIsInstance<com.example.generator.watchface.VectorDialElement.GlyphMarker>().associate { elem ->
            elem to textMeasurer.measure(
                text = elem.text,
                style = androidx.compose.ui.text.TextStyle(
                    color = androidx.compose.ui.graphics.Color(elem.colorHex),
                    fontSize = elem.fontSizeSp.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }"""
content = content.replace("    val textMeasurer = rememberTextMeasurer()", remember_block)

# 2. Update renderDialElements signature
sig_old = """private fun DrawScope.renderDialElements(
    elements: List<VectorDialElement>,
    center: Offset,
    radius: Float,
    textMeasurer: TextMeasurer
) {"""
sig_new = """private fun DrawScope.renderDialElements(
    elements: List<VectorDialElement>,
    center: Offset,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    measuredGlyphs: Map<com.example.generator.watchface.VectorDialElement.GlyphMarker, androidx.compose.ui.text.TextLayoutResult>
) {"""
content = content.replace(sig_old, sig_new)

# 3. Update call site of renderDialElements
content = content.replace("renderDialElements(dialModel.elements, center, radius, textMeasurer)", 
                          "renderDialElements(dialModel.elements, center, radius, textMeasurer, measuredGlyphs)")

# 4. Update the GlyphMarker branch in renderDialElements
glyph_old = """            is VectorDialElement.GlyphMarker -> {
                val angleRad = Math.toRadians(elem.angleDeg.toDouble())
                val r = radius * elem.radiusRatio
                val pos = Offset(center.x + (r * cos(angleRad)).toFloat(), center.y + (r * sin(angleRad)).toFloat())
                val layoutResult = textMeasurer.measure(
                    text = elem.text,
                    style = TextStyle(
                        color = Color(elem.colorHex),
                        fontSize = elem.fontSizeSp.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
                drawText(
                    layoutResult,
                    topLeft = Offset(pos.x - layoutResult.size.width / 2f, pos.y - layoutResult.size.height / 2f)
                )
            }"""
glyph_new = """            is VectorDialElement.GlyphMarker -> {
                val angleRad = Math.toRadians(elem.angleDeg.toDouble())
                val r = radius * elem.radiusRatio
                val pos = Offset(center.x + (r * cos(angleRad)).toFloat(), center.y + (r * sin(angleRad)).toFloat())
                val layoutResult = measuredGlyphs[elem]
                if (layoutResult != null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(pos.x - layoutResult.size.width / 2f, pos.y - layoutResult.size.height / 2f)
                    )
                }
            }"""
content = content.replace(glyph_old, glyph_new)

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "w") as f:
    f.write(content)
