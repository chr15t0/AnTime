package com.example.antime.ui.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.antime.R
import com.example.antime.databinding.FragmentProfileBinding
import com.example.antime.ui.login.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var dialog : Dialog
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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

        var titleUpdateForm = ""
        var titleSuccessfullyUpdated = ""
        var descriptionSuccessfullyUpdated = ""
        var dataUpdate = ""

        loadUser(user)

        binding.btnChangeUserame.setOnClickListener{
            titleUpdateForm = "Change Username"
            titleSuccessfullyUpdated = "Username Updated!"
            descriptionSuccessfullyUpdated = "Your username has been successfully updated"
            dataUpdate = "username"
            showDialogUpdate(titleUpdateForm,titleSuccessfullyUpdated,descriptionSuccessfullyUpdated,dataUpdate,user)
        }

        binding.btnChangeEmail.setOnClickListener{
            titleUpdateForm = "Change Email"
            titleSuccessfullyUpdated = "Email Updated!"
            descriptionSuccessfullyUpdated = "Your email has been successfully updated"
            dataUpdate = "email"
            showDialogUpdate(titleUpdateForm,titleSuccessfullyUpdated,descriptionSuccessfullyUpdated,dataUpdate,user)
        }

        binding.btnChangePassword.setOnClickListener{
            titleUpdateForm = "Change Password"
            titleSuccessfullyUpdated = "Password Updated!"
            descriptionSuccessfullyUpdated = "Your password has been successfully updated"
            dataUpdate = "password"
            showDialogUpdate(titleUpdateForm,titleSuccessfullyUpdated,descriptionSuccessfullyUpdated,dataUpdate,user)
        }

        binding.btnLogout.setOnClickListener{
            lifecycleScope.launch {
                val credentialManager = CredentialManager.create(requireContext())

                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                startActivity(Intent(requireContext(),LoginActivity::class.java))
                requireActivity().finish()
            }
        }

    }

    private fun loadUser(user: FirebaseUser?) {
        db.collection("users").document(user?.uid.toString()).get()
            .addOnSuccessListener { document ->
                binding.textUsernameProfileFragment.text = document.getString("username")
                binding.textEmail.text = document.getString("email")
                document.reference.collection("task").get()
                    .addOnSuccessListener { documents ->
                        var numberofTaskCompleted = 0
                        for (document in documents.documents) {
                            if (document.getString("is done") == "true") {
                                numberofTaskCompleted += 1
                            }
                        }
                        binding.textNumberTaskCompleted.text =
                            numberofTaskCompleted.toString() + " Tasks Completed!"
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error getting user data from firebase Document", e)
                        Toast.makeText(requireContext(), "Error getting user data", Toast.LENGTH_LONG).show()
                    }
            }
    }
    private fun showDialogUpdate(titleUpdateForm: String, titleSuccessfullyUpdated: String, descriptionSuccessfullyUpdated: String, dataUpdate: String,user: FirebaseUser?) {
        dialog.setContentView(R.layout.form_update_profile)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val titleUpdateFormTextView : TextView = dialog.findViewById(R.id.title_UpdateDataForm)
        titleUpdateFormTextView.text = titleUpdateForm

        val edtUpdateData : TextInputEditText = dialog.findViewById(R.id.edt_UpdatedData)

        val edtConfirmCurrentPassword : TextInputEditText = dialog.findViewById(R.id.edt_ConfrimCurrentPassword)

        val btnUpdate : Button = dialog.findViewById(R.id.button_Update)
        btnUpdate.setOnClickListener{
            user.let {
                val credential = EmailAuthProvider.getCredential(it?.email.toString(),edtConfirmCurrentPassword.text.toString())
                it?.reauthenticate(credential)
                    ?.addOnCompleteListener {
                        if (it.isSuccessful){
                            if (dataUpdate=="email"){
                                updateEmail(user,edtUpdateData.text.toString(),titleSuccessfullyUpdated,descriptionSuccessfullyUpdated)
                            }else if (dataUpdate=="password"){
                                updatePassword(user,edtUpdateData.text.toString(),titleSuccessfullyUpdated,descriptionSuccessfullyUpdated)
                            } else{
                                updateDatatoFirestore(user,dataUpdate,edtUpdateData.text.toString(),titleSuccessfullyUpdated,descriptionSuccessfullyUpdated)
                            }
                        }else{
                            Toast.makeText(requireContext(),"Password is incorrect",Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }
                    ?.addOnFailureListener { e ->
                        Log.w(TAG, "Error updating user password", e)
                        Toast.makeText(requireContext(),"Current Password is wrong",Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
            }
        }
    }

    private fun updatePassword(user: FirebaseUser?, newPassword: String, titleSuccessfullyUpdated: String, descriptionSuccessfullyUpdated: String) {
        user?.let {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    dialog.setContentView(R.layout.dialog_successfully_updated_data)
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val titleTextUpdated: TextView = dialog.findViewById(R.id.text_Updated)
                    titleTextUpdated.text = titleSuccessfullyUpdated

                    val descriptionTextUpdated: TextView =
                        dialog.findViewById(R.id.text_UpdatedDescription)
                    descriptionTextUpdated.text = descriptionSuccessfullyUpdated

                    val btnOk: Button = dialog.findViewById(R.id.btn_UpdatedDataOk)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        loadUser(user)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating user password", e)
                    Toast.makeText(
                        requireContext(),
                        "Error updating user password",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()
                }
        }
    }

    private fun updateEmail(user: FirebaseUser?, newEmail: String, titleSuccessfullyUpdated: String, descriptionSuccessfullyUpdated: String) {
        user?.let {
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    updateDatatoFirestore(user,"email",newEmail,titleSuccessfullyUpdated,descriptionSuccessfullyUpdated)
                }
                .addOnFailureListener{ e ->
                    Log.w(TAG, "Error updating user email address", e)
                    Toast.makeText(requireContext(),"Error updating user email address",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
        }
    }

    private fun updateDatatoFirestore(user: FirebaseUser?, dataVar: String, newData: String, titleSuccessfullyUpdated: String, descriptionSuccessfullyUpdated: String) {
        db.collection("users").document(user?.uid.toString()).update(dataVar,newData)
            .addOnSuccessListener {
                dialog.setContentView(R.layout.dialog_successfully_updated_data)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

                val titleTextUpdated : TextView = dialog.findViewById(R.id.text_Updated)
                titleTextUpdated.text = titleSuccessfullyUpdated

                val descriptionTextUpdated : TextView = dialog.findViewById(R.id.text_UpdatedDescription)
                descriptionTextUpdated.text = descriptionSuccessfullyUpdated

                val btnOk : Button = dialog.findViewById(R.id.btn_UpdatedDataOk)
                btnOk.setOnClickListener{
                    dialog.dismiss()
                    loadUser(user)
                }
            }
            .addOnFailureListener{ e ->
                Log.w(TAG, "Error updating document", e)
                Toast.makeText(requireContext(),"Error updating document",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}