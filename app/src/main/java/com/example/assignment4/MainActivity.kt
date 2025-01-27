package com.example.assignment4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.assignment4.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val userRef = currentUser?.let { FirebaseFirestore.getInstance().collection("users").document(currentUser.uid) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (currentUser == null) {
            logout()
            return
        }

        if(userRef == null) {
            logout()
            return
        }

        fetchUser()

        binding.deleteAccount.setOnClickListener {
            deleteUser()
        }

        binding.logout.setOnClickListener {
            logout()
        }

    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun logout() {
        auth.signOut()
        navigateToLogin()
    }

    private fun deleteUser() {
        userRef?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2️⃣ Delete user from Firebase Authentication
                currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        logout()
                    } else {
                        Toast.makeText(this, "Failed to delete account from authentication", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to delete user data from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUser() {
        userRef?.get()?.addOnSuccessListener { document ->
            if (document.exists()) {
                binding.firstName.text = "First Name: " + document.getString("firstName")
                binding.lastName.text = "Last Name: " + document.getString("lastName")
                binding.username.text = "Username: " + document.getString("username")
                binding.email.text = "Email: " + document.getString("email")
                binding.phone.text = "Phone: " + document.getString("phone")
                binding.password.text = "Password: " + document.getString("password")
            } else {
                Toast.makeText(this, "No such document", Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_LONG).show()
            logout()
        } ?: run {
            Toast.makeText(this, "User reference is null", Toast.LENGTH_SHORT).show()
            logout()
        }
    }
}