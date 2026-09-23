with open("app/src/main/java/com/example/core/model/Models.kt", "r") as f:
    content = f.read()

mantra_code = """
data class Mantra(
    val id: String,
    val sanskrit: String,
    val transliteration: String,
    val language: String,
    val meaning: String,
    val associatedDeity: String? = null,
    val associatedContext: String? = null,
    val audioAsset: String? = null,
    val sourceTradition: String? = null
)

data class MuhurtaDeity(
    val deityName: String,
    val sanskritName: String,
    val transliteration: String,
    val description: String,
    val associatedMuhurta: String,
    val iconography: String? = null,
    val associatedMantra: Mantra? = null
)

"""

content = content.replace("data class TimingWindow(", mantra_code + "data class TimingWindow(")

# add muhurtaDeity to TimingWindow
tw_orig = """data class TimingWindow(
    val name: String,
    val devanagari: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isAuspicious: Boolean"""

tw_new = """data class TimingWindow(
    val name: String,
    val devanagari: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isAuspicious: Boolean,
    val deity: MuhurtaDeity? = null"""

content = content.replace(tw_orig, tw_new)

with open("app/src/main/java/com/example/core/model/Models.kt", "w") as f:
    f.write(content)
