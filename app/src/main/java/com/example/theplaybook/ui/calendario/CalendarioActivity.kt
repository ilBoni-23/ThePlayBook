package com.example.theplaybook.ui.calendario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarioDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCurrentMonth()
        setupTimeCells()
        setupFab()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Aggiorna mese corrente
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ITALIAN)
        binding.tvCurrentMonth.text = monthFormat.format(calendar.time).uppercase()
    }

    private fun setupCurrentMonth() {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.ITALIAN)
        binding.tvCurrentMonth.text = monthYearFormat.format(calendar.time).uppercase()
    }

    private fun setupTimeCells() {
        // Setup click listener per tutte le celle
        val cellIds = listOf(
            binding.cellLun8, binding.cellMar8 // Aggiungi tutte le celle...
        )

        cellIds.forEach { cell ->
            cell.setOnClickListener {
                showAddEventDialog()
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun showAddEventDialog() {
        val dialogView = DialogAddEventBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView.root)
            .create()

        // Setup date pickers
        dialogView.btnStartDate.setOnClickListener {
            showDatePicker(dialogView.btnStartDate)
        }

        dialogView.btnStartTime.setOnClickListener {
            showTimePicker(dialogView.btnStartTime)
        }

        // Setup radio group per tipo evento
        dialogView.rgEventType.setOnCheckedChangeListener { _, checkedId ->
            val isGaming = checkedId == R.id.rbGaming
            dialogView.layoutGamingOptions.visibility = if (isGaming) View.VISIBLE else View.GONE

            // Popola spinner giochi se gaming
            if (isGaming) {
                val games = arrayOf("Minecraft", "Call of Duty", "FIFA 24", "Elden Ring", "Cyberpunk 2077")
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
            saveEvent(dialogView)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showDatePicker(button: Button) {
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

    private fun showTimePicker(button: Button) {
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
        val eventName = binding.etEventName.text.toString()
        val startDate = binding.btnStartDate.text.toString()
        val startTime = binding.btnStartTime.text.toString()
        val eventType = if (binding.rbGaming.isChecked) "GAMING" else "PERSONALE"
        val game = if (binding.rbGaming.isChecked) binding.spinnerGames.selectedItem.toString() else null
        val notes = binding.etNotes.text.toString()

        // Qui salveresti l'evento nel database
        Toast.makeText(this, "Evento salvato: $eventName", Toast.LENGTH_SHORT).show()

        // Aggiungi evento alla griglia (visualizzazione)
        addEventToGrid(eventName, startDate, startTime, eventType, game)
    }

    private fun addEventToGrid(name: String, date: String, time: String, type: String, game: String?) {
        // Implementa l'aggiunta visuale dell'evento nella griglia
        // Per ora mostra un toast
        val message = if (type == "GAMING") {
            "Session gaming: $name ($game) il $date alle $time"
        } else {
            "Evento: $name il $date alle $time"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}