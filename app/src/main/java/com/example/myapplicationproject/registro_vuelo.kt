package com.example.myapplicationproject

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class registro_vuelo : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Listas para los spinners
    private val listaAeronaves = mutableListOf<String>()
    private val idsAeronaves   = mutableListOf<String>()
    private val listaPilotos   = mutableListOf<String>()
    private val idsPilotos     = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_vuelo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Inicializar Firebase ──────────────────────────────────────
        database = FirebaseDatabase.getInstance().reference
        auth     = FirebaseAuth.getInstance()

        // ── Referencias de vistas ─────────────────────────────────────
        val spinnerAeronave = findViewById<Spinner>(R.id.spinnerAeronave)
        val spinnerPiloto   = findViewById<Spinner>(R.id.spinnerPiloto)
        val spinnerEstado   = findViewById<Spinner>(R.id.spinnerEstado)
        val etFecha         = findViewById<EditText>(R.id.etFecha)
        val etHobbsInicial  = findViewById<EditText>(R.id.etHobbsInicial)
        val etHobbsFinal    = findViewById<EditText>(R.id.etHobbsFinal)
        val btnRegistrar    = findViewById<Button>(R.id.btnRegistrarVuelo)
        val tablaVuelos     = findViewById<LinearLayout>(R.id.tablaVuelos)

        // ── Header ────────────────────────────────────────────────────
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity0::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ── Spinner Estado: opciones fijas ────────────────────────────
        val estadosVuelo = listOf("Completado", "En curso", "Cancelado", "Demorado")
        val adapterEstado = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            estadosVuelo
        )
        spinnerEstado.adapter = adapterEstado

        // ── Cargar Aeronaves desde Firebase ──────────────────────────
        database.child("aeronaves").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaAeronaves.clear()
                idsAeronaves.clear()
                // Opción inicial de selección
                listaAeronaves.add("Seleccione aeronave")
                idsAeronaves.add("")
                for (aeronave in snapshot.children) {
                    val matricula = aeronave.child("matricula").getValue(String::class.java)
                    if (matricula != null) {
                        listaAeronaves.add(matricula)
                        idsAeronaves.add(aeronave.key ?: "")
                    }
                }
                spinnerAeronave.adapter = ArrayAdapter(
                    this@registro_vuelo,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaAeronaves
                )
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@registro_vuelo,
                    "Error cargando aeronaves: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // ── Cargar Pilotos desde Firebase ────────────────────────────
        database.child("pilotos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaPilotos.clear()
                idsPilotos.clear()
                // Opción inicial de selección
                listaPilotos.add("Seleccione piloto")
                idsPilotos.add("")
                for (piloto in snapshot.children) {
                    val nombre   = piloto.child("nombre").getValue(String::class.java) ?: ""
                    val apellido = piloto.child("apellido").getValue(String::class.java) ?: ""
                    listaPilotos.add("$nombre $apellido".trim())
                    idsPilotos.add(piloto.key ?: "")
                }
                spinnerPiloto.adapter = ArrayAdapter(
                    this@registro_vuelo,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaPilotos
                )
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@registro_vuelo,
                    "Error cargando pilotos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // ── Cargar tabla de vuelos desde Firebase ─────────────────────
        cargarTablaVuelos(tablaVuelos)

        // ── Botón Registrar Vuelo ─────────────────────────────────────
        btnRegistrar.setOnClickListener {

            val fecha       = etFecha.text.toString().trim()
            val hInicialStr = etHobbsInicial.text.toString().trim()
            val hFinalStr   = etHobbsFinal.text.toString().trim()
            val estado      = spinnerEstado.selectedItem?.toString() ?: "Completado"

            // Validaciones
            when {
                fecha.isEmpty() -> {
                    etFecha.error = "Ingresa la fecha del vuelo"
                    etFecha.requestFocus()
                    return@setOnClickListener
                }
                hInicialStr.isEmpty() -> {
                    etHobbsInicial.error = "Ingresa el Hobbs inicial"
                    etHobbsInicial.requestFocus()
                    return@setOnClickListener
                }
                hFinalStr.isEmpty() -> {
                    etHobbsFinal.error = "Ingresa el Hobbs final"
                    etHobbsFinal.requestFocus()
                    return@setOnClickListener
                }
                spinnerAeronave.selectedItemPosition == 0 -> {
                    Toast.makeText(this, "Selecciona una aeronave", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                spinnerPiloto.selectedItemPosition == 0 -> {
                    Toast.makeText(this, "Selecciona un piloto", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val hInicial = hInicialStr.toDoubleOrNull()
            val hFinal   = hFinalStr.toDoubleOrNull()

            if (hInicial == null || hFinal == null) {
                Toast.makeText(this, "Los valores Hobbs deben ser numéricos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hFinal < hInicial) {
                Toast.makeText(this, "Hobbs final debe ser mayor al inicial", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tiempoVuelo = hFinal - hInicial
            val idAeronave  = idsAeronaves[spinnerAeronave.selectedItemPosition]
            val idPiloto    = idsPilotos[spinnerPiloto.selectedItemPosition]
            val matricula   = listaAeronaves[spinnerAeronave.selectedItemPosition]

            val vueloMap = hashMapOf(
                "id_aeronave"   to idAeronave,
                "matricula"     to matricula,
                "id_piloto"     to idPiloto,
                "fecha_vuelo"   to fecha,
                "hobbs_inicial" to hInicial,
                "hobbs_final"   to hFinal,
                "tiempo_vuelo"  to tiempoVuelo,
                "ciclos_vuelo"  to 1,
                "estado"        to estado
            )

            // ── Guardar en Firebase ───────────────────────────────────
            database.child("vuelos").push().setValue(vueloMap)
                .addOnSuccessListener {
                    actualizarHorasAeronave(idAeronave, tiempoVuelo)
                    Toast.makeText(
                        this,
                        "✅ Vuelo registrado: %.1f horas".format(tiempoVuelo),
                        Toast.LENGTH_LONG
                    ).show()
                    limpiarCampos(etFecha, etHobbsInicial, etHobbsFinal)
                    // Recargar tabla con el nuevo vuelo
                    cargarTablaVuelos(tablaVuelos)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // ── Navegación inferior ───────────────────────────────────────
        configurarNavegacion()
    }

    // ── Carga los vuelos de Firebase y los pinta en la tabla ─────────
    private fun cargarTablaVuelos(tabla: LinearLayout) {
        tabla.removeAllViews() // Limpiar filas anteriores

        database.child("vuelos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val tvVacio = TextView(this@registro_vuelo).apply {
                        text = "No hay vuelos registrados"
                        textSize = 12f
                        setTextColor(0xFF999999.toInt())
                        gravity = Gravity.CENTER
                        setPadding(0, 16, 0, 16)
                    }
                    tabla.addView(tvVacio)
                    return
                }

                for (vuelo in snapshot.children) {
                    val fecha      = vuelo.child("fecha_vuelo").getValue(String::class.java) ?: "-"
                    val matricula  = vuelo.child("matricula").getValue(String::class.java) ?: "-"
                    val hInicial   = vuelo.child("hobbs_inicial").getValue(Double::class.java) ?: 0.0
                    val hFinal     = vuelo.child("hobbs_final").getValue(Double::class.java) ?: 0.0
                    val tiempo     = vuelo.child("tiempo_vuelo").getValue(Double::class.java) ?: 0.0
                    val estado     = vuelo.child("estado").getValue(String::class.java) ?: "Completado"

                    // Color del estado
                    val colorEstado = when (estado) {
                        "Completado" -> 0xFF4CAF50.toInt()  // Verde
                        "En curso"   -> 0xFF3949AB.toInt()  // Azul
                        "Cancelado"  -> 0xFFF44336.toInt()  // Rojo
                        "Demorado"   -> 0xFFFF9800.toInt()  // Naranja
                        else         -> 0xFF757575.toInt()  // Gris
                    }

                    // Fila de la tabla
                    val fila = LinearLayout(this@registro_vuelo).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(12, 12, 12, 12)
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    // Función para crear celda de texto
                    fun celda(texto: String, peso: Float, negrita: Boolean = false, color: Int = 0xFF333333.toInt()): TextView {
                        return TextView(this@registro_vuelo).apply {
                            this.text = texto
                            textSize = 10f
                            setTextColor(color)
                            gravity = Gravity.CENTER
                            if (negrita) setTypeface(typeface, Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso)
                        }
                    }

                    fila.addView(celda(fecha, 1.2f))
                    fila.addView(celda(matricula, 1f))
                    fila.addView(celda("%.1f h".format(hInicial), 1f))
                    fila.addView(celda("%.1f h".format(hFinal), 1f))
                    fila.addView(celda("%.1f h".format(tiempo), 1f))
                    fila.addView(celda(estado, 1f, negrita = true, color = colorEstado))

                    tabla.addView(fila)

                    // Divider entre filas
                    val divider = View(this@registro_vuelo).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                        )
                        setBackgroundColor(0xFFE0E0E0.toInt())
                    }
                    tabla.addView(divider)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@registro_vuelo,
                    "Error cargando vuelos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ── Actualiza horas_vuelo_totales de la aeronave en Firebase ─────
    private fun actualizarHorasAeronave(idAeronave: String, horasNuevas: Double) {
        val ref = database.child("aeronaves").child(idAeronave)
        ref.child("horas_vuelo_totales")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val horasActuales    = snapshot.getValue(Double::class.java) ?: 0.0
                    val horasActualizadas = horasActuales + horasNuevas
                    ref.child("horas_vuelo_totales").setValue(horasActualizadas)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── Limpiar campos después de registrar ──────────────────────────
    private fun limpiarCampos(vararg campos: EditText) {
        for (campo in campos) campo.text.clear()
    }

    // ── Navegación inferior ───────────────────────────────────────────
    private fun configurarNavegacion() {
        findViewById<ImageButton>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavVuelos).setOnClickListener {
            // Ya estamos en esta pantalla
        }
        findViewById<ImageButton>(R.id.btnNavAeronaves).setOnClickListener {
            val intent = Intent(this, flota_aeronave::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavConfiguracion).setOnClickListener {
            val intent = Intent(this, gestion_mantenimiento::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavPerfil).setOnClickListener {
            val intent = Intent(this, gestion_pilotos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}