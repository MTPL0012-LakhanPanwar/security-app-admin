package com.security.cameralockfacility.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.security.cameralockfacility.auth.TokenManager
import com.security.cameralockfacility.ui.AdminSplashScreen
import com.security.cameralockfacility.ui.CreateUpdateScreen
import com.security.cameralockfacility.ui.DashboardScreen
import com.security.cameralockfacility.ui.DeviceDetailScreen
import com.security.cameralockfacility.ui.FacilityDetailScreen
import com.security.cameralockfacility.ui.LoginScreen
import com.security.cameralockfacility.ui.RegisterScreen
import com.security.cameralockfacility.ui.theme.CameraLockFacilityTheme
import com.security.cameralockfacility.viewmodel.AdminViewModel
import com.security.cameralockfacility.viewmodel.AuthViewModel
import com.security.cameralockfacility.viewmodel.DeviceViewModel
import com.security.cameralockfacility.viewmodel.FacilityViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val facilityViewModel: FacilityViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tokenManager = TokenManager(this)
        setContent {
            CameraLockFacilityTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {

                    composable("splash") {
                        LaunchedEffect(Unit) {
                            delay(1500)
                            if (tokenManager.isLoggedIn()) {
                                navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                        AdminSplashScreen()
                    }

                    composable("login") {
                        LoginScreen(navController = navController, viewModel = authViewModel)
                    }

                    composable("register") {
                        RegisterScreen(navController = navController, viewModel = authViewModel)
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            outerNavController = navController,
                            facilityViewModel = facilityViewModel,
                            adminViewModel = adminViewModel,
                            deviceViewModel = deviceViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("facility/create") {
                        CreateUpdateScreen(
                            facilityId = null,
                            navController = navController,
                            viewModel = facilityViewModel
                        )
                    }

                    composable("facility/edit/{facilityId}") { backStack ->
                        val facilityId = backStack.arguments?.getString("facilityId")
                        CreateUpdateScreen(
                            facilityId = facilityId,
                            navController = navController,
                            viewModel = facilityViewModel
                        )
                    }

                    composable("facility/detail/{facilityId}") { backStack ->
                        val facilityId = backStack.arguments?.getString("facilityId") ?: return@composable
                        FacilityDetailScreen(
                            facilityId = facilityId,
                            navController = navController,
                            viewModel = facilityViewModel,
                            onUnauthorized = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("device/{deviceId}") { backStack ->
                        val deviceId = backStack.arguments?.getString("deviceId")
                            ?: return@composable
                        DeviceDetailScreen(
                            deviceId = deviceId,
                            navController = navController,
                            viewModel = deviceViewModel,
                            tokenManager = tokenManager,
                            onUnauthorized = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
