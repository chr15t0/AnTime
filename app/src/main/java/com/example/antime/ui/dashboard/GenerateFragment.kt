package com.example.antime.ui.dashboard

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.antime.R
import com.example.antime.algorithm.RankBasedAS
import com.example.antime.databinding.FormAddActivityBinding
import com.example.antime.algorithm.Activities
import com.example.antime.algorithm.Assignment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.antime.algorithm.DailySchedule
import com.example.antime.algorithm.Schedule
import com.example.antime.databinding.FormAddMeetingTimeBinding
import com.example.antime.databinding.FragmentGenerateBinding
import com.example.antime.ui.detailDailyActivities.ScheduleAdapter

class GenerateFragment : Fragment() {

    private var _binding: FragmentGenerateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val listActivities = ArrayList<Activities>()
    private val listMeetings = HashMap<String,MutableList<Int>>()
    private lateinit var auth : FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var dialog :Dialog
    private var numberOfActivities = 0
    private var numberOfMeetings=0
    private var maxActivities = 40
    private var maxMeetings = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentGenerateBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = Firebase.auth
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = auth.currentUser

        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        binding.btnAddActivities.setOnClickListener{
            if (numberOfActivities < maxActivities) {
                addActivity()
                numberOfActivities++
            } else {
                Toast.makeText(requireContext(), "Maximum activities reached!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnAddMeeting.setOnClickListener{
            if (numberOfMeetings < maxMeetings) {
                addMeeting()
                maxActivities -= 2
                numberOfMeetings++
                if (maxActivities < 0) maxActivities = 0
            } else {
                Toast.makeText(requireContext(), "Maximum meetings reached!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGenerateSchedule.setOnClickListener{
            if(checkifValidandRead()){
                val scheduler = RankBasedAS(listActivities,listMeetings)
                val resultRankedAS = scheduler.schedule()
                saveToFirestore(user,resultRankedAS)
                Log.d("GlobalBest: ",resultRankedAS.toString())
                resultRankedAS.forEach {
                    Log.d("ASRANK_RESULT", "${it.activities.prodi} - ${it.activities.pic}/${it.activities.programmer} scheduled on ${it.day} at ${it.startHour} in ${it.room}")
                }

                Toast.makeText(requireContext(), "Scheduling done!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMeeting() {
        val listBindingMeeting = FormAddMeetingTimeBinding.inflate(layoutInflater)
        val spinnerDay: Spinner = listBindingMeeting.spinnerMeetingDay
        spinnerDay.adapter = setAdapter(R.array.day_array)

        val spinnerHour: Spinner = listBindingMeeting.spinnerMeetingHour
        spinnerHour.adapter = setAdapter(R.array.hour_array)

        val btnDelete = listBindingMeeting.btnDelete
        btnDelete.setOnClickListener{
            binding.layoutListMeetings.removeView(listBindingMeeting.root)
            maxActivities += 2
            numberOfMeetings--
        }
        binding.layoutListMeetings.addView(listBindingMeeting.root)

    }

    private fun addActivity() {
        val listBinding = FormAddActivityBinding.inflate(layoutInflater)

        val spinnerProdi: Spinner = listBinding.spinnerProdi
        spinnerProdi.adapter = setAdapter(R.array.prodi_array)

        val spinnerPIC = listBinding.spinnerPIC
        spinnerPIC.adapter = setAdapter(R.array.pic_array)

        val spinnerProgrammer = listBinding.spinnerProgrammer
        spinnerProgrammer.adapter = setAdapter(R.array.programmer_array)

        val btnDelete = listBinding.btnDelete
        btnDelete.setOnClickListener{
            binding.layoutListActivities.removeView(listBinding.root)
            numberOfActivities--
        }

        binding.layoutListActivities.addView(listBinding.root)
    }

    private fun setAdapter(arrayList: Int): SpinnerAdapter {
        val aa = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,resources.getStringArray(arrayList))
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return aa
    }

    private fun checkifValidandRead(): Boolean {
        listActivities.clear()
        listMeetings.clear()
        var result = true
        if (binding.layoutListActivities.childCount == null || binding.layoutListActivities.isEmpty()){
            Log.e("Validation", "No activities found in layoutListActivities")
            Toast.makeText(requireContext(), "Please add at least one activity.", Toast.LENGTH_LONG).show()
            return false
        }

        for(i in 0 until  binding.layoutListActivities.childCount){
            val activities= binding.layoutListActivities.getChildAt(i)
            val prodi = activities.findViewById<Spinner>(R.id.spinnerProdi)
            val pic = activities.findViewById<Spinner>(R.id.spinnerPIC)
            val programmer = activities.findViewById<Spinner>(R.id.spinnerProgrammer)

            if(prodi.selectedItemPosition==0 || pic.selectedItemPosition==0 || programmer.selectedItemPosition==0 ) {
                Log.e("Validation", "Nothing has been selected at list $i")
                Toast.makeText(requireContext(),"Make sure all fields have been selected at list ${i+1}",Toast.LENGTH_LONG).show()
                result = false
            }

            listActivities.add(
                Activities(
                prodi.selectedItem.toString(),
                pic.selectedItem.toString(),
                programmer.selectedItem.toString()
            )
            )
            Log.d("test",listActivities.toString())
        }

        if (binding.layoutListMeetings.childCount>0 || binding.layoutListMeetings.isNotEmpty()){
            for(i in 0 until  binding.layoutListMeetings.childCount){
                val meetings= binding.layoutListMeetings.getChildAt(i)
                val day = meetings.findViewById<Spinner>(R.id.spinnerMeetingDay)
                val hour = meetings.findViewById<Spinner>(R.id.spinnerMeetingHour)

                if(day.selectedItemPosition==0 || hour.selectedItemPosition==0) {
                    Log.e("Validation", "Nothing has been selected at list $i")
                    Toast.makeText(requireContext(),"Make sure all fields have been selected at list ${i+1}",Toast.LENGTH_LONG).show()
                    result = false
                }

                if (listMeetings[day.selectedItem.toString()]==null){
                    listMeetings[day.selectedItem.toString()] = mutableListOf(hour.selectedItem.toString().split(":")[0].toInt())
                }else{
                    listMeetings[day.selectedItem.toString()]?.add(hour.selectedItem.toString().split(":")[0].toInt())
                }
                Log.d("test",listMeetings.toString())
            }
        }

        return result
    }

    private fun saveToFirestore(user: FirebaseUser?, resultRankedAS : List<Assignment>) {
        val activitiesMap = hashMapOf<String,ArrayList<Map<String,String>>>()

        for (activity in resultRankedAS){
            val activityData = mapOf(
                "Prodi" to activity.activities.prodi,
                "Pic" to activity.activities.pic,
                "Programmer" to activity.activities.programmer,
                "Room" to activity.room,
                "Start Hour" to activity.startHour.toString() + ":00",
                "End Hour" to (activity.startHour + 2).toString() + ":00"
            )
            if(!activitiesMap.containsKey(activity.day)){
                activitiesMap[activity.day]= ArrayList()
            }
            activitiesMap[activity.day]?.add(activityData)
        }

        val schedule = hashMapOf(
            "Activities" to activitiesMap
        )
        db.collection("users").document(user?.uid.toString())
            .collection("schedules").document("daily schedule").set(schedule)
            .addOnSuccessListener {
                Log.d("Store Schedule", "Successfully added Schedule to user's Firestore document")
                showDialogSchedule(user)
            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error adding data to Firestore document", e)
                Toast.makeText(requireContext(), "Failed to save Schedule", Toast.LENGTH_LONG).show()
            }

    }

    private fun showDialogSchedule(user: FirebaseUser?) {
        dialog.setContentView(R.layout.dialog_schedule)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val btnOk : Button = dialog.findViewById(R.id.btn_ok)
        btnOk.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_generate_to_navigation_home)
            dialog.dismiss()
        }

        db.collection("users").document(user?.uid.toString())
            .collection("schedules").document("daily schedule").get()
            .addOnSuccessListener { document ->
                // Directly retrieve activities without checking document existence
                var activitiesMap = document.get("Activities") as? Map<String, ArrayList<Map<String, String>>> ?: emptyMap()
                val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                val timeOrder = listOf("8:00","10:00","13:00","15:00")
                val roomOrder = listOf("Ruangan 1 Ditgrasi ","Ruangan 2 Ditgrasi")
                val sortedActivities = activitiesMap.toSortedMap(compareBy ({daysOrder.indexOf(it)},{ timeOrder.indexOf(it) }, {roomOrder.indexOf(it)}))
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
                val mondayAdapter = ScheduleAdapter()
                val tuesdayAdapter = ScheduleAdapter()
                val wednesdayAdapter = ScheduleAdapter()
                val thursdayAdapter = ScheduleAdapter()
                val fridayAdapter = ScheduleAdapter()

                val rv_monday: RecyclerView = dialog.findViewById(R.id.rv_scheduleMonday)
                val rv_tuesday: RecyclerView = dialog.findViewById(R.id.rv_scheduleTuesday)
                val rv_wednesday: RecyclerView = dialog.findViewById(R.id.rv_scheduleWednesday)
                val rv_thursday: RecyclerView = dialog.findViewById(R.id.rv_scheduleThursday)
                val rv_friday: RecyclerView = dialog.findViewById(R.id.rv_scheduleFriday)

                rv_monday.adapter = mondayAdapter
                rv_tuesday.adapter = tuesdayAdapter
                rv_wednesday.adapter = wednesdayAdapter
                rv_thursday.adapter = thursdayAdapter
                rv_friday.adapter = fridayAdapter

                for (i in adapterData) {
                    when (i.day) {
                        "Monday" -> mondayAdapter.submitList(i.activities)
                        "Tuesday" -> tuesdayAdapter.submitList(i.activities)
                        "Wednesday" -> wednesdayAdapter.submitList(i.activities)
                        "Thursday" -> thursdayAdapter.submitList(i.activities)
                        "Friday" -> fridayAdapter.submitList(i.activities)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch schedule", e)
                Toast.makeText(requireContext(), "Error fetching schedule", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}