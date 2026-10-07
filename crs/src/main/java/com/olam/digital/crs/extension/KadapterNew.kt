package com.olam.digital.crs.extension

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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




class PdfPageAdapter(private val renderer: PdfRenderer) :
    RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    inner class PageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        return PageViewHolder(imageView)
    }

    override fun getItemCount(): Int = renderer.pageCount

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = renderer.openPage(position)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        holder.imageView.setImageBitmap(bitmap)
        page.close()
    }
}





