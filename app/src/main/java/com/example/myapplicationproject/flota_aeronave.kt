package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class flota_aeronave : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flota_aeronave)

        // ── Inicializar Firebase ──────────────────────────────────────
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // ── Referencias de UI (Header) ────────────────────────────────
        val btnPerfilHeader = findViewById<ImageButton>(R.id.btnPerfil)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarsesion)

        // ── ScrollView (para hacer scroll al detalle) ─────────────────
        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        // ── Referencias de tabla ──────────────────────────────────────
        val btnDetalle1 = findViewById<Button>(R.id.btnDetalle1)
        val btnDetalle2 = findViewById<Button>(R.id.btnDetalle2)
        val btnDetalle3 = findViewById<Button>(R.id.btnDetalle3)

        val cardDetalle1 = findViewById<CardView>(R.id.cardDetalle1)
        val cardDetalle2 = findViewById<CardView>(R.id.cardDetalle2)
        val cardDetalle3 = findViewById<CardView>(R.id.cardDetalle3)

        // ── Referencias botones eliminar ──────────────────────────────
        val btnEliminar1 = findViewById<Button>(R.id.btnEliminarAeronave1)
        val btnEliminar2 = findViewById<Button>(R.id.btnEliminarAeronave2)
        val btnEliminar3 = findViewById<Button>(R.id.btnEliminarAeronave3)

        // ── Referencias navegación inferior ───────────────────────────
        val btnNavHome          = findViewById<ImageButton>(R.id.btnNavHome)
        val btnNavVuelos        = findViewById<ImageButton>(R.id.btnNavVuelos)
        val btnNavAeronaves     = findViewById<ImageButton>(R.id.btnNavAeronaves)
        val btnNavMantenimiento = findViewById<ImageButton>(R.id.btnNavMantenimiento)
        val btnNavPerfil        = findViewById<ImageButton>(R.id.btnNavPerfil)

        // ── IDs de aeronaves en Firebase ──────────────────────────────
        val idAeronave1 = "aeronave_001" // HK-1234
        val idAeronave2 = "aeronave_002" // HK-5678
        val idAeronave3 = "aeronave_003" // HK-9012

        // ── Ocultar todas las tarjetas de detalle al inicio ───────────
        fun ocultarTodos() {
            cardDetalle1.visibility = View.GONE
            cardDetalle2.visibility = View.GONE
            cardDetalle3.visibility = View.GONE
        }

        ocultarTodos()

        // ── Función para mostrar detalle y hacer scroll hacia él ──────
        fun mostrarDetalle(card: CardView) {
            ocultarTodos()
            card.visibility = View.VISIBLE

            // Espera que el layout termine de dibujarse y luego hace scroll
            card.post {
                scrollView.smoothScrollTo(0, card.top)
            }
        }

        // ── Botones Detalle: muestran la tarjeta de la matrícula ──────
        btnDetalle1.setOnClickListener { mostrarDetalle(cardDetalle1) } // HK-1234
        btnDetalle2.setOnClickListener { mostrarDetalle(cardDetalle2) } // HK-5678
        btnDetalle3.setOnClickListener { mostrarDetalle(cardDetalle3) } // HK-9012

        // ── Función reutilizable para eliminar aeronave ───────────────
        fun eliminarAeronave(idAeronave: String, matricula: String, card: CardView) {
            AlertDialog.Builder(this)
                .setTitle("Eliminar Aeronave")
                .setMessage("¿Estás seguro de que deseas eliminar la aeronave $matricula? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    database.child("aeronaves").child(idAeronave)
                        .removeValue()
                        .addOnSuccessListener {
                            card.visibility = View.GONE
                            android.widget.Toast.makeText(
                                this,
                                "✅ Aeronave $matricula eliminada correctamente",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            android.widget.Toast.makeText(
                                this,
                                "❌ Error al eliminar: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // ── Botones eliminar con confirmación ─────────────────────────
        btnEliminar1.setOnClickListener { eliminarAeronave(idAeronave1, "HK-1234", cardDetalle1) }
        btnEliminar2.setOnClickListener { eliminarAeronave(idAeronave2, "HK-5678", cardDetalle2) }
        btnEliminar3.setOnClickListener { eliminarAeronave(idAeronave3, "HK-9012", cardDetalle3) }

        // ── Header ────────────────────────────────────────────────────
        btnPerfilHeader.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }

        // Cerrar sesión con Firebase → redirige a MainActivity0 (Login)
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity0::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ── Navegación inferior ───────────────────────────────────────
        btnNavHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        btnNavVuelos.setOnClickListener {
            startActivity(Intent(this, registro_vuelo::class.java))
        }

        btnNavAeronaves.setOnClickListener {
            // Ya estás en esta pantalla, no hace nada
        }

        btnNavMantenimiento.setOnClickListener {
            startActivity(Intent(this, gestion_mantenimiento::class.java))
        }

        btnNavPerfil.setOnClickListener {
            startActivity(Intent(this, gestion_pilotos::class.java))
        }
    }
}