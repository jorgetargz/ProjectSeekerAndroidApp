package com.jorgetargz.projectseeker.framework.common.adapters.projects

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.ItemProjectBinding
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.inflate
import java.time.format.DateTimeFormatter

class ProjectsAdapter(
    private val profile: Profile,
    private val projectsActions: ProjectsActions,
) : ListAdapter<Project, ProjectsAdapter.ItemViewHolder>(
    ItemViewHolder.DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            projectsActions,
            parent.inflate(R.layout.item_project),
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = with(holder) {
        val item = getItem(position)
        setupItemView(item, profile)
    }

    class ItemViewHolder(
        private val projectsActions: ProjectsActions,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemProjectBinding.bind(itemView)

        fun setupItemView(item: Project, profile: Profile) = with(binding) {
            tvTitle.text = item.title
            val budget = if (item.minBudget == item.maxBudget) {
                item.minBudget.toString()
            } else {
                "${item.minBudget}€ - ${item.maxBudget}€"
            }
            tvBudget.text = budget
            tvDeadlineDate.text = item.deadlineDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            tvSkills.text = item.skills.joinToString(", ")
            tvStatus.text = item.status.toString()

            when (profile) {
                is Profile.Freelancer -> {
                    binding.tvOfferStatusLabel.visibility = View.VISIBLE
                    binding.tvOfferStatus.visibility = View.VISIBLE
                    val offerStatus = item.offers.firstOrNull { it.freelancerId == profile.id }?.status
                    binding.tvOfferStatus.text = offerStatus?.toString() ?: "No offer"
                }

                is Profile.Client -> {
                    binding.tvOfferStatusLabel.visibility = View.GONE
                    binding.tvOfferStatus.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                projectsActions.onClick(item.id)
            }
        }

        class DiffCallback : DiffUtil.ItemCallback<Project>() {
            override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
                return oldItem == newItem
            }
        }
    }
}