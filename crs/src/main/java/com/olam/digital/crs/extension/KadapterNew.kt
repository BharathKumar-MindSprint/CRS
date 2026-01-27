package com.olam.digital.crs.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class KadapterNew<in ITEM, ViewBinding>(
    private val items: MutableList<ITEM>, /*layoutResId: Int,*/
    private val bindHolder: View.(ITEM, Int, ViewBinding) -> Unit,
    private val binding: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding,
    private val itemClick: ITEM.() -> Unit = {}
) : AbstractAdapterNew<ITEM, ViewBinding>(items,/* layoutResId,*/ binding) {

    override fun onItemClick(itemView: View, position: Int) {
        items[position].itemClick()
    }

  /*  override fun View.bind(item: ITEM, position: Int, _bi: ViewBinding?) {
        bindHolder(item, position, _bi!!)
    }*/
    public override fun View.bind(item: ITEM, position: Int, _bi: androidx.viewbinding.ViewBinding) {
        bindHolder(item, position, _bi as ViewBinding)
    }

    override fun getItemViewType(position: Int) = 0

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return items.size
    }

}

fun <ITEM, ViewBinding> RecyclerView.setUpAdapter(
    items: MutableList<ITEM>,
    layoutResId: Int,
    binding: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding,
    bindHolder: View.(ITEM, Int, ViewBinding) -> Unit,
    itemClick: ITEM.() -> Unit = {},
    manager: RecyclerView.LayoutManager = LinearLayoutManager(this.context)
): KadapterNew<ITEM, ViewBinding> {
    layoutManager = manager
    return KadapterNew(items,/* layoutResId,*/ bindHolder, binding, itemClick).apply { adapter = this }
}





