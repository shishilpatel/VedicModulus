The application freezing has been traced to a Jetpack Compose architectural issue where heavy "root" screen components (`DashboardScreen` and `DailyHighlightsTab`) were observing a `currentTime` state that ticked *every single second*. This caused the entire application layout—including all charts, lists, API parsers, and UI cards—to invalidate and redraw 60 times a minute, overloading the main thread and resulting in ANR (Application Not Responding) freezes.

To resolve this, I have:
1. Removed the second-by-second ticker from the root level of `DashboardScreen` and `DailyHighlightsTab`.
2. Down-scoped the 1Hz `currentTime` state directly into the specific sub-components that actually need it (e.g., `VedicTimeCard` and `WatchFaceTab`). 
3. The root screens now safely observe a `currentMinuteTick` state, which only re-evaluates the heavy layout once per minute. 

This correctly optimizes the Compose tree rendering, allowing the app to run smoothly without locking up the UI thread.
