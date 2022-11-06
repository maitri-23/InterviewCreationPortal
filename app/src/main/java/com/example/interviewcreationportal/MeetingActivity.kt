@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.interviewcreationportal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcreationportal.data.User
import com.example.interviewcreationportal.data.UserSlot
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

class MeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    // Initialize all the variables which holds the views
    private lateinit var meetingName: TextInputLayout
    private lateinit var chooseDateButton: Button
    private lateinit var chooseStartTimeButton: Button
    private lateinit var chooseEndTimeButton: Button
    private lateinit var addMeetingButton: Button
    private lateinit var addUserToTheMeeting: Button
    private lateinit var adapter: UserAdapter
    private lateinit var recyclerList: RecyclerView
    private lateinit var insertOrUpdateDisplay: TextView

    // Initialize all the variables which holds date and start and end time
    private var startTimeClicked: Boolean = false
    var dayChosen = -1
    var monthChosen = -1
    var yearChosen = -1
    var startHour = -1
    var startMinute = -1
    var endHour = -1
    var endMinute = -1
    var timeStampStart = ""
    var timeStampEnd = ""

    // Define global variables to hold the meeting details
    var selectedUserList: MutableList<User> = mutableListOf()
    var userTimeSlots: MutableList<UserSlot> = mutableListOf()
    var isUpdate = false
    var meetingId = ""
    var initialStartTimestamp = ""
    var initialEndTimestamp = ""
    var initialSelectedUsers: List<User> = emptyList()
    private lateinit var initialSelectedId: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        // Initialize the views
        meetingName = findViewById(R.id.nameOfMeeting)
        chooseDateButton = findViewById(R.id.chooseDatePicker)
        chooseStartTimeButton = findViewById(R.id.chooseStartTimePicker)
        chooseEndTimeButton = findViewById(R.id.chooseEndTimePicker)
        addMeetingButton = findViewById(R.id.addMeetingButton)
        addUserToTheMeeting = findViewById(R.id.addUserToMeetingButton)
        recyclerList =
            findViewById<RecyclerView>(R.id.usersInInterview) as RecyclerView
        insertOrUpdateDisplay = findViewById(R.id.insertOrUpdateDisplay)

        // Get the details of the existing meeting if the screen is set as UPDATION.
        val intent = intent
        val isEditingVersion = intent.getBooleanExtra("EDIT", false)
        if (isEditingVersion) {
            isUpdate = true
            meetingId = intent.getStringExtra("MEETING_ID").toString()
            val meetingName = intent.getStringExtra("MEETING_NAME")
            val meetingStartTime = intent.getStringExtra("MEETING_START_TIME")
            val meetingEndTime = intent.getStringExtra("MEETING_END_TIME")
            initialSelectedId = intent.getStringArrayListExtra("USER_LIST_ID")!!

            updateViewModify(meetingName!!, meetingStartTime!!, meetingEndTime!!)
        }

        // Code to choose the date from date picker dialog.
        chooseDateButton.setOnClickListener {
            val calendarDate: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarDate.get(Calendar.YEAR),
                    calendarDate.get(Calendar.MONTH),
                    calendarDate.get(Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.show()
        }

        // Code to choose the start time from time picker dialog.
        chooseStartTimeButton.setOnClickListener {
            if (dayChosen == -1) {
                Toast.makeText(
                    this,
                    "First Select Date before selecting start time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = true
                val calendarStartTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarStartTime.get(Calendar.HOUR),
                    calendarStartTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }

        }

        // Code to choose the end time from time picker dialog.
        chooseEndTimeButton.setOnClickListener {
            if (startHour == -1) {
                Toast.makeText(
                    this,
                    "Choose Start Time before choosing End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = false
                val calendarEndTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarEndTime.get(Calendar.HOUR),
                    calendarEndTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }
        }

        // On clicking the button to add/update meeting, it will first validate the input fields,
        // and upon passing validation, it will add/update the meeting on the firebase.
        addMeetingButton.setOnClickListener {
            if (validationOfFields()) {
                if (NetworkConnectivity.isNetworkAvailable(this)) {
                    // first convert the date and time fields to EPOCH
                    parseDateTimeToTimeStamp()
                    // If network connectivity present, first check if any of the contact gets collided with the given timestamp.
                    if (!collisionCheck(selectedUserList)) {
                        // If no collision present, you are good to go.
                        // If it is update, delete the previous stored meeting info,
                        if (isUpdate) {
                            FirebaseConnections.deleteMeetingWhileUpdating(
                                this,
                                meetingId,
                                initialSelectedId,
                                initialStartTimestamp
                            )
                        }
                        // Add the new info to the server
                        val nameOfMeeting = meetingName.editText?.text.toString()
                        FirebaseConnections.uploadMeetingToFirebase(
                            this,
                            nameOfMeeting,
                            selectedUserList,
                            timeStampStart,
                            timeStampEnd
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            // close the activity after 0.5 second
                            val mainActivityIntent =
                                Intent(applicationContext, MainActivity::class.java)
                            mainActivityIntent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(mainActivityIntent)
                            finishAndRemoveTask()
                        }, 500)
                    } else {
                        Toast.makeText(this, getString(R.string.collision_found), Toast.LENGTH_LONG)
                            .show()
                    }

                } else {
                    Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
                }
            }
        }

        // First it will fetch the user details from the server and display in a Checkbox dialog list.
        addUserToTheMeeting.setOnClickListener {
            // Fetch user list only when network connectivity is present.
            if (NetworkConnectivity.isNetworkAvailable(this))
                fetchUsersList()
            else {
                Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
            }

        }

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    // If the current activity is set as update, then extract the initial information of the meeting
    // and update the global variables and UI views.
    private fun updateViewModify(name: String, meetingStartTime: String, meetingEndTime: String) {
        addMeetingButton.text = getString(R.string.UPDATE_MEETING)
        insertOrUpdateDisplay.text = getString(R.string.UPDATE_A_MEETING)
        meetingName.editText?.setText(name)
        initialStartTimestamp = meetingStartTime
        initialEndTimestamp = meetingEndTime
        val dayFormat = SimpleDateFormat("dd")
        dayChosen = dayFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val monthFormat = SimpleDateFormat("MM")
        monthChosen = monthFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val yearFormat = SimpleDateFormat("yyyy")
        yearChosen = yearFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val hourFormat = SimpleDateFormat("HH")
        startHour = hourFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endHour = hourFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val minuteFormat = SimpleDateFormat("mm")
        startMinute = minuteFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endMinute = minuteFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val dateText: String = "DATE : $dayChosen  /  $monthChosen  /  $yearChosen"
        chooseDateButton.text = dateText
        val startTimeText: String = "$startHour : $startMinute"
        chooseStartTimeButton.text = "START : $startTimeText"
        val endTimeText: String = "$endHour : $endMinute"
        chooseEndTimeButton.text = "END : $endTimeText"
        fetchInitialUserList(meetingId)
    }

    // If the current activity is set as update, then extract the initial user list added to the meeting.
    private fun fetchInitialUserList(meetingID: String) {
        userTimeSlots.clear()
        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).child(meetingID).child("users")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            // Traversing in the meetings, extracting the data, and inserting in a list.
                            val usersList: MutableList<User> = mutableListOf()
                            for (dataSnapshot in snapshot.children) {
                                val id = dataSnapshot.child("UID").value.toString()
                                val name = dataSnapshot.child("name").value.toString()
                                val currentUser =
                                    User(id, name, "")
                                usersList.add(currentUser)

                                // Extracting the time-slots also of the users.
                                for (timeSnapshot in dataSnapshot.child("slots").children) {
                                    val start = timeSnapshot.child("startStamp").value.toString()
                                    val end = timeSnapshot.child("endStamp").value.toString()
                                    val userSlot = UserSlot(id, start, end)
                                    userTimeSlots.add(userSlot)
                                }
                            }

                            // After the list is fetched from the server, we set up in the adapter.
                            // The recycler adapter displays the whole list, as card item format.
                            selectedUserList = usersList
                            initialSelectedUsers = usersList
                            adapter =
                                UserAdapter(applicationContext, selectedUserList)
                            recyclerList.adapter = adapter
                            recyclerList.layoutManager = LinearLayoutManager(applicationContext)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }

    // This function checks for if the selected user list contains any user whose time slot collide with already scheduled time slots.
    private fun collisionCheck(selectedUserList: MutableList<User>): Boolean {

        // first we build a set for all the selected users list
        val setsOfChosenID = mutableSetOf<String>()
        for (users in selectedUserList) {
            setsOfChosenID.add(users.id)
        }

        // next we build a set for all the users who were already present in the meeting beforehand
        // (applied for update case only, not for insert)
        val setsOfAlreadyRegisteredID = mutableSetOf<String>()
        if (isUpdate) {
            for (userId in initialSelectedId) {
                setsOfAlreadyRegisteredID.add(userId)
            }
        }

        // extracting the meeting start and end timestamp
        val meetingStart = timeStampStart.toLong()
        val meetingEnd = timeStampEnd.toLong()

        val userActualTimeSlotsToCheck: MutableList<UserSlot> = mutableListOf()

        // we extract the time slots of all the people ->
        // except the time slots for people who were already in the interview,
        // and their time slots are in the range [initial start time , initial end tme]
        for (timeSlots in userTimeSlots) {
            if (isUpdate) {
                if (setsOfAlreadyRegisteredID.contains(timeSlots.id)) {
                    val slotStart = timeSlots.startStamp
                    val slotEnd = timeSlots.endStamp
                    if (slotStart == initialStartTimestamp && slotEnd == initialEndTimestamp) {
                        continue
                    } else {
                        userActualTimeSlotsToCheck.add(timeSlots)
                    }
                } else {
                    userActualTimeSlotsToCheck.add(timeSlots)
                }
            } else {
                userActualTimeSlotsToCheck.add(timeSlots)
            }
        }

        // now in the selected time slots, check if the current meeting time collides with any one of them.
        for (timeSlots in userActualTimeSlotsToCheck) {
            if (setsOfChosenID.contains(timeSlots.id)) {
                val slotStart = timeSlots.startStamp.toLong()
                val slotEnd = timeSlots.endStamp.toLong()
                if (!(meetingEnd < slotStart || meetingStart > slotEnd)) {
                    // collision found
                    return true
                }
            }
        }

        // no collision found
        return false
    }

    // This function is run to fetch ALL the user list from the server.
    private fun fetchUsersList() {
        userTimeSlots.clear()
        val reference = Firebase.database.reference
        reference.child(getString(R.string.users)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // Traversing in the meetings, extracting the data, and inserting in a list.
                        val usersList: MutableList<User> = mutableListOf()
                        val usersNameList: MutableList<String> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val email = dataSnapshot.child("email").value.toString()
                            val currentUser =
                                User(id, name, email)
                            usersList.add(currentUser)
                            usersNameList.add(name)

                            for (timeSnapshot in dataSnapshot.child("slots").children) {
                                val start = timeSnapshot.child("startStamp").value.toString()
                                val end = timeSnapshot.child("endStamp").value.toString()
                                val userSlot = UserSlot(id, start, end)
                                userTimeSlots.add(userSlot)
                            }

                        }

                        // Displaying the checkbox dialog based on the fetched list
                        dialog(usersNameList, usersList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // Dialog to display all the users in a checkbox list to choose from.
    private fun dialog(usersNameList: MutableList<String>, usersList: List<User>) {
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.darkRed)
            .setTitle(R.string.selectContact)
            .setIcon(R.drawable.ic_baseline_person_add_alt_1_24)
            .setItemsMultiChoice(
                usersNameList
            ) { positions, items ->
                selectedUserList.clear()
                for (pos in positions) {
                    selectedUserList.add(usersList.get(pos))
                }

                // Based on the chosen list, the adapter of the recycler view is updated.
                adapter =
                    UserAdapter(applicationContext, selectedUserList)
                recyclerList.adapter = adapter
                recyclerList.layoutManager = LinearLayoutManager(applicationContext)

                Toast.makeText(
                    this@MeetingActivity,
                    "Contact List Updated",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            .setConfirmButtonText(R.string.confirm)
            .show()

    }

    // Validation of fields before uploading in the server.
    private fun validationOfFields(): Boolean {

        // Name field kept as blank verification
        val nameOfMeeting = meetingName.editText?.text.toString()
        if (nameOfMeeting == "") {
            Toast.makeText(
                this,
                "Choose Name!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Field kept blank Verification
        if (yearChosen == -1 || startHour == -1 || endHour == -1) {
            Toast.makeText(
                this,
                "Recheck all Date and Time fields!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Start Date cannot be lesser than Current Date Verification
        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        var currentMonth = calenderCurrent.get(Calendar.MONTH)
        currentMonth++
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
        if (yearChosen < currentYear || (yearChosen == currentYear && monthChosen < currentMonth) || (yearChosen == currentYear && monthChosen == currentMonth && dayChosen < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Start Time cannot be lesser than Current Time Verification
        val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
        val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)
        if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
            if (startHour < hourCurrent || (hourCurrent == startHour && startMinute < minuteCurrent)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be lesser than Current Time!",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        // Selected user list to the meeting from the whole user list is less than 2 verification
        if (selectedUserList.size <= 1) {
            Toast.makeText(
                this,
                "Minimum participants allowed is Two!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // all verifications are perfect.
        return true
    }

    // Conversion of start dateTime and end dateTime to EPOCH timeStamp
    private fun parseDateTimeToTimeStamp() {

        val dateTimeStart =
            LocalDateTime.of(yearChosen, monthChosen, dayChosen, startHour, startMinute, 0)
        timeStampStart =
            dateTimeStart.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

        val dateTimeEnd =
            LocalDateTime.of(yearChosen, monthChosen, dayChosen, endHour, endMinute, 0)
        timeStampEnd =
            dateTimeEnd.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

    }

    // Setting of the chosen date from the date picker dialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        val currentMonth = calenderCurrent.get(Calendar.MONTH)
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)

        // Start Date cannot be lesser than Current Date Verification
        if (year < currentYear || (year == currentYear && month < currentMonth) || (year == currentYear && month == currentMonth && dayOfMonth < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        yearChosen = year
        monthChosen = month + 1
        dayChosen = dayOfMonth

        val dateText: String = "DATE : $dayOfMonth  /  $monthChosen  /  $year"
        chooseDateButton.text = dateText
    }

    @SuppressLint("SetTextI18n")
    // Setting of the chosen time from the time picker dialog
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeText: String = "$hourOfDay : $minute"
        if (startTimeClicked) {
            if (endHour != -1) {
                // Start Time cannot be lesser than End Time verification
                if (endHour < hourOfDay || (endHour == hourOfDay && endMinute <= minute)) {
                    Toast.makeText(
                        this,
                        "Start Time cannot be lesser than End Time!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    chooseStartTimeButton.text = "START : $timeText"
                }
            } else {
                val calenderCurrent = Calendar.getInstance()
                val currentYear = calenderCurrent.get(Calendar.YEAR)
                var currentMonth = calenderCurrent.get(Calendar.MONTH)
                currentMonth++
                val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
                val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
                val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)

                // Start Time cannot be lesser than Current Time verification
                if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
                    if (hourOfDay < hourCurrent || (hourCurrent == hourOfDay && minute < minuteCurrent)) {
                        Toast.makeText(
                            this,
                            "Start Time cannot be lesser than Current Time!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        startHour = hourOfDay
                        startMinute = minute
                        chooseStartTimeButton.text = "START : $timeText"
                    }
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    chooseStartTimeButton.text = "START : $timeText"
                }

            }

        } else {
            // Start Time cannot be greater than End Time verification
            if (startHour > hourOfDay || (startHour == hourOfDay && startMinute >= minute)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be greater than End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                endHour = hourOfDay
                endMinute = minute
                chooseEndTimeButton.text = "END : $timeText"
            }

        }

    }

}