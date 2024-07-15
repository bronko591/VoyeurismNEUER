package com.example.voyeurism.fragments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.BuildCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.voyeurism.R
import com.example.voyeurism.activitys.DetailsActivity

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        findPreference<SwitchPreferenceCompat>("check_box_pip")
            ?.setOnPreferenceChangeListener { _, newValue ->
                if(newValue as Boolean){
                    DetailsActivity().isPipMode = true
                    DetailsActivity().checkPIPPermission()
                    Toast.makeText(requireContext(), "Picture in Picture Mode Enabled.", Toast.LENGTH_SHORT).show()
                }else{
                    DetailsActivity().isPipMode = false
                    Toast.makeText(requireContext(), "Picture in Picture Mode Disabled.", Toast.LENGTH_SHORT).show()
                }
                true // Return true if the event is handled.
            }

    }

}