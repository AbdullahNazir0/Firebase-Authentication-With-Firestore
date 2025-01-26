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
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser!!.uid)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                binding.firstName.text = document.getString("firstName")
                binding.lastName.text = document.getString("lastName")
                binding.username.text = document.getString("username")
                binding.email.text = document.getString("email")
                binding.phone.text = document.getString("phone")
                binding.password.text = document.getString("password")
            } else {
                Toast.makeText(this, "No such document", Toast.LENGTH_LONG).show()
            }


            binding.deleteAccount.setOnClickListener {
                userRef.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser.delete().addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_LONG).show()
        }

    }
}