package com.simpleattendance.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.databinding.ItemClassBinding
import com.simpleattendance.databinding.ItemClassGroupBinding

class ClassAdapter(
    private val onClassClick: (ClassEntity) -> Unit,
    private val onClassLongClick: (ClassEntity) -> Unit,
    private val onGroupClick: (ClassGroup) -> Unit
) : ListAdapter<ClassGroup, RecyclerView.ViewHolder>(GroupDiffCallback()) {
    
    companion object {
        private const val TYPE_GROUP_SINGLE = 0
        private const val TYPE_GROUP_MULTIPLE = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        val group = getItem(position)
        return if (group.classes.size == 1) TYPE_GROUP_SINGLE else TYPE_GROUP_MULTIPLE
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_GROUP_SINGLE -> {
                val binding = ItemClassBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ClassViewHolder(binding)
            }
            else -> {
                val binding = ItemClassGroupBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                GroupViewHolder(binding)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val group = getItem(position)
        when (holder) {
            is ClassViewHolder -> holder.bind(group.classes.first())
            is GroupViewHolder -> holder.bind(group)
        }
    }
    
    inner class ClassViewHolder(
        private val binding: ItemClassBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(classEntity: ClassEntity) {
            binding.className.text = classEntity.displayName
            binding.subjectName.text = classEntity.subject
            
            binding.root.setOnClickListener {
                onClassClick(classEntity)
            }
            
            binding.root.setOnLongClickListener {
                onClassLongClick(classEntity)
                true
            }
        }
    }
    
    inner class GroupViewHolder(
        private val binding: ItemClassGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(group: ClassGroup) {
            binding.groupName.text = group.displayName
            binding.subjectCount.text = "${group.classes.size} subjects"
            
            binding.expandIcon.rotation = if (group.isExpanded) 180f else 0f
            binding.subjectsContainer.visibility = if (group.isExpanded) View.VISIBLE else View.GONE
            
            // Populate subjects
            binding.subjectsContainer.removeAllViews()
            if (group.isExpanded) {
                group.classes.forEach { classEntity ->
                    val subjectBinding = ItemClassBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.subjectsContainer,
                        false
                    )
                    subjectBinding.className.text = classEntity.subject
                    subjectBinding.subjectName.visibility = View.GONE
                    
                    subjectBinding.root.setOnClickListener {
                        onClassClick(classEntity)
                    }
                    
                    subjectBinding.root.setOnLongClickListener {
                        onClassLongClick(classEntity)
                        true
                    }
                    
                    binding.subjectsContainer.addView(subjectBinding.root)
                }
            }
            
            binding.groupHeader.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
    
    class GroupDiffCallback : DiffUtil.ItemCallback<ClassGroup>() {
        override fun areItemsTheSame(oldItem: ClassGroup, newItem: ClassGroup): Boolean {
            return oldItem.batchKey == newItem.batchKey
        }
        
        override fun areContentsTheSame(oldItem: ClassGroup, newItem: ClassGroup): Boolean {
            return oldItem == newItem
        }
    }
}
