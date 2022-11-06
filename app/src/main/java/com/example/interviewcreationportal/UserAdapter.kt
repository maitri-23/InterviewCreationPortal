package com.example.interviewcreationportal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcreationportal.data.User

// This class is used to setup the recycler adapter for the user list in both DisplayActivity.kt and MeetingActivity.kt
class UserAdapter(private val context: Context, private val userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    // Setting up the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_person, parent, false)
        return ViewHolder(view)
    }

    // Setting up data to the view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.name.text = currentUser.name

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // Getting the views
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.meetingUserName)
    }
}