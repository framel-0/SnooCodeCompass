package com.tinydavid.snoocodecompass.ui.home

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tinydavid.snoocodecompass.databinding.ActivityHomeBinding

class HomeActivity : FragmentActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}