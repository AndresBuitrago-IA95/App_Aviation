package com.example.myapplicationproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
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

class gestion_mantenimiento : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Listas para spinners dinámicos
    private val listaAeronaves   = mutableListOf<String>()
    private val idsAeronaves     = mutableListOf<String>()
    private val listaTipos       = mutableListOf<String>()
    private val idsTipos         = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestion_mantenimiento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Inicializar Firebase ──────────────────────────────────────
        database = FirebaseDatabase.getInstance().reference
        auth     = FirebaseAuth.getInstance()

        // ── Referencias de UI ─────────────────────────────────────────
        val btnCerrarSesion         = findViewById<Button>(R.id.btnCerrarSesion)
        val btnPerfilIcono          = findViewById<ImageButton>(R.id.btnPerfil)

        val spinnerAeronave         = findViewById<Spinner>(R.id.spinnerAeronave)
        val spinnerTipoInspeccion   = findViewById<Spinner>(R.id.spinnerTipoInspeccion)
        val etFechaProgramada       = findViewById<EditText>(R.id.etFechaProgramada)
        val etTallerMecanico        = findViewById<EditText>(R.id.etTallerMecanico)
        val btnGuardarMantenimiento = findViewById<Button>(R.id.btnGuardarMantenimiento)

        val etInformeMecanico       = findViewById<EditText>(R.id.etInformeMecanico)
        val btnProcesarInforme      = findViewById<Button>(R.id.btnProcesarInforme)

        val tvInfoAeronave          = findViewById<TextView>(R.id.tvInfoAeronave)
        val tvInfoInspeccion        = findViewById<TextView>(R.id.tvInfoInspeccion)
        val chkMantenimientoLinea   = findViewById<CheckBox>(R.id.chkMantenimientoLinea)
        val chkCiclosVuelo          = findViewById<CheckBox>(R.id.chkCiclosVuelo)
        val chkHorasVuelo           = findViewById<CheckBox>(R.id.chkHorasVuelo)
        val tvResumenInforme        = findViewById<TextView>(R.id.tvResumenInforme)
        val btnLiberarAeronave      = findViewById<Button>(R.id.btnLiberarAeronave)

        // ── Cargar Aeronaves desde Firebase ──────────────────────────
        database.child("aeronaves").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaAeronaves.clear()
                idsAeronaves.clear()
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
                    this@gestion_mantenimiento,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaAeronaves
                )
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@gestion_mantenimiento,
                    "Error cargando aeronaves: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // ── Cargar Tipos de Inspección desde Firebase ─────────────────
        // Nodo: tipos_mantenimiento → { nombre: "100 horas" }
        database.child("tipos_mantenimiento").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTipos.clear()
                idsTipos.clear()
                listaTipos.add("Seleccione tipo")
                idsTipos.add("")
                for (tipo in snapshot.children) {
                    val nombre = tipo.child("nombre").getValue(String::class.java)
                    if (nombre != null) {
                        listaTipos.add(nombre)
                        idsTipos.add(tipo.key ?: "")
                    }
                }
                // Si no hay datos en Firebase, usar lista de respaldo
                if (listaTipos.size == 1) {
                    listaTipos.addAll(listOf("100 horas", "Anual", "Preventiva", "Correctiva"))
                }
                spinnerTipoInspeccion.adapter = ArrayAdapter(
                    this@gestion_mantenimiento,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaTipos
                )
            }
            override fun onCancelled(error: DatabaseError) {
                // Fallback con lista estática
                val fallback = listOf("Seleccione tipo", "100 horas", "Anual", "Preventiva", "Correctiva")
                spinnerTipoInspeccion.adapter = ArrayAdapter(
                    this@gestion_mantenimiento,
                    android.R.layout.simple_spinner_dropdown_item,
                    fallback
                )
            }
        })

        // ── Actualizar info del checklist al cambiar spinner ──────────
        spinnerAeronave.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) tvInfoAeronave.text = "Aeronave: ${listaAeronaves[pos]}"
                else tvInfoAeronave.text = "Aeronave: —"
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerTipoInspeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) tvInfoInspeccion.text = "Tipo de inspección: ${listaTipos[pos]}"
                else tvInfoInspeccion.text = "Tipo de inspección: —"
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ── Botón Guardar Mantenimiento ───────────────────────────────
        btnGuardarMantenimiento.setOnClickListener {
            val fecha  = etFechaProgramada.text.toString().trim()
            val taller = etTallerMecanico.text.toString().trim()

            when {
                spinnerAeronave.selectedItemPosition == 0 -> {
                    Toast.makeText(this, "Selecciona una aeronave", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                spinnerTipoInspeccion.selectedItemPosition == 0 -> {
                    Toast.makeText(this, "Selecciona el tipo de inspección", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                fecha.isEmpty() -> {
                    etFechaProgramada.error = "Ingresa la fecha programada"
                    etFechaProgramada.requestFocus()
                    return@setOnClickListener
                }
                taller.isEmpty() -> {
                    etTallerMecanico.error = "Ingresa el taller o mecánico"
                    etTallerMecanico.requestFocus()
                    return@setOnClickListener
                }
            }

            val idAeronave = idsAeronaves[spinnerAeronave.selectedItemPosition]
            val matricula  = listaAeronaves[spinnerAeronave.selectedItemPosition]
            val tipoInsp   = listaTipos[spinnerTipoInspeccion.selectedItemPosition]

            val mantenimientoMap = hashMapOf(
                "id_aeronave"       to idAeronave,
                "matricula"         to matricula,
                "tipo_inspeccion"   to tipoInsp,
                "fecha_programada"  to fecha,
                "taller_mecanico"   to taller,
                "estado"            to "Programado"
            )

            database.child("mantenimientos").push().setValue(mantenimientoMap)
                .addOnSuccessListener {
                    Toast.makeText(this,
                        "✅ Mantenimiento guardado correctamente", Toast.LENGTH_SHORT).show()
                    etFechaProgramada.text.clear()
                    etTallerMecanico.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "❌ Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // ── Botón Procesar Informe del Mecánico ───────────────────────
        // Lee el texto del informe y marca automáticamente los checkboxes
        // según las palabras clave que encuentre
        btnProcesarInforme.setOnClickListener {
            val informe = etInformeMecanico.text.toString().lowercase()

            if (informe.isBlank()) {
                etInformeMecanico.error = "Escribe el informe del mecánico"
                etInformeMecanico.requestFocus()
                return@setOnClickListener
            }

            // Palabras clave para cada ítem del checklist
            val claveMantenimientoLinea = listOf(
                "mantenimiento en línea", "mantenimiento en linea",
                "línea", "linea", "mantenimiento línea"
            )
            val claveCiclosVuelo = listOf(
                "ciclos de vuelo", "ciclos vuelo", "ciclos", "ciclo de vuelo"
            )
            val claveHorasVuelo = listOf(
                "horas de vuelo", "horas vuelo", "horas de vuel",
                "horas totales", "hobbs", "tiempo de vuelo"
            )

            // Marcar automáticamente según palabras clave encontradas
            val encontroMantenimiento = claveMantenimientoLinea.any { informe.contains(it) }
            val encontroCiclos        = claveCiclosVuelo.any { informe.contains(it) }
            val encontroHoras         = claveHorasVuelo.any { informe.contains(it) }

            chkMantenimientoLinea.isChecked = encontroMantenimiento
            chkCiclosVuelo.isChecked        = encontroCiclos
            chkHorasVuelo.isChecked         = encontroHoras

            // Construir resumen de lo detectado
            val itemsMarcados = mutableListOf<String>()
            if (encontroMantenimiento) itemsMarcados.add("Mantenimiento en línea")
            if (encontroCiclos)        itemsMarcados.add("Ciclos de vuelo")
            if (encontroHoras)         itemsMarcados.add("Horas de vuelo")

            if (itemsMarcados.isEmpty()) {
                tvResumenInforme.text = "⚠️ No se detectaron ítems del checklist en el informe. Puedes marcarlos manualmente."
            } else {
                tvResumenInforme.text = "✅ Ítems detectados en el informe: ${itemsMarcados.joinToString(", ")}"
            }
            tvResumenInforme.visibility = View.VISIBLE

            Toast.makeText(this, "Informe procesado correctamente", Toast.LENGTH_SHORT).show()
        }

        // ── Botón Liberar Aeronave ────────────────────────────────────
        btnLiberarAeronave.setOnClickListener {
            val todosMarcados = chkMantenimientoLinea.isChecked &&
                    chkCiclosVuelo.isChecked &&
                    chkHorasVuelo.isChecked

            if (!todosMarcados) {
                Toast.makeText(this,
                    "⚠️ Completa todos los ítems del checklist antes de liberar la aeronave",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Actualizar estado de la aeronave a Disponible en Firebase
            val pos = spinnerAeronave.selectedItemPosition
            if (pos > 0) {
                val idAeronave = idsAeronaves[pos]
                database.child("aeronaves").child(idAeronave)
                    .child("estado").setValue("Disponible")
                    .addOnSuccessListener {
                        Toast.makeText(this,
                            "✅ Aeronave ${listaAeronaves[pos]} liberada para vuelo",
                            Toast.LENGTH_LONG).show()
                        // Resetear checklist
                        chkMantenimientoLinea.isChecked = false
                        chkCiclosVuelo.isChecked        = false
                        chkHorasVuelo.isChecked         = false
                        tvResumenInforme.visibility     = View.GONE
                        etInformeMecanico.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this,
                            "❌ Error al liberar aeronave: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Selecciona una aeronave primero", Toast.LENGTH_SHORT).show()
            }
        }

        // ── Header ────────────────────────────────────────────────────
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity0::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnPerfilIcono.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }

        // ── Navegación inferior ───────────────────────────────────────
        findViewById<ImageButton>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavVuelos).setOnClickListener {
            val intent = Intent(this, registro_vuelo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavAeronaves).setOnClickListener {
            val intent = Intent(this, flota_aeronave::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btnNavConfiguracion).setOnClickListener {
            // Ya estás en esta pantalla
        }
        findViewById<ImageButton>(R.id.btnNavPerfil).setOnClickListener {
            val intent = Intent(this, gestion_pilotos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}