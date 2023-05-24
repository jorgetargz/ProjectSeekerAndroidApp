package com.jorgetargz.projectseeker.framework.common.adapters.offers

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.ItemOfferBinding
import com.jorgetargz.projectseeker.domain.project.Offer
import com.jorgetargz.projectseeker.domain.project.OfferStatus
import com.jorgetargz.projectseeker.framework.common.inflate

class OffersAdapter(
    private val offersActions: OffersActions
) :
    ListAdapter<Offer, OffersAdapter.ItemViewholder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        return ItemViewholder(
            offersActions,
            parent.inflate(R.layout.item_offer),
        )
    }

    override fun onBindViewHolder(holder: ItemViewholder, position: Int) = with(holder) {
        val item = getItem(position)
        bind(item)
    }

    class ItemViewholder(
        private val offersActions: OffersActions,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemOfferBinding.bind(itemView)

        fun bind(item: Offer) = with(binding) {
            tvDescription.text = item.description
            tvBudget.text = item.budget.toString()
            tvOfferStatus.text = item.status.toString()

            btnViewProfile.setOnClickListener {
                offersActions.onViewProfile(item.freelancerId)
            }

            if (item.status == OfferStatus.PENDING) {
                btnAcceptOffer.visibility = View.VISIBLE
                btnAcceptOffer.setOnClickListener {
                    offersActions.onAcceptOffer(item.freelancerId)
                }
            } else {
                btnAcceptOffer.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem.freelancerId == newItem.freelancerId
        }

        override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem == newItem
        }
    }
}
