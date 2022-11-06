package com.example.interviewcreationportal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcreationportal.data.Meeting
import java.text.SimpleDateFormat

class MeetingAdapter(private val context: Context, private val meetings: List<Meeting>) :
    RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {

    // Setting up the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_meeting, parent, false)
        return ViewHolder(view).listen { pos, type ->
            // Clicking on a meeting should open the DisplayActivity.kt,
            // where it is displayed in a much broader way.
            // For that, pass parameters to the intent, to share data from this activity to that activity.
            val item = meetings[pos]
            val intent = Intent(context, DisplayActivity::class.java)
            intent.putExtra("EDIT", true)
            intent.putExtra("MEETING_ID", item.id)
            intent.putExtra("MEETING_NAME", item.name)
            intent.putExtra("MEETING_START_TIME", item.slot.startStamp)
            intent.putExtra("MEETING_END_TIME", item.slot.endStamp)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // Setting up the data.
    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Get the data about the current position, parse it, adn display it in the current card of the recycler view.
        val meeting = meetings[position]
        holder.meetingRoomName.text = meeting.name

        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        val convertedStartDate = simpleDateFormat.format(meeting.slot.startStamp.toLong() * 1000L)
        holder.timeStampStart.text = convertedStartDate.toString()

        val convertedEndDate = simpleDateFormat.format(meeting.slot.endStamp.toLong() * 1000L)
        holder.timeStampEnd.text = convertedEndDate.toString()
    }

    override fun getItemCount(): Int {
        return meetings.size
    }

    // Getting the views
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val meetingRoomName = view.findViewById<TextView>(R.id.meetingRoomName)!!
        val timeStampStart = view.findViewById<TextView>(R.id.timestampStart)!!
        val timeStampEnd = view.findViewById<TextView>(R.id.timeStampEnd)!!
    }

    // Setting up the click listener
    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}
