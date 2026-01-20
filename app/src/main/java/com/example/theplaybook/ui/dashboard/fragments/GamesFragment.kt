package com.example.theplaybook.ui.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.theplaybook.R
import com.example.theplaybook.ui.dashboard.DashboardViewModel
import com.example.theplaybook.ui.dashboard.adapters.GamesAdapter

class GamesFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)

        recyclerView = view.findViewById(R.id.rvGames)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GamesAdapter()
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.example.theplaybook.ui.dashboard.DashboardUiState.Success -> {
                    // Passa tutti i giochi, non solo quelli recenti
                    adapter.submitList(state.data.recentGames)
                }
                else -> {}
            }
        }
    }
}