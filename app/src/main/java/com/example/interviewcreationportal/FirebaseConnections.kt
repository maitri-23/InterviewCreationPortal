package com.example.interviewcreationportal

import android.content.Context
import android.widget.Toast
import com.example.interviewcreationportal.data.Meeting
import com.example.interviewcreationportal.data.Slot
import com.example.interviewcreationportal.data.User
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseConnections {
    companion object {

        // To upload a new user to firebase realtime database
        fun uploadUserToFirebase(context: Context, name: String, email: String) {
            val reference = Firebase.database.reference
            // create the User class
            val userId = System.currentTimeMillis().toString()
            val user = User(userId, name, email)
            // Upload user class to firebase
            reference.child(context.getString(R.string.users)).child(userId).setValue(user)
            // Display Toast to user
            Toast.makeText(
                context, context.getString(R.string.user_uploaded_to_firebase), Toast.LENGTH_LONG
            ).show()
        }

        // To upload a new meeting to firebase realtime database
        fun uploadMeetingToFirebase(
            context: Context,
            meetingName: String,
            selectedUserList: List<User>,
            timestampStart: String,
            timestampEnd: String
        ) {
            val reference = Firebase.database.reference
            // create the Meeting class
            val meetingId = System.currentTimeMillis().toString()
            val meeting = Meeting(meetingId, meetingName, Slot(timestampStart, timestampEnd))
            // Upload meeting class to firebase
            reference.child(context.getString(R.string.meeting)).child(meetingId).setValue(meeting)
            for (users in selectedUserList) {
                reference.child(context.getString(R.string.meeting)).child(meetingId)
                    .child(context.getString(R.string.users)).child(users.id).child("UID")
                    .setValue(users.id)
                reference.child(context.getString(R.string.meeting)).child(meetingId)
                    .child(context.getString(R.string.users)).child(users.id).child("name")
                    .setValue(users.name)
                val slot: Slot = Slot(timestampStart, timestampEnd)
                reference.child(context.getString(R.string.users)).child(users.id)
                    .child(context.getString(R.string.slots)).child(timestampStart).setValue(slot)
            }
            // Display Toast to user
            Toast.makeText(
                context, context.getString(R.string.meeting_uploaded_to_firebase), Toast.LENGTH_LONG
            ).show()
        }

        // To delete an existing meeting from the firebase
        fun deleteMeeting(
            context: Context,
            meetingId: String,
            selectedUserList: List<User>,
            startTimestamp: String
        ) {
            // remove the meeting details
            val reference = Firebase.database.reference
            reference.child(context.getString(R.string.meeting)).child(meetingId).removeValue()
            // remove the time-slots of the users associated with the meeting
            for (users in selectedUserList) {
                reference.child(context.getString(R.string.users)).child(users.id)
                    .child(context.getString(R.string.slots)).child(startTimestamp).removeValue()
            }
        }

        // To delete an existing meeting while updating from the firebase
        fun deleteMeetingWhileUpdating(
            context: Context,
            meetingId: String,
            initialSelectedUsers: ArrayList<String>,
            startTimestamp: String
        ) {
            // remove the meeting details
            val reference = Firebase.database.reference
            reference.child(context.getString(R.string.meeting)).child(meetingId).removeValue()
            // remove the time-slots of the users associated with the meeting
            for (userId in initialSelectedUsers) {
                reference.child(context.getString(R.string.users)).child(userId)
                    .child(context.getString(R.string.slots)).child(startTimestamp).removeValue()
            }
        }

    }
}