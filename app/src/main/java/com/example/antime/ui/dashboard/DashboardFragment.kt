package com.example.antime.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.antime.R
import com.example.antime.databinding.FormAddActivityBinding
import com.example.antime.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddActivities.setOnClickListener{
            addActivity()
        }
    }

    private fun addActivity() {
        val listBinding = FormAddActivityBinding.inflate(layoutInflater)
        val spinnerProdi = listBinding.spinnerProdi
        val spinnerPIC = listBinding.spinnerPIC
        val spinnerProgrammer = listBinding.spinnerProgrammer
        val btnDelete = listBinding.btnDelete
        btnDelete.setOnClickListener{
            binding.layoutListActivities.removeView(listBinding.root)
        }
        binding.layoutListActivities.addView(listBinding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}