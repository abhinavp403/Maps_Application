package com.dev.abhinav.mapsapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.abhinav.mapsapplication.fragment.FavoritesFragment
import com.dev.abhinav.mapsapplication.fragment.MapFragment

class TabPageAdapter(activity: FragmentActivity, private val tabCount: Int) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = tabCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MapFragment()
            1 -> FavoritesFragment()
            else -> MapFragment()
        }
    }
}