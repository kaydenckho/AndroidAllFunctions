package com.example.androidallfunctions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_PAGES = 2

class ViewPagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> return ShortVideoDetailFragment()
            2 -> {
                return ShortVideoDetailFragment()
            }
            else -> return ShortVideoDetailFragment()
        }
    }


}