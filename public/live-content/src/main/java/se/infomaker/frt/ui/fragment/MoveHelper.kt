package se.infomaker.frt.ui.fragment

import android.content.res.Resources
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.storagemodule.Storage
import se.infomaker.streamviewer.SubscriptionAdapter
import kotlin.math.round

open class MoveHelper(protected val adapter: SubscriptionAdapter<*>) : ItemTouchHelper.Callback() {

    private var _dragging = false
    private val liftHeight: Float
    private var dragFromPosition = -1
    private var dragToPosition = -1
    private val ordering: MutableList<Pair<String?, Int?>> = mutableListOf()
    val isDragging
        get() = _dragging

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                Storage.getSubscriptions().map {
                    Pair(it.uuid, it.order)
                }.forEach {
                    ordering.add(it)
                }
                viewHolder?.also {
                    dragFromPosition = it.adapterPosition
                    ViewCompat.animate(it.itemView).translationZ(liftHeight).setDuration(DURATION.toLong()).start()
                }
                _dragging = true
            }
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                    adapter.updateSubscriptionOrdering(ordering)
                    // Reset drag positions
                    dragFromPosition = -1
                    dragToPosition = -1
                }
                ordering.clear()
                _dragging = false
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        ViewCompat.animate(viewHolder.itemView).translationZ(0f).setDuration(DURATION.toLong())
            .start()
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeFlag(
            ItemTouchHelper.ACTION_STATE_DRAG,
            ItemTouchHelper.DOWN or ItemTouchHelper.UP
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        dragToPosition = target.adapterPosition
        val sub1ToMove = ordering[viewHolder.absoluteAdapterPosition]
        val sub2ToMove = ordering[target.absoluteAdapterPosition]

        ordering[viewHolder.absoluteAdapterPosition] = sub2ToMove
        ordering[target.absoluteAdapterPosition] = sub1ToMove
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isLongPressDragEnabled(): Boolean = true

    private fun dp2px(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return round(px)
    }

    companion object {
        const val DURATION = 150
    }

    init {
        liftHeight = dp2px(8f)
    }
}