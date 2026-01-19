package com.example.theplaybook.ui.dashboard.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.theplaybook.ui.dashboard.fragments.*

class DashboardPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OverviewFragment()
            1 -> GamesFragment()
            2 -> AchievementsFragment()
            3 -> StatsFragment()
            else -> OverviewFragment()
        }
    }
}