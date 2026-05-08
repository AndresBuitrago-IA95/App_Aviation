package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Ajuste de insets para diseño Edge-to-Edge
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // --- REFERENCIAS DE UI (HEADER) ---
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val btnPerfilHeader = findViewById<ImageButton>(R.id.btnPerfil)

        // --- REFERENCIAS DE TARJETAS ---
        val tvTotalAeronaves = findViewById<TextView>(R.id.tvTotalAeronaves)
        val tvEnVuelo        = findViewById<TextView>(R.id.tvEnVuelo)
        val tvAlertas        = findViewById<TextView>(R.id.tvAlertasCriticas)

        // --- REFERENCIAS FORMULARIO NUEVA AERONAVE ---
        val etMatricula  = findViewById<EditText>(R.id.etMatricula)
        val etFabricante = findViewById<EditText>(R.id.etFabricante)
        val etModelo     = findViewById<EditText>(R.id.etModelo)
        val etSerie      = findViewById<EditText>(R.id.etSerie)
        val etCategoria  = findViewById<EditText>(R.id.etCategoria)
        val etAnio       = findViewById<EditText>(R.id.etAnio)
        val etPesoMax    = findViewById<EditText>(R.id.etPesoMax)
        val etCarga      = findViewById<EditText>(R.id.etCarga)
        val btnAgregar   = findViewById<Button>(R.id.btnAgregarAeronave)

        // --- NAVEGACIÓN INFERIOR ---
        val btnNavHome          = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavVuelos        = findViewById<ImageButton>(R.id.btnNavVuelos)
        val btnNavAeronaves     = findViewById<ImageButton>(R.id.btnNavAeronaves)
        val btnNavMantenimiento = findViewById<ImageButton>(R.id.btnNavMantenimiento)
        val btnNavPerfil        = findViewById<ImageButton>(R.id.btnNavPerfil)

        // =============================================
        // --- CARGAR MÉTRICAS DESDE FIREBASE ---
        // =============================================
        val database = FirebaseDatabase.getInstance().reference

        // Total de aeronaves
        database.child("aeronaves").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount.toInt()
                tvTotalAeronaves?.text = total.toString()

                // Aeronaves en vuelo (estado == "En vuelo")
                val enVuelo = snapshot.children.count { aeronave ->
                    aeronave.child("estado").getValue(String::class.java) == "En vuelo"
                }
                tvEnVuelo?.text = enVuelo.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Alertas: mantenimientos con estado "Programado"
        database.child("mantenimientos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = snapshot.children.count { m ->
                    m.child("estado").getValue(String::class.java) == "Programado"
                }
                tvAlertas?.text = alertas.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================================
        // --- BOTÓN AGREGAR AERONAVE ---
        // =============================================
        btnAgregar?.setOnClickListener {
            val matricula  = etMatricula.text.toString().trim()
            val fabricante = etFabricante.text.toString().trim()
            val modelo     = etModelo.text.toString().trim()
            val serie      = etSerie.text.toString().trim()
            val categoria  = etCategoria.text.toString().trim()
            val anio       = etAnio.text.toString().trim()
            val pesoMax    = etPesoMax.text.toString().trim()
            val carga      = etCarga.text.toString().trim()

            if (matricula.isEmpty() || fabricante.isEmpty() || modelo.isEmpty()) {
                Toast.makeText(this, "Matrícula, fabricante y modelo son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val aeronaveMap = hashMapOf(
                "matricula"          to matricula,
                "fabricante"         to fabricante,
                "modelo"             to modelo,
                "numero_serie"       to serie,
                "categoria"          to categoria,
                "anio_fabricacion"   to anio,
                "peso_maximo"        to pesoMax,
                "capacidad_carga"    to carga,
                "estado"             to "Disponible",
                "horas_vuelo_totales" to 0.0
            )

            database.child("aeronaves").push().setValue(aeronaveMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Aeronave $matricula registrada", Toast.LENGTH_SHORT).show()
                    etMatricula.text.clear(); etFabricante.text.clear(); etModelo.text.clear()
                    etSerie.text.clear(); etCategoria.text.clear(); etAnio.text.clear()
                    etPesoMax.text.clear(); etCarga.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // =============================================
        // --- LÓGICA DE BOTONES ---
        // =============================================

        btnCerrarSesion?.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity0::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnPerfilHeader?.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }

        // =============================================
        // --- NAVEGACIÓN INFERIOR ---
        // =============================================

        btnNavHome?.setOnClickListener {
            Toast.makeText(this, "Ya estás en el Inicio", Toast.LENGTH_SHORT).show()
        }

        btnNavVuelos?.setOnClickListener {
            startActivity(Intent(this, registro_vuelo::class.java))
        }

        btnNavAeronaves?.setOnClickListener {
            startActivity(Intent(this, flota_aeronave::class.java))
        }

        btnNavMantenimiento?.setOnClickListener {
            startActivity(Intent(this, gestion_mantenimiento::class.java))
        }

        btnNavPerfil?.setOnClickListener {
            startActivity(Intent(this, gestion_pilotos::class.java))
        }
    }
}