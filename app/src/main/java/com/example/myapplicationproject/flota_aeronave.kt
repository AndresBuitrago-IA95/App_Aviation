package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class flota_aeronave : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val idsAeronaves = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flota_aeronave)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val btnPerfilHeader = findViewById<ImageButton>(R.id.btnPerfil)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarsesion)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        val cardDetalle1 = findViewById<CardView>(R.id.cardDetalle1)
        val cardDetalle2 = findViewById<CardView>(R.id.cardDetalle2)
        val cardDetalle3 = findViewById<CardView>(R.id.cardDetalle3)

        val btnDetalle1 = findViewById<Button>(R.id.btnDetalle1)
        val btnDetalle2 = findViewById<Button>(R.id.btnDetalle2)
        val btnDetalle3 = findViewById<Button>(R.id.btnDetalle3)

        val btnEliminar1 = findViewById<Button>(R.id.btnEliminarAeronave1)
        val btnEliminar2 = findViewById<Button>(R.id.btnEliminarAeronave2)
        val btnEliminar3 = findViewById<Button>(R.id.btnEliminarAeronave3)

        cardDetalle1.visibility = View.GONE
        cardDetalle2.visibility = View.GONE
        cardDetalle3.visibility = View.GONE

        fun mostrarDetalle(card: CardView) {
            cardDetalle1.visibility = View.GONE
            cardDetalle2.visibility = View.GONE
            cardDetalle3.visibility = View.GONE
            card.visibility = View.VISIBLE
            card.post { scrollView.smoothScrollTo(0, card.top) }
        }

        btnDetalle1.setOnClickListener { mostrarDetalle(cardDetalle1) }
        btnDetalle2.setOnClickListener { mostrarDetalle(cardDetalle2) }
        btnDetalle3.setOnClickListener { mostrarDetalle(cardDetalle3) }

        database.child("aeronaves").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idsAeronaves.clear()
                val aeronaves = snapshot.children.toList()
                val cards = listOf(cardDetalle1, cardDetalle2, cardDetalle3)
                val detalleBtns = listOf(btnDetalle1, btnDetalle2, btnDetalle3)
                val eliminarBtns = listOf(btnEliminar1, btnEliminar2, btnEliminar3)

                for (i in 0 until 3) {
                    if (i < aeronaves.size) {
                        val aeronave = aeronaves[i]
                        val id = aeronave.key ?: continue
                        val matricula = aeronave.child("matricula").getValue(String::class.java) ?: "-"
                        val modelo = aeronave.child("modelo").getValue(String::class.java) ?: "-"
                        val fabricante = aeronave.child("fabricante").getValue(String::class.java) ?: "-"
                        val horas = aeronave.child("horas_vuelo_totales").getValue(Double::class.java) ?: 0.0
                        val estado = aeronave.child("estado").getValue(String::class.java) ?: "Disponible"
                        val anio = aeronave.child("anio_fabricacion").getValue(String::class.java) ?: "-"

                        idsAeronaves.add(id)

                        val card = cards[i]
                        card.visibility = View.GONE

                        val layout = card.getChildAt(0) as? android.widget.LinearLayout
                        if (layout != null && layout.childCount >= 5) {
                            (layout.getChildAt(1) as? TextView)?.text = "Matrícula: $matricula"
                            (layout.getChildAt(2) as? TextView)?.text = "Modelo: $modelo  ($fabricante)"
                            (layout.getChildAt(3) as? TextView)?.text = "Horas Totales: %.1f h".format(horas)
                            (layout.getChildAt(4) as? TextView)?.text = "Estado: $estado  |  Año: $anio"
                        }

                        detalleBtns[i].isEnabled = true
                        eliminarBtns[i].isEnabled = true
                    } else {
                        idsAeronaves.add("")
                        detalleBtns[i].isEnabled = false
                        eliminarBtns[i].isEnabled = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.widget.Toast.makeText(
                    this@flota_aeronave,
                    "Error cargando aeronaves: ${error.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        })

        fun eliminarAeronave(index: Int, card: CardView) {
            if (index >= idsAeronaves.size || idsAeronaves[index].isEmpty()) {
                android.widget.Toast.makeText(this, "No hay aeronave en este slot", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            val id = idsAeronaves[index]
            AlertDialog.Builder(this)
                .setTitle("Eliminar Aeronave")
                .setMessage("¿Estás seguro de que deseas eliminar esta aeronave? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    database.child("aeronaves").child(id).removeValue()
                        .addOnSuccessListener {
                            card.visibility = View.GONE
                            android.widget.Toast.makeText(this, "✅ Aeronave eliminada correctamente", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            android.widget.Toast.makeText(this, "❌ Error al eliminar: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnEliminar1.setOnClickListener { eliminarAeronave(0, cardDetalle1) }
        btnEliminar2.setOnClickListener { eliminarAeronave(1, cardDetalle2) }
        btnEliminar3.setOnClickListener { eliminarAeronave(2, cardDetalle3) }

        btnPerfilHeader.setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity0::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btnNavHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavVuelos).setOnClickListener {
            startActivity(Intent(this, registro_vuelo::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavAeronaves).setOnClickListener {
            // Ya estás en esta pantalla
        }
        findViewById<ImageButton>(R.id.btnNavMantenimiento).setOnClickListener {
            startActivity(Intent(this, gestion_mantenimiento::class.java))
        }
        findViewById<ImageButton>(R.id.btnNavPerfil).setOnClickListener {
            startActivity(Intent(this, gestion_pilotos::class.java))
        }
    }
}
