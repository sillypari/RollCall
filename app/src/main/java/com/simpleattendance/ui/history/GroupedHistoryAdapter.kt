package com.simpleattendance.ui.history

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simpleattendance.R
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.databinding.ItemDateHeaderBinding
import com.simpleattendance.databinding.ItemHistorySessionBinding
import java.text.SimpleDateFormat
import java.util.*

sealed class HistoryListItem {
    data class DateHeader(
        val date: String,
        val dateMillis: Long,
        val isToday: Boolean,
        val sessionCount: Int,
        var isExpanded: Boolean = true
    ) : HistoryListItem()
    
    data class Session(
        val sessionWithClass: SessionWithClass,
        val dateKey: String
    ) : HistoryListItem()
}

class GroupedHistoryAdapter(
    private val onSessionClick: (AttendanceSessionEntity) -> Unit,
    private val onSessionLongClick: (AttendanceSessionEntity) -> Unit
) : ListAdapter<HistoryListItem, RecyclerView.ViewHolder>(DiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    private val expandedDates = mutableSetOf<String>()
    private var allSessions: List<SessionWithClass> = emptyList()
    
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SESSION = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryListItem.DateHeader -> VIEW_TYPE_HEADER
            is HistoryListItem.Session -> VIEW_TYPE_SESSION
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                DateHeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemHistorySessionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SessionViewHolder(binding)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is HistoryListItem.Session -> (holder as SessionViewHolder).bind(item)
        }
    }
    
    fun setSessionsGroupedByDate(sessions: List<SessionWithClass>) {
        allSessions = sessions
        rebuildList()
    }
    
    private fun rebuildList() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // Group sessions by date
        val groupedSessions = allSessions.groupBy { session ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = session.session.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.toSortedMap(compareByDescending { it })
        
        // Build list items
        val items = mutableListOf<HistoryListItem>()
        
        groupedSessions.forEach { (dateMillis, sessionsForDate) ->
            val isToday = dateMillis == today
            val dateKey = dateFormat.format(Date(dateMillis))
            
            // Initialize expanded state - today expanded by default, others collapsed
            // Only set default on first load (when expandedDates doesn't have any entry for this date yet)
            val isExpanded = if (expandedDates.isEmpty() && isToday) {
                expandedDates.add(dateKey)
                true
            } else {
                expandedDates.contains(dateKey)
            }
            
            // Add date header
            val header = HistoryListItem.DateHeader(
                date = if (isToday) "Today" else dateKey,
                dateMillis = dateMillis,
                isToday = isToday,
                sessionCount = sessionsForDate.size,
                isExpanded = isExpanded
            )
            items.add(header)
            
            // Add sessions if expanded
            if (isExpanded) {
                sessionsForDate.forEach { sessionWithClass ->
                    items.add(HistoryListItem.Session(sessionWithClass, dateKey))
                }
            }
        }
        
        submitList(items)
    }
    
    private fun toggleDateExpansion(dateKey: String) {
        android.util.Log.d("GroupedHistoryAdapter", "toggleDateExpansion called for: $dateKey")
        android.util.Log.d("GroupedHistoryAdapter", "expandedDates before: $expandedDates")
        
        if (expandedDates.contains(dateKey)) {
            expandedDates.remove(dateKey)
        } else {
            expandedDates.add(dateKey)
        }
        
        android.util.Log.d("GroupedHistoryAdapter", "expandedDates after: $expandedDates")
        
        // Rebuild with new expansion state
        rebuildListAndNotify()
    }
    
    private fun rebuildListAndNotify() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // Group sessions by date
        val groupedSessions = allSessions.groupBy { session ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = session.session.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.toSortedMap(compareByDescending { it })
        
        // Build list items
        val items = mutableListOf<HistoryListItem>()
        
        groupedSessions.forEach { (dateMillis, sessionsForDate) ->
            val isToday = dateMillis == today
            val dateKey = dateFormat.format(Date(dateMillis))
            val isExpanded = expandedDates.contains(dateKey)
            
            // Add date header
            val header = HistoryListItem.DateHeader(
                date = if (isToday) "Today" else dateKey,
                dateMillis = dateMillis,
                isToday = isToday,
                sessionCount = sessionsForDate.size,
                isExpanded = isExpanded
            )
            items.add(header)
            
            // Add sessions if expanded
            if (isExpanded) {
                sessionsForDate.forEach { sessionWithClass ->
                    items.add(HistoryListItem.Session(sessionWithClass, dateKey))
                }
            }
        }
        
        android.util.Log.d("GroupedHistoryAdapter", "Submitting ${items.size} items")
        
        // Use submitList with a callback to ensure it completes
        submitList(items.toList()) {
            android.util.Log.d("GroupedHistoryAdapter", "submitList completed")
        }
    }
    
    inner class DateHeaderViewHolder(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(header: HistoryListItem.DateHeader) {
            binding.dateText.text = header.date
            binding.countText.text = "${header.sessionCount} session${if (header.sessionCount > 1) "s" else ""}"
            
            // Rotate arrow based on expansion state
            binding.expandIcon.rotation = if (header.isExpanded) 180f else 0f
            
            binding.dateCard.setOnClickListener {
                val actualDateKey = if (header.isToday) {
                    dateFormat.format(Date(header.dateMillis))
                } else {
                    header.date
                }
                
                // Animate arrow
                val targetRotation = if (header.isExpanded) 0f else 180f
                ObjectAnimator.ofFloat(binding.expandIcon, "rotation", binding.expandIcon.rotation, targetRotation).apply {
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
                
                toggleDateExpansion(actualDateKey)
            }
        }
    }
    
    inner class SessionViewHolder(
        private val binding: ItemHistorySessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: HistoryListItem.Session) {
            val session = item.sessionWithClass.session
            val classEntity = item.sessionWithClass.classEntity
            
            binding.classNameText.text = classEntity?.fullDisplayName ?: "Unknown Class"
            binding.dateText.text = timeFormat.format(Date(session.date))
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
    
    class DiffCallback : DiffUtil.ItemCallback<HistoryListItem>() {
        override fun areItemsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
            return when {
                oldItem is HistoryListItem.DateHeader && newItem is HistoryListItem.DateHeader ->
                    oldItem.dateMillis == newItem.dateMillis
                oldItem is HistoryListItem.Session && newItem is HistoryListItem.Session ->
                    oldItem.sessionWithClass.session.id == newItem.sessionWithClass.session.id
                else -> false
            }
        }
        
        override fun areContentsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
            return oldItem == newItem
        }
    }
}
