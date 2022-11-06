package com.example.interviewcreationportal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class AddUserActivity : AppCompatActivity() {

    // Variable binding both name and email address Text Input Layouts to it
    private lateinit var nameTextInputLayout: TextInputLayout
    private lateinit var emailTextInputLayout: TextInputLayout

    private lateinit var addUserButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        // assigning variables with views
        nameTextInputLayout = findViewById(R.id.name)
        emailTextInputLayout = findViewById(R.id.email)
        addUserButton = findViewById(R.id.addUserButton)

        // On clicking the button to add user, it will first validate the input fields,
        // and upon passing validation, it will update the user on the firebase.
        addUserButton.setOnClickListener {
            val inputName = nameTextInputLayout.editText?.text.toString()
            val inputEmail = emailTextInputLayout.editText?.text.toString()
            if (validation(inputName, inputEmail)) {
                // After successful validation, it first checks whether the device is connected to network connectivity or not.
                if (NetworkConnectivity.isNetworkAvailable(this)) {
                    // If network connectivity present, then update the user to server.
                    FirebaseConnections.uploadUserToFirebase(this, inputName, inputEmail)
                    Handler(Looper.getMainLooper()).postDelayed({
                        // close the activity after 0.5 second
                        finish()
                    }, 500)
                } else {
                    Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
                }

            }
        }


    }

    // Validation on Input fields to check if all the data are in sync to what we want to create a new user.
    private fun validation(inputName: String, inputEmail: String): Boolean {
        if (inputName == "") {
            Toast.makeText(this, getString(R.string.empty_field_name), Toast.LENGTH_LONG).show()
            return false
        }
        if (inputEmail == "") {
            Toast.makeText(this, getString(R.string.empty_field_email), Toast.LENGTH_LONG).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email_address), Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }
}