package com.example.assignment4

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.assignment4.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.register.setOnClickListener {
            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            val email = binding.email.text.toString()
            val username = binding.username.text.toString()
            val phone = binding.phone.text.toString()
            val password = binding.password.text.toString()
            val rePassword = binding.rePassword.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_LONG).show()
                binding.loading.visibility = View.GONE
                return@setOnClickListener
            }

            if(password != rePassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_LONG).show()
                binding.loading.visibility = View.GONE
                return@setOnClickListener
            }

            registerUser(firstName, lastName, email, username, phone, password)
        }

        binding.switchToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment())
                .commit()
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerUser(firstName: String, lastName: String, email: String, username: String, phone: String, password: String) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods.isNullOrEmpty()) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { registrationTask ->
                            binding.loading.visibility = View.GONE
                            if (registrationTask.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                val userRef = FirebaseFirestore.getInstance().collection("users").document(user?.uid!!)
                                val userData = hashMapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "email" to email,
                                    "username" to username,
                                    "phone" to phone,
                                    "password" to password
                                )

                                userRef.set(userData)
                                    .addOnCompleteListener { firestoreTask ->
                                        if (firestoreTask.isSuccessful) {
                                            Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_LONG).show()
                                            parentFragmentManager.beginTransaction()
                                                .replace(R.id.container, LoginFragment())
                                                .commit()
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Registration failed: ${registrationTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    // Email already in use
                    Toast.makeText(requireContext(), "Email is already in use, please log in", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to check email availability: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}