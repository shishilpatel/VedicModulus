package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserPreferencesRepository
import com.example.ui.viewmodel.VastuCompassViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.engines.vastu.VastuEngine

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VastuPersistenceTest {

    @Test
    fun testVastuPersistenceAcrossInstances() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        
        // 1. Initially it should default or read cached
        val vm1 = VastuCompassViewModel(app)
        
        // 2. Set to false (turn off)
        vm1.setLiveSensorActive(false)
        assertFalse(vm1.isLiveSensorActive.value)
        assertFalse(vm1.uiState.value.isLiveSensorActive)
        assertFalse(UserPreferencesRepository.isVastuLiveSensorEnabledCached(app))
        assertFalse(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(app))
        
        // 3. New ViewModel instance (simulating page change or activity recreation)
        val vm2 = VastuCompassViewModel(app)
        assertFalse("vm2 isLiveSensorActive should be false", vm2.isLiveSensorActive.value)
        assertFalse("vm2 uiState.isLiveSensorActive should be false", vm2.uiState.value.isLiveSensorActive)
        
        // 4. Test refreshFromStorage
        vm2.refreshFromStorage()
        assertFalse("After refreshFromStorage it should be false", vm2.isLiveSensorActive.value)
    }

    @Test
    fun testVastuSpaceOptimizationTogglePersistence() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        // Simulate user turning off the Vastu Space Optimization button
        UserPreferencesRepository.setVastuSpaceOptimizationEnabledCached(app, false)
        assertFalse(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(app))
        assertFalse(UserPreferencesRepository.isVastuLiveSensorEnabledCached(app))

        // Simulate navigating to another page and returning
        val vm = VastuCompassViewModel(app)
        vm.refreshFromStorage()
        assertFalse("ViewModel should remain OFF after page change", vm.isLiveSensorActive.value)
        assertFalse("UI state should remain OFF after page change", vm.uiState.value.isLiveSensorActive)

        // Simulate user turning ON the Vastu Space Optimization switch
        vm.setLiveSensorActive(true)
        assertTrue("ViewModel should be ON after toggling ON", vm.isLiveSensorActive.value)
        assertTrue("UI state should be ON after toggling ON", vm.uiState.value.isLiveSensorActive)
        assertTrue(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(app))

        // Manual interaction (selecting direction or rotating wheel) MUST NOT disable the ON switch
        vm.selectDirection(VastuEngine.ASHTA_DIK[2])
        assertTrue("Selecting direction must preserve ON switch state", vm.isLiveSensorActive.value)
        assertTrue(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(app))

        vm.updateManualRotation(180f)
        assertTrue("Rotating compass wheel must preserve ON switch state", vm.isLiveSensorActive.value)
        assertTrue(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(app))
    }
}
