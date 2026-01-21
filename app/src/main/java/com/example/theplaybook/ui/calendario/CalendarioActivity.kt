package com.example.theplaybook.ui.calendario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityCalendarioDetailedBinding
import com.example.theplaybook.databinding.DialogAddEventBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioDetailedBinding
    private val calendar = Calendar.getInstance()
    private val events = mutableListOf<CalendarEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarioDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCurrentMonth()
        setupFab()
        loadMockEvents()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Aggiorna mese corrente
        updateMonthDisplay()
    }

    private fun setupCurrentMonth() {
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ITALIAN)
        binding.tvCurrentMonth.text = monthFormat.format(calendar.time).uppercase()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun loadMockEvents() {
        // Eventi mock per esempio
        events.addAll(listOf(
            CalendarEvent(
                id = 1,
                title = "Uscita GTA VI",
                description = "Data di uscita prevista",
                date = "17/09/2025",
                time = null,
                type = "GAMING",
                gameName = "Grand Theft Auto VI"
            ),
            CalendarEvent(
                id = 2,
                title = "Sessione Co-op",
                description = "Giocare con amici",
                date = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(Date()),
                time = "21:00",
                type = "GAMING",
                gameName = "Counter-Strike 2"
            ),
            CalendarEvent(
                id = 3,
                title = "Dentista",
                description = "Controllo annuale",
                date = "15/12/2024",
                time = "10:30",
                type = "PERSONALE",
                gameName = null
            )
        ))

        Toast.makeText(this, "${events.size} eventi caricati", Toast.LENGTH_SHORT).show()
    }

    private fun showAddEventDialog() {
        val dialogView = DialogAddEventBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuovo Evento")
            .setView(dialogView.root)
            .create()

        // Setup date picker - OBBLIGATORIO
        dialogView.btnStartDate.setOnClickListener {
            showDatePicker(dialogView.btnStartDate)
        }

        // Setup time picker - FACOLTATIVO
        dialogView.btnStartTime.setOnClickListener {
            showTimePicker(dialogView.btnStartTime)
        }

        // Imposta testo iniziale
        dialogView.btnStartDate.text = "Seleziona data *"
        dialogView.btnStartTime.text = "Ora (facoltativa)"

        // Setup radio group per tipo evento
        dialogView.rgEventType.setOnCheckedChangeListener { _, checkedId ->
            val isGaming = checkedId == R.id.rbGaming
            dialogView.layoutGamingOptions.visibility = if (isGaming) View.VISIBLE else View.GONE

            // Popola spinner giochi se gaming
            if (isGaming) {
                val games = arrayOf(
                    "Seleziona gioco",
                    "Counter-Strike 2",
                    "Elden Ring",
                    "Cyberpunk 2077",
                    "Minecraft",
                    "Dota 2",
                    "Grand Theft Auto V",
                    "Apex Legends",
                    "Altro"
                )
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, games)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.spinnerGames.adapter = adapter
            }
        }

        // Bottoni dialog
        dialogView.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.btnSave.setOnClickListener {
            if (validateEvent(dialogView)) {
                saveEvent(dialogView)
                dialog.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun validateEvent(binding: DialogAddEventBinding): Boolean {
        val eventName = binding.etEventName.text.toString().trim()
        val date = binding.btnStartDate.text.toString()

        // Validazione campi obbligatori
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Inserisci un titolo per l'evento", Toast.LENGTH_SHORT).show()
            return false
        }

        if (date == "Seleziona data *" || date.isEmpty()) {
            Toast.makeText(this, "Seleziona una data", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showDatePicker(button: android.widget.Button) {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
                button.text = format.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker(button: android.widget.Button) {
        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedTime.set(Calendar.MINUTE, minute)
                val format = SimpleDateFormat("HH:mm", Locale.ITALIAN)
                button.text = format.format(selectedTime.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun saveEvent(binding: DialogAddEventBinding) {
        val eventName = binding.etEventName.text.toString().trim()
        val startDate = binding.btnStartDate.text.toString()
        val startTime = if (binding.btnStartTime.text != "Ora (facoltativa)") {
            binding.btnStartTime.text.toString()
        } else {
            null
        }

        val eventType = if (binding.rbGaming.isChecked) "GAMING" else "PERSONALE"

        val game = if (binding.rbGaming.isChecked && binding.spinnerGames.selectedItemPosition > 0) {
            binding.spinnerGames.selectedItem.toString()
        } else {
            null
        }

        val notes = binding.etNotes.text.toString().trim()

        // Crea nuovo evento
        val newEvent = CalendarEvent(
            id = events.size + 1,
            title = eventName,
            description = notes,
            date = startDate,
            time = startTime,
            type = eventType,
            gameName = game
        )

        events.add(newEvent)

        // Mostra conferma
        val timeText = if (startTime != null) " alle $startTime" else ""
        val gameText = if (game != null) " per $game" else ""
        val message = "Evento salvato: $eventName il $startDate$timeText$gameText"

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Qui potresti salvare nel database
        // saveToDatabase(newEvent)
    }

    // Aggiungi questa import se manca
    // import android.view.View

    data class CalendarEvent(
        val id: Int,
        val title: String,
        val description: String?,
        val date: String, // Formato: dd/MM/yyyy
        val time: String?, // Formato: HH:mm (facoltativo)
        val type: String, // "GAMING" o "PERSONALE"
        val gameName: String? // Solo per eventi gaming
    ) {
        val displayDate: String
            get() = if (time != null) {
                "$date - $time"
            } else {
                date
            }
    }
}