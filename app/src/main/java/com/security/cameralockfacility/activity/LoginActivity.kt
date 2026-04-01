package com.security.cameralockfacility.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.security.cameralockfacility.ui.LoginScreen
import com.security.cameralockfacility.ui.theme.CameraLockFacilityTheme
import com.security.cameralockfacility.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraLockFacilityTheme {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("destination", "dashboard")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToRegister = {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

