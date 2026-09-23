package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.components.GalaxyWatchFaceCanvas
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val engine = VedicPersonalizationEngine()
    val profile = BirthProfile(
      fullName = "Aarav Sharma",
      birthDate = LocalDate.of(1990, 8, 15),
      birthTime = LocalTime.of(7, 42),
      birthLocation = CityRepository.getDefaultCity(),
      preferredStyle = WatchFaceStyle.COSMIC
    )
    val personalized = engine.createPersonalizedProfile(profile)
    val daily = engine.createDailyVedicProfile(ZonedDateTime.parse("2024-01-01T12:00:00Z"), profile.birthLocation)
    val config = engine.generateWatchFaceConfig(personalized)

    composeTestRule.mainClock.autoAdvance = false

    composeTestRule.setContent {
      MyApplicationTheme {
        GalaxyWatchFaceCanvas(
          config = config,
          personalizedProfile = personalized,
          dailyProfile = daily
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
