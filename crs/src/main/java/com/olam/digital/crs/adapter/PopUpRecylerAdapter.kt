package com.olam.digital.crs.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.digital.crs.databinding.DropdownedittextBinding

class PopUpRecylerAdapter(
    private var mList: Array<String?>,
    private var returnList: ArrayList<Any>?,
    var addNewNumber: ((userData: Any?) -> Unit)
) :
    RecyclerView.Adapter<PopUpRecylerAdapter.MyViewHolder>() {


    class MyViewHolder(private val binding: DropdownedittextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            localData: String?,
            addNewNumber: (userData: Any) -> Unit,
            position: Int,
            returnData: Any?
        ) {
            binding.apply {
                txtSpinnerEdit.text = localData
                itemView.setOnClickListener {
                    if (returnData != null) {
                        addNewNumber.invoke(returnData)
                    }
                }
            }
        }
    }


    fun setData(numberHistoryList: Array<String?>?) {
        numberHistoryList?.let { this.mList = it }
    }


    fun refreshData(premiumModelList: Array<String?>) {
        this.mList = premiumModelList
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mList[position], addNewNumber, position, returnList?.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            DropdownedittextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mList.size
}