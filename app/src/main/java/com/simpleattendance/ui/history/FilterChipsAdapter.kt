package com.simpleattendance.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simpleattendance.R
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.databinding.ItemFilterChipBinding
import com.simpleattendance.util.AnimationUtils

class FilterChipsAdapter(
    private val onChipSelected: (ClassEntity?, android.view.View) -> Unit
) : RecyclerView.Adapter<FilterChipsAdapter.ChipViewHolder>() {

    private var classes: List<ClassEntity> = emptyList()
    private var selectedIndex = 0

    fun submitClasses(newClasses: List<ClassEntity>) {
        classes = newClasses
        notifyDataSetChanged()
    }

    fun setSelectedClassId(classId: Long?) {
        val oldIndex = selectedIndex
        selectedIndex = if (classId == null) {
            0
        } else {
            val idx = classes.indexOfFirst { it.id == classId }
            if (idx != -1) idx + 1 else 0
        }
        if (oldIndex != selectedIndex) {
            notifyItemChanged(oldIndex)
            notifyItemChanged(selectedIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemFilterChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val isSelected = position == selectedIndex
        val classEntity = if (position == 0) null else classes[position - 1]
        val text = classEntity?.displayName ?: "All Classes"
        holder.bind(text, isSelected, classEntity)
    }

    override fun getItemCount(): Int = classes.size + 1

    inner class ChipViewHolder(
        private val binding: ItemFilterChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, isSelected: Boolean, classEntity: ClassEntity?) {
            binding.chipText.text = text
            val context = binding.root.context

            if (isSelected) {
                binding.chipCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_subtle))
                binding.chipCard.strokeColor = ContextCompat.getColor(context, R.color.primary)
                binding.chipText.setTextColor(ContextCompat.getColor(context, R.color.primary))
            } else {
                binding.chipCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                binding.chipCard.strokeColor = ContextCompat.getColor(context, R.color.glass_border)
                binding.chipText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            binding.root.setOnClickListener {
                if (selectedIndex != adapterPosition) {
                    val oldIndex = selectedIndex
                    selectedIndex = adapterPosition
                    notifyItemChanged(oldIndex)
                    notifyItemChanged(selectedIndex)
                    
                    // Spring compression feedback
                    AnimationUtils.applySpringScale(it)
                    
                    onChipSelected(classEntity, it)
                }
            }
        }
    }
}
