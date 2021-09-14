package com.dev.abhinav.mapsapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dev.abhinav.mapsapplication.adapter.TabPageAdapter
import com.google.android.material.tabs.TabLayout

// Called from SignInActivity
class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewpager)
        viewPager.isUserInputEnabled = false

        setUpTabBar()
    }

    // Sets up Tab bar layout for fragments
    private fun setUpTabBar() {
       val adapter = TabPageAdapter(this, tabLayout.tabCount)
        viewPager.adapter = adapter

        // Callback for changing pages
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
                viewPager.adapter!!.notifyDataSetChanged()
            }
        })

        // Callback when tab selection changes
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}