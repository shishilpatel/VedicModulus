package com.example.generator.watchface

import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.WatchFaceConfiguration
import com.example.core.model.WatchFaceStyle

/**
 * Builds declarative Watch Face Format (WFF v1 / v2) XML conforming to Google Wear OS
 * Watch Face Format specifications.
 *
 * This XML can be packaged into standalone WFF watch face APKs or transmitted via Google
 * Wear OS Data Layer / Watch Face Push API to Wear OS 4+ / 5+ devices (including Samsung
 * Galaxy Watch and Google Pixel Watch).
 */
object WatchFaceFormatBuilder {

    /**
     * Generates a complete, valid Watch Face Format XML string based on the active
     * configuration, astrological profiles, and complication selections.
     */
    fun buildWffXml(
        config: WatchFaceConfiguration,
        personalizedProfile: PersonalizedProfile?,
        dailyProfile: DailyVedicProfile?,
        width: Int = 450,
        height: Int = 450
    ): String {
        val primaryHex = String.format("#%08X", config.primaryColorHex)
        val accentHex = String.format("#%08X", config.accentColorHex)
        val styleName = config.style.name.lowercase()

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        sb.appendLine("""<WatchFace width="$width" height="$height">""")

        // Metadata Header
        sb.appendLine("""    <Metadata key="CLOCK_TYPE" value="ANALOG" />""")
        sb.appendLine("""    <Metadata key="PREVIEW_TIME" value="10:08:32" />""")
        sb.appendLine("""    <Metadata key="VENDOR" value="Vedic Astrology &amp; Vastu" />""")
        sb.appendLine("""    <Metadata key="STYLE_ID" value="${config.style.name}" />""")
        sb.appendLine("""    <Metadata key="VERSION" value="2" />""")

        // User Configurations (Colors & Complications)
        sb.appendLine("""    <UserConfigurations>""")
        sb.appendLine("""        <ColorConfiguration id="theme_palette" defaultValue="$styleName">""")
        sb.appendLine("""            <ColorOption id="$styleName" colors="$primaryHex $accentHex" />""")
        sb.appendLine("""            <ColorOption id="vedic_gold" colors="#ffffd166 #fff43f5e" />""")
        sb.appendLine("""            <ColorOption id="royal_ruby" colors="#ffffd700 #ffdc2626" />""")
        sb.appendLine("""            <ColorOption id="chakra_lotus" colors="#ffa855f7 #ff06b6d4" />""")
        sb.appendLine("""        </ColorConfiguration>""")
        sb.appendLine("""    </UserConfigurations>""")

        // Scene Definition
        sb.appendLine("""    <Scene backgroundColor="#ff000000">""")

        // Background Outer Dial Track
        sb.appendLine("""        <!-- Dial Geometry & Background -->""")
        sb.appendLine("""        <PartDraw x="0" y="0" width="$width" height="$height">""")
        sb.appendLine("""            <Circle centerX="${width / 2}" centerY="${height / 2}" radius="220" color="#ff1e293b" />""")
        sb.appendLine("""            <Circle centerX="${width / 2}" centerY="${height / 2}" radius="200" color="#ff0f172a" />""")
        sb.appendLine("""            <Circle centerX="${width / 2}" centerY="${height / 2}" radius="190" color="#ff000000" />""")

        // Style-specific elements in WFF
        when (config.style) {
            WatchFaceStyle.COSMIC -> {
                sb.appendLine("""            <!-- Cosmic Planetary Orbits -->""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="150" color="#33ffd166" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="115" color="#2238bdf8" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="80" color="#33f43f5e" strokeWidth="1" />""")
            }
            WatchFaceStyle.ROYAL -> {
                sb.appendLine("""            <!-- Indian Royal Heritage Rings -->""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="215" color="#ffffd700" strokeWidth="3" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="202" color="#ffb8860b" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="90" color="#ffdc2626" strokeWidth="2" />""")
            }
            WatchFaceStyle.CHAKRA -> {
                sb.appendLine("""            <!-- 7 Chakra Energy Vortex Bands -->""")
                val chakraColors = listOf("#ffe63946", "#fff77f00", "#fffcbf49", "#ff2a9d8f", "#ff457b9d", "#ff3d348b", "#ffe0aaff")
                val radii = listOf(210, 185, 160, 135, 110, 85, 60)
                for (i in chakraColors.indices) {
                    sb.appendLine("""            <Circle centerX="225" centerY="225" radius="${radii[i]}" color="${chakraColors[i]}" strokeWidth="1" />""")
                }
            }
            WatchFaceStyle.VASTU -> {
                sb.appendLine("""            <!-- 8-Directional Vastu Mandala Ring -->""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="212" color="$primaryHex" strokeWidth="2" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="80" color="$accentHex" strokeWidth="1" />""")
            }
            WatchFaceStyle.PANCHANGA -> {
                sb.appendLine("""            <!-- Traditional Vedic Panchanga Dial -->""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="214" color="#ffd4af37" strokeWidth="2" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="185" color="#44d4af37" strokeWidth="1" />""")
            }
            WatchFaceStyle.YANTRA -> {
                sb.appendLine("""            <!-- Sacred Sri Yantra Concentric Rings -->""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="195" color="$primaryHex" strokeWidth="2" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="185" color="#aaffd166" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="150" color="$primaryHex" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="120" color="$primaryHex" strokeWidth="1" />""")
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="8" color="#ffe63946" />""")
            }
            else -> {
                sb.appendLine("""            <Circle centerX="225" centerY="225" radius="210" color="$primaryHex" strokeWidth="2" />""")
            }
        }
        sb.appendLine("""        </PartDraw>""")

        // Complication Slots (Official Wear OS standard layout)
        sb.appendLine("""        <!-- Complications: Top, Left, Right, Bottom -->""")
        sb.appendLine("""        <ComplicationSlot slotId="0" supportedTypes="SHORT_TEXT LONG_TEXT RANGED_VALUE SMALL_IMAGE MONOCHROMATIC_IMAGE" x="175" y="35" width="100" height="55">""")
        sb.appendLine("""            <DefaultProviderPolicy primaryProvider="com.example.wear.VedicSunriseSunsetComplicationService" defaultSystemProviderType="SUNRISE_SUNSET" />""")
        sb.appendLine("""        </ComplicationSlot>""")

        sb.appendLine("""        <ComplicationSlot slotId="1" supportedTypes="SHORT_TEXT LONG_TEXT RANGED_VALUE SMALL_IMAGE MONOCHROMATIC_IMAGE" x="30" y="185" width="95" height="70">""")
        sb.appendLine("""            <DefaultProviderPolicy primaryProvider="com.example.wear.VedicComplicationDataSourceService" defaultSystemProviderType="HEART_RATE" />""")
        sb.appendLine("""        </ComplicationSlot>""")

        sb.appendLine("""        <ComplicationSlot slotId="2" supportedTypes="SHORT_TEXT LONG_TEXT RANGED_VALUE SMALL_IMAGE MONOCHROMATIC_IMAGE" x="325" y="185" width="95" height="70">""")
        sb.appendLine("""            <DefaultProviderPolicy primaryProvider="com.example.wear.VedicChoghadiyaComplicationService" />""")
        sb.appendLine("""        </ComplicationSlot>""")

        sb.appendLine("""        <ComplicationSlot slotId="3" supportedTypes="SHORT_TEXT LONG_TEXT RANGED_VALUE SMALL_IMAGE MONOCHROMATIC_IMAGE" x="175" y="345" width="100" height="60">""")
        sb.appendLine("""            <DefaultProviderPolicy primaryProvider="com.example.wear.VedicHoraComplicationService" defaultSystemProviderType="STEP_COUNT" />""")
        sb.appendLine("""        </ComplicationSlot>""")

        // Clock Hands
        sb.appendLine("""        <!-- Analog Clock Hands -->""")
        sb.appendLine("""        <AnalogClock x="0" y="0" width="$width" height="$height">""")
        sb.appendLine("""            <HourHand x="218" y="115" width="14" height="135" pivotX="0.5" pivotY="0.82">""")
        sb.appendLine("""                <PartDraw x="0" y="0" width="14" height="135">""")
        sb.appendLine("""                    <Line startX="7" startY="0" endX="7" endY="135" color="#ffffffff" strokeWidth="6" />""")
        sb.appendLine("""                </PartDraw>""")
        sb.appendLine("""            </HourHand>""")

        sb.appendLine("""            <MinuteHand x="220" y="65" width="10" height="185" pivotX="0.5" pivotY="0.86">""")
        sb.appendLine("""                <PartDraw x="0" y="0" width="10" height="185">""")
        sb.appendLine("""                    <Line startX="5" startY="0" endX="5" endY="185" color="#ffe2e8f0" strokeWidth="4" />""")
        sb.appendLine("""                </PartDraw>""")
        sb.appendLine("""            </MinuteHand>""")

        sb.appendLine("""            <SecondHand x="222" y="45" width="6" height="205" pivotX="0.5" pivotY="0.88">""")
        sb.appendLine("""                <PartDraw x="0" y="0" width="6" height="205">""")
        sb.appendLine("""                    <Line startX="3" startY="0" endX="3" endY="205" color="$primaryHex" strokeWidth="2" />""")
        sb.appendLine("""                </PartDraw>""")
        sb.appendLine("""            </SecondHand>""")
        sb.appendLine("""        </AnalogClock>""")

        // Center Pin
        sb.appendLine("""        <PartDraw x="220" y="220" width="10" height="10">""")
        sb.appendLine("""            <Circle centerX="5" centerY="5" radius="5" color="$primaryHex" />""")
        sb.appendLine("""        </PartDraw>""")

        sb.appendLine("""    </Scene>""")
        sb.appendLine("""</WatchFace>""")

        return sb.toString()
    }
}
