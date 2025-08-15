package com.example.basictaskmanagerapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var btnCreateAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etSignupEmail = findViewById(R.id.etSignupEmail)
        etSignupPassword = findViewById(R.id.etSignupPassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)

        btnCreateAccount.setOnClickListener {
            val email = etSignupEmail.text.toString().trim()
            val password = etSignupPassword.text.toString().trim()

            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Valid email & 6+ char password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    // Save user profile doc to Firestore (optional)
                    val userDoc = mapOf("email" to email, "createdAt" to System.currentTimeMillis())
                    firestore.collection("users").document(uid).set(userDoc)
                        .addOnCompleteListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message ?: "Signup failed", Toast.LENGTH_LONG).show()
                }
        }
    }
}
