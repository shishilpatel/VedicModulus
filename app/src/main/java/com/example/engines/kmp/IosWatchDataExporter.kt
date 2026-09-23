package com.example.engines.kmp

import java.time.LocalDate
import java.time.LocalTime

/**
 * Exporter bridge that serializes pure Vedic calculations into Swift / SwiftUI friendly JSON & key-value payloads.
 *
 * This allows iOS developers to receive live Vedic Panchanga, Muhurta, and Hora data
 * via Bluetooth BLE, Local Network API, or exported files for rendering on Apple Watch WatchKit & WidgetKit complications.
 */
object IosWatchDataExporter {

    fun exportAsSwiftDictionaryJson(): String {
        val export = KmpVedicCoreEngine.generateWatchComplicationExport()
        return """
        {
          "timeFormatted": "${export.timeFormatted}",
          "tithi": "${export.tithi}",
          "nakshatra": "${export.nakshatra}",
          "choghadiya": "${export.choghadiya}",
          "choghadiyaDevanagari": "${export.choghadiyaDevanagari}",
          "horaPlanet": "${export.horaPlanet}",
          "horaSymbol": "${export.horaSymbol}",
          "isAuspicious": ${export.isAuspicious},
          "rahuKalam": "${export.rahuKalam}",
          "batteryPercent": ${export.batteryPercent},
          "vitalityScore": ${export.vitalityScore},
          "timestampMs": ${export.timestampMs}
        }
        """.trimIndent()
    }

    /**
     * Generates SwiftUI Swift code snippet template for iOS/watchOS developers to map this engine output.
     */
    fun generateSwiftUiComplicationSnippet(): String {
        return """
        // Swift / SwiftUI WatchKit Complication Data Model
        import SwiftUI

        struct VedicComplicationData: Codable {
            let timeFormatted: String
            let tithi: String
            let nakshatra: String
            let choghadiya: String
            let choghadiyaDevanagari: String
            let horaPlanet: String
            let horaSymbol: String
            let isAuspicious: Bool
            let rahuKalam: String
            let batteryPercent: Int
            let vitalityScore: Int
            let timestampMs: Int64
        }

        struct VedicWatchWidgetView: View {
            let data: VedicComplicationData

            var body: some View {
                VStack(spacing: 2) {
                    Text(data.choghadiyaDevanagari)
                        .font(.caption)
                        .bold()
                        .foregroundColor(data.isAuspicious ? .green : .red)
                    Text(data.horaSymbol + " " + data.horaPlanet)
                        .font(.footnote)
                    Text(data.tithi)
                        .font(.system(size: 8))
                        .foregroundColor(.secondary)
                }
            }
        }
        """.trimIndent()
    }
}
