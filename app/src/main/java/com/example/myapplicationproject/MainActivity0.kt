package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity0 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main0)

        // Ajustar el padding para que el contenido no quede debajo de la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencia al botón "Ingresar" de la cabecera
        val btnIngresar = findViewById<Button>(R.id.btningresar)

        // Acción: Redirigir a la pantalla de Login (MainActivity)
        btnIngresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // No ponemos finish() aquí porque queremos que el usuario
            // pueda volver a leer la info si retrocede desde el login.
        }
    }
}