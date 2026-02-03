package com.simpleattendance.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.databinding.ItemHistorySessionBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onSessionClick: (AttendanceSessionEntity) -> Unit,
    private val onSessionLongClick: (AttendanceSessionEntity) -> Unit
) : ListAdapter<SessionWithClass, HistoryAdapter.ViewHolder>(DiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorySessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemHistorySessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(sessionWithClass: SessionWithClass) {
            val session = sessionWithClass.session
            val classEntity = sessionWithClass.classEntity
            
            binding.classNameText.text = classEntity?.fullDisplayName ?: "Unknown Class"
            binding.dateText.text = dateFormat.format(Date(session.date))
            binding.subjectText.text = classEntity?.subject ?: ""
            
            binding.presentText.text = "P: ${session.presentCount}"
            binding.absentText.text = "A: ${session.absentCount}"
            binding.percentageText.text = String.format("%.0f%%", session.percentage)
            
            binding.root.setOnClickListener {
                onSessionClick(session)
            }
            
            binding.root.setOnLongClickListener {
                onSessionLongClick(session)
                true
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<SessionWithClass>() {
        override fun areItemsTheSame(oldItem: SessionWithClass, newItem: SessionWithClass): Boolean {
            return oldItem.session.id == newItem.session.id
        }
        
        override fun areContentsTheSame(oldItem: SessionWithClass, newItem: SessionWithClass): Boolean {
            return oldItem == newItem
        }
    }
}
