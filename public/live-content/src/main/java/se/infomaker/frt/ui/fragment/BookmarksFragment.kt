package se.infomaker.frt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.FragmentBookmarkContentListBinding
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.datastore.Bookmark
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frtutilities.NavigationChromeOwner
import se.infomaker.frtutilities.ktx.config
import se.infomaker.iap.theme.ktx.backgroundColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.livecontentui.ads.StickyAdsCoordinator
import se.infomaker.livecontentui.bookmark.BookmarkSelectorHandler
import se.infomaker.livecontentui.bookmark.BookmarkSelectorListener
import se.infomaker.livecontentui.bookmark.Bookmarker
import se.infomaker.livecontentui.bookmark.SwipeToDeleteCallback
import se.infomaker.livecontentui.bookmark.config.BookmarkConfig
import se.infomaker.livecontentui.bookmark.sync.BookmarksSyncManager
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.ContentListItemBoundaryDecoration
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator
import se.infomaker.livecontentui.view.canScrollUp
import se.infomaker.livecontentui.view.setDivider
import se.infomaker.streamviewer.extensions.getDrawableIdentifierOrFallback
import javax.inject.Inject

@AndroidEntryPoint
class BookmarksFragment : BaseModule() {

    private val theme by theme()
    private val viewModel by viewModels<BookmarksViewModel>()
    private val config by config<BookmarkConfig>()

    private var binding: FragmentBookmarkContentListBinding? = null
    private lateinit var adapter: BookmarksAdapter
    private lateinit var callback: SwipeToDeleteCallback
    private lateinit var bookmarker: Bookmarker

    @Inject lateinit var bookmarksSyncManager: BookmarksSyncManager

    private val selectorListener: BookmarkSelectorListener = object : BookmarkSelectorListener {
        override fun onDismissed(selected: List<Bookmark>) {
            adapter.deselectItems(selected)
        }

        override fun onDeleted(deleted: List<Bookmark>) {
            binding?.recyclerView?.let { view ->
                val message = context?.resources?.getQuantityString(R.plurals.bookmarks_removed, deleted.size, deleted.size)
                    ?: "Bookmarks removed"
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.bookmark_undo_action) {
                        bookmarker.insertAll(deleted)
                    }.show()
            }  
        }

        override fun onModeChange(isSelecting: Boolean, selector: BookmarkSelectorHandler) {
            if (isSelecting) {
                callback.disabled = true
                (activity as? AppCompatActivity)?.startSupportActionMode(selector)
            }
            else {
                callback.disabled = false
                selector.finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentBookmarkContentListBinding.inflate(inflater, container, false).also { this.binding = it }

        bookmarker = Bookmarker(binding.root, moduleIdentifier)
        OfflineBannerCoordinator(binding.offlineBanner, resourceManager).also {
            lifecycle.addObserver(it)
        }

        adapter = BookmarksAdapter(requireContext(), moduleIdentifier) {
            BookmarkSelectorHandler(theme, resourceManager, selectorListener, bookmarker)
        }

        setupEmptyView()

        setupItemDecorations()

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            updateViewVisibility(it.isNullOrEmpty())
        }
        val deleteIconIdentifier = resourceManager.getDrawableIdentifierOrFallback("action_delete", R.drawable.delete_subscription)
        callback = SwipeToDeleteCallback(binding.recyclerView.context, deleteIconIdentifier) { bookmark ->
            bookmarker.delete(bookmark)
            bookmarker.showSnackbar(bookmark, bookmarked = false)
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = object : LinearLayoutManager(context) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                onLayoutCompleted()
            }
        }

        config.ads?.let { adsConfig ->
            val stickyAdsCoordinator = StickyAdsCoordinator(binding.topStickyAdWrapper, requireActivity().findViewById(R.id.bottom_sticky_ad_wrapper), adsConfig.provider, adsConfig.sticky)
            lifecycle.addObserver(stickyAdsCoordinator)
        }

        adapter.restoreInstanceState(savedInstanceState)

        return binding.root.apply {
            setBackgroundColor(theme.backgroundColor.get())
            theme.apply(this)
        }
    }

    private fun onLayoutCompleted() {
        if (binding?.recyclerView?.canScrollUp() != true) {
            expandNavigationChrome()
        }
    }

    private fun expandNavigationChrome() {
        (activity as? NavigationChromeOwner)?.expandNavigationChrome()
    }

    override fun onResume() {
        super.onResume()
        bookmarksSyncManager.sync()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cancelSelection()
    }

    private fun setupItemDecorations() {
        setupItemSpacing()
        setupDivider()
    }

    private fun setupItemSpacing() {
        // This is sort of a hack since we have a default divider in Bookmarks and
        // we want to keep being backwards compatible.
        val itemSpacing = theme.getSize("contentListItemSpacing", ThemeSize.ZERO).sizePx.toInt()
        if (itemSpacing > 0) {
            binding?.recyclerView?.addItemDecoration(ContentListItemBoundaryDecoration.create(theme))
        }
    }

    private fun setupDivider() {
        var identifier = resourceManager.getDrawableIdentifier("bookmark_divider")
        if (identifier == 0) {
            identifier = R.drawable.default_bookmark_divider
        }
        binding?.recyclerView?.setDivider(identifier)
    }

    private fun setupEmptyView() {
        var layoutIdentifier = resourceManager.getLayoutIdentifier("bookmarks_empty_view")
        if (layoutIdentifier < 1) {
            layoutIdentifier = R.layout.no_bookmarks_default
        }
        LayoutInflater.from(context).inflate(layoutIdentifier, binding?.emptyContainer, true)
    }

    private fun updateViewVisibility(noContent: Boolean) {
        if (noContent) {
            binding?.emptyContainer?.visibility = View.VISIBLE
            binding?.recyclerView?.visibility = View.GONE
            expandNavigationChrome()
        }
        else {
            binding?.emptyContainer?.visibility = View.GONE
            binding?.recyclerView?.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() = false

    override fun onAppBarPressed() {
        binding?.recyclerView?.smoothScrollToPosition(0)
    }

    override fun shouldDisplayToolbar() = true
}