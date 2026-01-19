package com.example.theplaybook.ui.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.theplaybook.R
import com.example.theplaybook.ui.dashboard.DashboardViewModel

class OverviewFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var tvTotalHours: TextView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvRecentGames: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)

        tvTotalHours = view.findViewById(R.id.tvTotalHours)
        tvTotalGames = view.findViewById(R.id.tvTotalGames)
        tvRecentGames = view.findViewById(R.id.tvRecentGames)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.example.theplaybook.ui.dashboard.DashboardUiState.Success -> {
                    updateUI(state.data)
                }
                else -> {}
            }
        }
    }

    private fun updateUI(data: com.example.theplaybook.ui.dashboard.DashboardData) {
        tvTotalHours.text = "%.1f h".format(data.totalPlaytimeHours)
        tvTotalGames.text = data.totalGames.toString()

        tvRecentGames.text = if (data.recentGames.isEmpty()) {
            "Nessun gioco recente"
        } else {
            data.recentGames.joinToString("\n") { game ->
                "• ${game.name} (${game.playtimeForever / 60}h)"
            }
        }
    }
}