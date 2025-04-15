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
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.antime.R
import com.example.antime.algorithm.RankBasedAS
import com.example.antime.databinding.FormAddActivityBinding
import com.example.antime.databinding.FragmentDashboardBinding
import com.example.antime.algorithm.Activities
import com.example.antime.algorithm.Assignment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.core.view.isEmpty

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val listActivities = ArrayList<Activities>()
    private lateinit var auth : FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var dialog :Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
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
            addActivity()
        }

        binding.btnGenerateSchedule.setOnClickListener{
            if(checkifValidandRead()){
                val scheduler = RankBasedAS(listActivities)
                val resultRankedAS = scheduler.schedule()
                saveToFirestore(user,resultRankedAS)
                Log.d("GlobalBest: ",resultRankedAS.toString())
                resultRankedAS.forEach {
                    Log.d("ASRANK_RESULT", "${it.activities.prodi} - ${it.activities.pic}/${it.activities.programmer} scheduled on ${it.day} at ${it.startHour} in ${it.room}")
                }

                Toast.makeText(requireContext(), "Scheduling done! Check Logcat.", Toast.LENGTH_SHORT).show()
            }
        }
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
        db.collection("users").document(user?.uid.toString())
            .collection("schedules").document("daily schedule").get()
            .addOnSuccessListener { document ->
                // Directly retrieve activities without checking document existence
                var activitiesMap = document.get("Activities") as? Map<String, ArrayList<Map<String, String>>> ?: emptyMap()

                val scheduleText = StringBuilder()

                //sort days
                val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                activitiesMap = activitiesMap.toSortedMap(compareBy { daysOrder.indexOf(it) })

                activitiesMap?.forEach { (day, activities) ->
                    scheduleText.append("$day\n")
                    activities.forEach { activity ->
                        val prodi = activity["Prodi"] ?: "Unknown"
                        val pic = activity["Pic"] ?: "Unknown"
                        val programmer = activity["Programmer"] ?: "Unknown"
                        val room = activity["Room"] ?: "Unknown"
                        val startHour = activity["Start Hour"] ?: "Unknown"
                        val endHour = activity["End Hour"] ?: "Unknown"

                        scheduleText.append("Prodi: $prodi\n")
                            .append("PIC: $pic\n")
                            .append("Programmer: $programmer\n")
                            .append("Room: $room\n")
                            .append("Start Hour: $startHour\n")
                            .append("End Hour: $endHour\n\n")
                    }
                }

                // Set the retrieved schedule data into the dialog's TextView
                val textSchedule = dialog.findViewById<TextView>(R.id.textSchedule)
                textSchedule.text = scheduleText.toString()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch schedule", e)
                Toast.makeText(requireContext(), "Error fetching schedule", Toast.LENGTH_LONG).show()
            }

        dialog.setContentView(R.layout.dialog_schedule)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}