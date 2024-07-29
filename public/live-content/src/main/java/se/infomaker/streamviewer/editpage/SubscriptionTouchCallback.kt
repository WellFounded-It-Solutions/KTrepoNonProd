package se.infomaker.streamviewer.editpage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.frt.ui.fragment.MoveHelper
import se.infomaker.storagemodule.model.Subscription

class SubscriptionTouchCallback(
    context: Context,
    adapter: EditPageAdapter,
    deleteIconIdentifier: Int,
    private val onDelete: (Subscription) -> Unit
) : MoveHelper(adapter) {

    private val deleteIcon = AppCompatResources.getDrawable(context, deleteIconIdentifier)?.apply {
        mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
    }
    private val deleteBackground = ColorDrawable(Color.parseColor("#E31E08"))
    private val deleteIconHeight = deleteIcon?.intrinsicHeight ?: 0
    private val deleteIconWidth = deleteIcon?.intrinsicWidth ?: 0
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val toReturn = super.onMove(recyclerView, viewHolder, target)
        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return toReturn
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        (viewHolder as? EditPageViewHolder)?.let {
            it.subscription?.let(onDelete)
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (!isDragging) {
            if (dX < 0) {
                deleteBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            } else {
                deleteBackground.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
            }
            deleteBackground.draw(c)

            // Calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - deleteIconHeight) / 2
            val deleteIconMargin = (itemHeight - deleteIconHeight) / 2
            val deleteIconLeft = if (dX < 0) itemView.right - deleteIconMargin - deleteIconWidth else itemView.left + deleteIconMargin
            val deleteIconRight = if (dX < 0) itemView.right - deleteIconMargin else itemView.left + deleteIconMargin + deleteIconWidth
            val deleteIconBottom = deleteIconTop + deleteIconHeight

            // Draw the delete icon
            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}