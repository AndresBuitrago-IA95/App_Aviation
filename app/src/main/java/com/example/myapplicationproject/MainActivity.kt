package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Manejo de insets
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Referencias de los componentes
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginbtn = findViewById<Button>(R.id.btnLogin)
        val crearbtn = findViewById<Button>(R.id.btnRegister)

        // --- LÓGICA DE INICIO DE SESIÓN ---
        loginbtn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()

            if (emailText.isNotEmpty() && passText.isNotEmpty()) {
                auth.signInWithEmailAndPassword(emailText, passText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Bienvenida de nuevo!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // --- BOTÓN PARA IR A CREAR CUENTA ---
        crearbtn.setOnClickListener {
            startActivity(Intent(this, crear_cuenta::class.java))
        }
    }

    // Se eliminó la redirección automática del onStart para permitir ver el Login siempre.
}