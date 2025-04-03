package com.example.antime.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.antime.databinding.FragmentHomeBinding
import com.example.antime.ui.DetailDailyActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = Firebase.auth
//        val username = db.runTransaction{ it.get(db.collection("users").document(user?.uid.toString())).getString("username") }

//        Toast.makeText(requireContext(), username.toString(),Toast.LENGTH_LONG).show()
//        binding.textUsername.text = username.toString()

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = auth.currentUser
        val db = Firebase.firestore
        db.collection("users").document(user?.uid.toString()).get()
            .addOnSuccessListener {document->
                binding.textUsername.text = document.getString("username")
            }
        binding.btnTest.setOnClickListener{
            val textTest= "Lisa blackpink is my bias"
            val detailDailyActivitiesIntent = Intent(requireContext(), DetailDailyActivity::class.java)
            detailDailyActivitiesIntent.putExtra(DetailDailyActivity.EXTRA_TEXT,textTest.toString())
            startActivity(detailDailyActivitiesIntent)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}