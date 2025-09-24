package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.digital.ofis.databinding.FarmerListBinding
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto

class FarmerAndStakeHolderAdapter(private val farmerAndStakeHolderList: MutableList<FarmerAndStakeHolderDto>, private val listener: (Int) -> Unit) :
    RecyclerView.Adapter<FarmerAndStakeHolderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FarmerListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farmerAndStakeHolderDto = farmerAndStakeHolderList[position]
        holder.bind(farmerAndStakeHolderDto)
    }

    override fun getItemCount(): Int {
        return farmerAndStakeHolderList.size
    }

    inner class ViewHolder(private val binding: FarmerListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listener(adapterPosition)
            }
        }

        internal fun bind(farmerAndStakeHolderDto: FarmerAndStakeHolderDto) {
            binding.apply {
                tvName.text = farmerAndStakeHolderDto.name
                tvId.text = farmerAndStakeHolderDto.displayId
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSupplierList(list: List<FarmerAndStakeHolderDto>?) {
        farmerAndStakeHolderList.clear()
        if (list != null) {
            farmerAndStakeHolderList.addAll(list.toMutableList())
        }
        notifyDataSetChanged()
    }

}
