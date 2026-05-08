package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class crear_cuenta : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_cuenta)

        // Inicializar Firebase Auth y Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val etNombre    = findViewById<EditText>(R.id.etNombre)
        val etApellido  = findViewById<EditText>(R.id.etApellido)
        val etCorreo    = findViewById<EditText>(R.id.etCorreo)
        val etCelular   = findViewById<EditText>(R.id.etCelular)
        val etPassword  = findViewById<EditText>(R.id.etPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val btnRegresar  = findViewById<Button>(R.id.btnRegresar)

        btnRegistrar.setOnClickListener {

            val nombre   = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val email    = etCorreo.text.toString().trim()
            val celular  = etCelular.text.toString().trim()
            val pass     = etPassword.text.toString().trim()

            // ── Validaciones ──────────────────────────────────────────
            when {
                nombre.isEmpty() -> {
                    etNombre.error = "Ingresa tu nombre"
                    etNombre.requestFocus()
                    return@setOnClickListener
                }
                apellido.isEmpty() -> {
                    etApellido.error = "Ingresa tu apellido"
                    etApellido.requestFocus()
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    etCorreo.error = "Ingresa tu correo"
                    etCorreo.requestFocus()
                    return@setOnClickListener
                }
                celular.isEmpty() -> {
                    etCelular.error = "Ingresa tu celular"
                    etCelular.requestFocus()
                    return@setOnClickListener
                }
                pass.isEmpty() -> {
                    etPassword.error = "Ingresa tu contraseña"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }
                pass.length < 6 -> {
                    etPassword.error = "La contraseña debe tener mínimo 6 caracteres"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }
            }

            // ── Registro en Firebase Auth ─────────────────────────────
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val userId = auth.currentUser?.uid

                        // Fecha actual para fecha_registro
                        val fechaRegistro = SimpleDateFormat(
                            "yyyy-MM-dd", Locale.getDefault()
                        ).format(Date())

                        // ── Mapa de datos que coincide con tu BD ──────
                        val userMap = hashMapOf(
                            "nombre"          to nombre,
                            "apellido"        to apellido,
                            "correo"          to email,
                            "celular"         to celular,
                            "fecha_registro"  to fechaRegistro
                        )

                        // ── Guardar en Realtime Database ──────────────
                        userId?.let { uid ->
                            database.child("usuarios").child(uid)
                                .setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "¡Registro exitoso en DatAviation!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(this, MainActivity::class.java)
                                    )
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error al guardar datos: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }

                    } else {
                        // ── Mensajes de error amigables ───────────────
                        val errorMsg = when {
                            task.exception?.message?.contains("email address is already in use") == true ->
                                "Este correo ya está registrado"
                            task.exception?.message?.contains("badly formatted") == true ->
                                "El formato del correo no es válido"
                            task.exception?.message?.contains("Password should be at least") == true ->
                                "La contraseña debe tener mínimo 6 caracteres"
                            else -> "Error: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }
}