package com.pcsalt.example.biometricauth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.pcsalt.example.biometricauth.databinding.ActivityMainBinding
import com.pcsalt.example.biometricauth.extension.showAlert
import com.pcsalt.example.biometricauth.extension.showToast
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBiometricPrompt()

        binding.btn.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun initBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    checkError(errorCode)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Authentication succeeded!")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("${getString(R.string.app_name)} Locked")
            .setSubtitle("Log in using your biometric credential or Device PIN")
//          Cannot use setNegativeButtonText() and setAllowedAuthenticators() both at a time
//          If only BIOMETRIC_STRONG is used in setAllowedAuthenticators() then
//          setNegativeButtonText() needs to be provided
//            .setNegativeButtonText("Use different Auth")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    private fun checkError(errorCode: Int) {
        when (errorCode) {
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                showToast("The hardware is unavailable. Try again later.")
            }
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
                showToast("The sensor was unable to process the current image.")
            }
            BiometricPrompt.ERROR_TIMEOUT -> {
                showToast("The current operation has been running too long and has timed out.")
            }
            BiometricPrompt.ERROR_NO_SPACE -> {
                showToast("Not enough device storage remaining")
            }
            BiometricPrompt.ERROR_CANCELED -> {
                showToast("The operation is canceled")
            }
            BiometricPrompt.ERROR_LOCKOUT -> {
                showToast("Too many failed attempts")
            }
            BiometricPrompt.ERROR_VENDOR -> {
                showToast("The operation failed due to a vendor-specific error.\n")
            }
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                showToast("Too many failed attempts. Use PIN to unlock.")
            }
            BiometricPrompt.ERROR_USER_CANCELED -> {
                showToast("Canceled by user")
            }
            BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                showToast("No biometric registered")
                showAlert(
                    "Error",
                    "Currently no fingerprint is registered. Please register at least one fingerprint to enable this feature.",
                    "Register",
                    {
                        val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL); startActivity(intent)
                    },
                    "Cancel",
                    { biometricPrompt.authenticate(promptInfo) })
            }
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                showToast("Required hardware not present")
            }
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                // if setNegativeButtonText() is not provided, then this error code will not come
                showToast("User pressed negative button. Developers need to handle the action.")
            }
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                showToast("The device does not have pin, pattern, or password set up.")
                showAlert(
                    "Error",
                    "The device does not have pin, pattern, or password set up. Please add any of the options to enable this feature.",
                    "Setup",
                    {
                        val intent =
                            Intent(Settings.ACTION_SECURITY_SETTINGS)//(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                        startActivity(intent)
                    }, "Dismiss", {})
            }
        }
    }
}