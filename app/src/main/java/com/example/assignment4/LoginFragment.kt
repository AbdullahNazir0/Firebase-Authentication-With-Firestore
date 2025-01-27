package com.example.assignment4

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.assignment4.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE

            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        binding.switchToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, RegisterFragment())
                .commit()
        }

        binding.forgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, ForgotPasswordFragment())
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

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.loading.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val sp = requireActivity().getSharedPreferences("user", 0)
                    val editor = sp.edit()
                    editor.putString("email", user?.email)
                    editor.putString("uid", user?.uid)
                    editor.apply()
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_LONG).show()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            // The user does not exist or has been deleted
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Invalid password or email format
                            Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthUserCollisionException -> {
                            // Email is already registered
                            Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            // General authentication failure
                            Toast.makeText(requireContext(), "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }
}