package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Perfil : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val etNombre   = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo   = findViewById<EditText>(R.id.etCorreo)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)

        val btnGuardar  = findViewById<Button>(R.id.btnGuardarCambios)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnAtras    = findViewById<Button>(R.id.btnAtras)

        // ── Cargar datos del usuario ──────────────────────────────────
        val user = auth.currentUser
        if (user != null) {
            etCorreo.setText(user.email)
            etCorreo.isEnabled = false

            database.child("usuarios").child(user.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        etNombre.setText(snapshot.child("nombre").getValue(String::class.java))
                        etApellido.setText(snapshot.child("apellido").getValue(String::class.java))
                        val celular = snapshot.child("celular").getValue(String::class.java)
                            ?: snapshot.child("telefono").getValue(String::class.java)
                        etTelefono.setText(celular)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show()
                }
        }

        // ── Guardar cambios ───────────────────────────────────────────
        btnGuardar.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val datos = hashMapOf<String, Any>(
                    "nombre"   to etNombre.text.toString().trim(),
                    "apellido" to etApellido.text.toString().trim(),
                    "celular"  to etTelefono.text.toString().trim()
                )
                database.child("usuarios").child(uid).updateChildren(datos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnCancelar.setOnClickListener { recreate() }
        btnAtras.setOnClickListener { finish() }

        // ── Navegación inferior completa ──────────────────────────────
        findViewById<ImageButton>(R.id.btnNavHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.btnNavVuelos).setOnClickListener {
            startActivity(Intent(this, registro_vuelo::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavAeronaves).setOnClickListener {
            startActivity(Intent(this, flota_aeronave::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavMantenimiento).setOnClickListener {
            startActivity(Intent(this, gestion_mantenimiento::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavPerfil).setOnClickListener {
            // Ya estás en esta pantalla
        }
    }
}
