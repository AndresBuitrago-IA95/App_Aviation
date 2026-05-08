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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Perfil : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // Corregido: usar Realtime Database en lugar de Firestore,
    // ya que crear_cuenta guarda los datos ahí.
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        // Configuración de Insets para diseño borde a borde
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Referencias de los campos
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)

        // Referencias de los botones
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCambios)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnAtras = findViewById<Button>(R.id.btnAtras)

        // 1. CARGAR DATOS DEL USUARIO (Desde Firebase Auth y Realtime Database)
        val user = auth.currentUser
        if (user != null) {
            etCorreo.setText(user.email)
            etCorreo.isEnabled = false // El correo no se edita directamente por seguridad

            // Traer datos adicionales de Realtime Database (Nombre, Apellido, Celular)
            database.child("usuarios").child(user.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        etNombre.setText(snapshot.child("nombre").getValue(String::class.java))
                        etApellido.setText(snapshot.child("apellido").getValue(String::class.java))
                        // El campo guardado en crear_cuenta es "celular", lo mostramos en etTelefono
                        val celular = snapshot.child("celular").getValue(String::class.java)
                            ?: snapshot.child("telefono").getValue(String::class.java)
                        etTelefono.setText(celular)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show()
                }
        }

        // 2. BOTÓN GUARDAR CAMBIOS
        btnGuardar.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val datosActualizados = hashMapOf<String, Any>(
                    "nombre"   to etNombre.text.toString().trim(),
                    "apellido" to etApellido.text.toString().trim(),
                    "celular"  to etTelefono.text.toString().trim()
                )

                database.child("usuarios").child(uid).updateChildren(datosActualizados)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 3. BOTÓN CANCELAR (Limpia o recarga la actividad)
        btnCancelar.setOnClickListener {
            recreate() // Reinicia la actividad para descartar cambios no guardados
        }

        // 4. BOTÓN ATRÁS
        btnAtras.setOnClickListener {
            finish() // Cierra esta pantalla y vuelve a la anterior (Dashboard)
        }

        // 5. NAVEGACIÓN INFERIOR (Solo ejemplo para el botón Home)
        findViewById<android.widget.ImageButton>(R.id.btnNavHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}