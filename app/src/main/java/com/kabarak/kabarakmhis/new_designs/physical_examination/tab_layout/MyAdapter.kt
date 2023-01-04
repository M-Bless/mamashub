package com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@Suppress("DEPRECATION")
internal class MyAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {

            0 -> { FragmentPhysicalExam1Form() }
            1 -> { FragmentWatchReadings() }

            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }
}