package com.example.basictaskmanagerapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide action bar for splash-like experience
        supportActionBar?.hide()

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)

        initViews()
        initFirebase()
        setupClickListeners()
        setupAnimations()
        checkExistingUser()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
    }

    private fun checkExistingUser() {
        if (auth.currentUser != null) {
            navigateToMain()
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        btnGoogleSignIn.setOnClickListener {
            // TODO: Implement Google Sign In
            showMessage("Google Sign In coming soon!", false)
        }
    }

    private fun setupAnimations() {
        // Animate logo and title
        val logo = findViewById<View>(R.id.ivLogo)
        val title = findViewById<View>(R.id.tvAppTitle)
        val subtitle = findViewById<View>(R.id.tvSubtitle)

        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(200)
            .start()

        title.alpha = 0f
        title.translationY = -50f
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .start()

        subtitle.alpha = 0f
        subtitle.translationY = -30f
        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(600)
            .start()

        // Animate form elements
        val formElements = listOf(tilEmail, tilPassword, btnLogin, btnRegister, btnGoogleSignIn)
        formElements.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 100f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(800 + (index * 100).toLong())
                .start()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Email validation
        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            tilEmail.isErrorEnabled = true
            shakeView(tilEmail)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Please enter a valid email"
            tilEmail.isErrorEnabled = true
            shakeView(tilEmail)
            isValid = false
        } else {
            tilEmail.error = null
            tilEmail.isErrorEnabled = false
        }

        // Password validation
        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            tilPassword.isErrorEnabled = true
            if (isValid) shakeView(tilPassword)
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            tilPassword.isErrorEnabled = true
            if (isValid) shakeView(tilPassword)
            isValid = false
        } else {
            tilPassword.error = null
            tilPassword.isErrorEnabled = false
        }

        return isValid
    }

    private fun shakeView(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).apply {
            duration = 600
            start()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showLoading(false)
                showMessage("Welcome back! 🎉", true)
                animateSuccessAndNavigate()
            }
            .addOnFailureListener { error ->
                showLoading(false)
                val errorMessage = when {
                    error.message?.contains("user-not-found") == true ->
                        "No account found with this email"
                    error.message?.contains("wrong-password") == true ->
                        "Incorrect password"
                    error.message?.contains("invalid-email") == true ->
                        "Invalid email format"
                    error.message?.contains("network") == true ->
                        "Network error. Please check your connection"
                    else -> "Login failed: ${error.message}"
                }
                showMessage(errorMessage, false)
            }
    }

    private fun animateSuccessAndNavigate() {
        // Success animation
        btnLogin.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction {
                btnLogin.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .withEndAction {
                        navigateToMain()
                    }
                    .start()
            }
            .start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnRegister.isEnabled = false
            btnGoogleSignIn.isEnabled = false
            btnLogin.text = "Signing in..."
        } else {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            btnRegister.isEnabled = true
            btnGoogleSignIn.isEnabled = true
            btnLogin.text = "Sign In"
        }
    }

    private fun showMessage(message: String, isSuccess: Boolean) {
        val color = if (isSuccess) R.color.success_color else R.color.error_color
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, color))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .show()
    }

    override fun onBackPressed() {
        // Add custom back button behavior or exit animation
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}