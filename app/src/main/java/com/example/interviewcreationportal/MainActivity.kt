package com.example.interviewcreationportal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcreationportal.data.Meeting
import com.example.interviewcreationportal.data.Slot
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // Variables binding all the UI related views.
    private lateinit var addUserButton: FloatingActionButton
    private lateinit var addMeetingButton: FloatingActionButton
    private lateinit var adapter: MeetingAdapter
    private lateinit var recyclerList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Defining the Recycler View to display all the meetings in a list.
        recyclerList =
            findViewById<RecyclerView>(R.id.meetingRooms) as RecyclerView

        // Route User To ADD USER activity, when user clicks on ADD USER Floating Action Button.
        addUserButton = findViewById(R.id.fabAddUser)
        addUserButton.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        // Route User To MEETING activity, when user clicks on ADD MEETING Floating Action Button.
        addMeetingButton = findViewById(R.id.fabAddMeeting)
        addMeetingButton.setOnClickListener {
            val intent = Intent(this, MeetingActivity::class.java)
            intent.putExtra("EDIT", false)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        // If network is present, then fetch the meeting list from the firebase database.
        if (NetworkConnectivity.isNetworkAvailable(this))
            fetchMeetingList()
    }

    private fun fetchMeetingList() {
        // Fetching the list from the firebase database
        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // Traversing in the meetings, extracting the data, and inserting in a list.
                        val meetingRooms: MutableList<Meeting> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val startTimestamp =
                                dataSnapshot.child("slot").child("startStamp").value.toString()
                            val endTimestamp =
                                dataSnapshot.child("slot").child("endStamp").value.toString()
                            val currentMeetingRoom =
                                Meeting(id, name, Slot(startTimestamp, endTimestamp))
                            meetingRooms.add(currentMeetingRoom)
                        }

                        // After the list is fetched from the server, we set up in the adapter.
                        // The recycler adapter displays the whole list, as card item format.
                        adapter =
                            MeetingAdapter(applicationContext, meetingRooms)
                        recyclerList.adapter = adapter
                        recyclerList.layoutManager = LinearLayoutManager(applicationContext)


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}