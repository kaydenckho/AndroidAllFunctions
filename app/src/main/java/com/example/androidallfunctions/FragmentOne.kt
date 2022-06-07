package com.example.androidallfunctions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.androidallfunctions.databinding.FragmentOneLayoutBinding

class FragmentOne : Fragment() {

    private val binding by lazy { FragmentOneLayoutBinding.inflate(layoutInflater) }
    var callback: OnDayNightStateChanged? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDayNightStateChanged) {
            callback = context
        } else {
            throw RuntimeException("$context must implement SampleCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        binding.switcher.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switcher.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                binding.parent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
//                callback?.onDayNightApplied(OnDayNightStateChanged.NIGHT)
//            } else {
//                binding.parent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
//                callback?.onDayNightApplied(OnDayNightStateChanged.DAY)
//            }
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}