package com.example.antime.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.antime.R
import com.example.antime.algorithm.RankBasedAS
import com.example.antime.databinding.FragmentHomeBinding
import com.example.antime.algorithm.Activities
import com.example.antime.algorithm.DailySchedule
import com.example.antime.ui.detailDailyActivities.DetailDailyActivity
import com.example.antime.algorithm.Schedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private val schedule = mutableListOf<Schedule>()


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
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch user", e)
                Toast.makeText(requireContext(), "Error fetching schedule", Toast.LENGTH_LONG).show()
            }


        db.collection("users").document(user?.uid.toString())
            .collection("schedules").document("daily schedule").get()
            .addOnSuccessListener { document ->
                // Directly retrieve activities without checking document existence
                var activitiesMap = document.get("Activities") as? Map<String, ArrayList<Map<String, String>>> ?: emptyMap()
                val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                val sortedActivities = activitiesMap.toSortedMap(compareBy { daysOrder.indexOf(it)})
                    .mapValues { entry ->
                        entry.value.map { activity ->
                            val prodi = activity["Prodi"] ?: "Unknown"
                            val pic = activity["Pic"] ?: "Unknown"
                            val programmer = activity["Programmer"] ?: "Unknown"
                            val room = activity["Room"] ?: "Unknown"
                            val startHour = activity["Start Hour"] ?: "Unknown"
                            val endHour = activity["End Hour"] ?: "Unknown"
                            Schedule(entry.key, prodi, pic, programmer, room, startHour, endHour)
                        }
                    }
                val adapterData = sortedActivities.map { (day,schedule)->
                    DailySchedule(
                        day = day,
                        activities = schedule
                    )
                }
                setAdapterRecyclerView(adapterData)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch schedule", e)
                Toast.makeText(requireContext(), "Error fetching schedule", Toast.LENGTH_LONG).show()
            }
        binding.btnTest.setOnClickListener{
//            val textTest= "Lisa blackpink is my bias"
//            val detailDailyActivitiesIntent = Intent(requireContext(), DetailDailyActivity::class.java)
//            detailDailyActivitiesIntent.putExtra(DetailDailyActivity.EXTRA_TEXT,textTest.toString())
//            startActivity(detailDailyActivitiesIntent)


            val testActivities = generateTestActivities()
            val scheduler = RankBasedAS(testActivities)
            val results = scheduler.schedule()

            Log.d("GlobalBest: ",results.toString())

            results.forEach {
                Log.d("ASRANK_RESULT", "${it.activities.prodi} - ${it.activities.pic}/${it.activities.programmer} scheduled on ${it.day} at ${it.startHour} in ${it.room}")
            }
//
            Toast.makeText(requireContext(), "Scheduling done! Check Logcat.", Toast.LENGTH_SHORT).show()
//            Log.d("tanggal hari ini", LocalDate.now().toString())
        }
        val date = LocalDate.now().toString()
        val formatted = date.withDateFormat()
        binding.textDate.text = formatted
    }

    private fun setAdapterRecyclerView(adapterData: List<DailySchedule>) {
        val adapter = DaysAdapter()
        adapter.submitList(adapterData)
        binding.recyclerViewDays.adapter = adapter
        adapter.setOnItemClickCallback(object :DaysAdapter.OnItemClickCallback{
            override fun onItemClicked(schedule: DailySchedule, options: ActivityOptionsCompat) {
                val scheduleIntent = Intent(requireContext(), DetailDailyActivity::class.java)
                scheduleIntent.putExtra("SCHEDULE",schedule.day)
                scheduleIntent.putParcelableArrayListExtra("SCHEDULE_LIST",
                    ArrayList(schedule.activities)
                )
                startActivity(scheduleIntent,options.toBundle())
            }
        })
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