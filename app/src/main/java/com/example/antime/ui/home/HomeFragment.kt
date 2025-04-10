package com.example.antime.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.antime.R
import com.example.antime.algorithm.RankBasedAS
import com.example.antime.databinding.FragmentHomeBinding
import com.example.antime.ui.Activities
import com.example.antime.ui.DetailDailyActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                var username = document.getString("username")?.split(" ")
                binding.textUsername.text = username?.get(0).toString() + "!"
                Glide.with(this)
                    .load(R.drawable.android_neutral_sq_na)
                    .into(binding.imageUser)
            }
        binding.btnTest.setOnClickListener{
//            val textTest= "Lisa blackpink is my bias"
//            val detailDailyActivitiesIntent = Intent(requireContext(), DetailDailyActivity::class.java)
//            detailDailyActivitiesIntent.putExtra(DetailDailyActivity.EXTRA_TEXT,textTest.toString())
//            startActivity(detailDailyActivitiesIntent)


//            val testActivities = generateTestActivities()
//            val scheduler = RankBasedAS(testActivities)
//            val results = scheduler.schedule()
//
//            Log.d("GlobalBest: ",results.toString())
//
//            results.forEach {
//                Log.d("ASRANK_RESULT", "${it.activities.prodi} - ${it.activities.pic}/${it.activities.programmer} scheduled on ${it.day} at ${it.startHour} in ${it.room}")
//            }
//
//            Toast.makeText(requireContext(), "Scheduling done! Check Logcat.", Toast.LENGTH_SHORT).show()
            Log.d("tanggal hari ini", LocalDate.now().toString())
        }
        val date = LocalDate.now().toString()
        val formatted = date.withDateFormat()
        binding.textDate.text = formatted
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateTestActivities(): List<Activities> {
        return listOf(
            Activities("TI", "david", "said"),
            Activities("SI", "christo", "said"), // conflict: same programmer
            Activities("MI", "fenny", "wildan"),
            Activities("TI", "nana", "fathur"),
            Activities("TI", "david", "wildan"), // conflict: same pic
            Activities("SI", "fenny", "said"),   // conflict: same programmer
            Activities("MI", "nana", "wildan"),  // conflict: same programmer
            Activities("TI", "david", "fathur"), // conflict: same pic
            Activities("SI", "christo", "fathur"),
            Activities("MI", "fenny", "said"),   // conflict: same programmer
            Activities("TI", "nana", "wildan"),  // conflict: duplicate
            Activities("SI", "christo", "said") // conflict: same programmer
        )
    }

    private fun String.withDateFormat(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = format.parse(this) as Date
        return DateFormat.getDateInstance(DateFormat.FULL).format(date)
    }
}