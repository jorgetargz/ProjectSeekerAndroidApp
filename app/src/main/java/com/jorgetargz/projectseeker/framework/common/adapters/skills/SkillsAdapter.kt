package com.jorgetargz.projectseeker.framework.common.adapters.skills

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.ItemSkillBinding
import com.jorgetargz.projectseeker.framework.common.inflate

class SkillsAdapter(
    private val skillsActions: SkillsActions
) :
    ListAdapter<String, SkillsAdapter.ItemViewholder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        return ItemViewholder(
            skillsActions,
            parent.inflate(R.layout.item_skill),
        )
    }

    override fun onBindViewHolder(holder: ItemViewholder, position: Int) = with(holder) {
        val item = getItem(position)
        bind(item)
    }

    class ItemViewholder(
        private val skillsActions: SkillsActions,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemSkillBinding.bind(itemView)

        fun bind(item: String) = with(binding) {
            skill.text = item
            skill.setOnLongClickListener {
                skillsActions.onLongClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
