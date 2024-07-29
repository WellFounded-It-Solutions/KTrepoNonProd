package se.infomaker.frt.ui.fragment

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.FragmentSearchBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frtutilities.NavigationChromeOwner
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.provisioning.ui.hideKeyboard
import se.infomaker.iap.theme.extensions.setCursorDrawableColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.ktx.toolbarActionColor
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment
import se.infomaker.streamviewer.extensions.getDrawableIdentifierOrFallback

private typealias ModuleId = String

class SearchFragment : BaseModule(), NavigationChromeOwner {

    private val theme by theme()
    private var binding: FragmentSearchBinding? = null

    override val appBarLayout: AppBarLayout
        get() = binding?.appBarLayout
            ?: throw IllegalStateException("Fragment $this has not initialized AppBarLayout yet.")

    override val collapsingToolbarLayout: CollapsingToolbarLayout? = null

    private val hideKeyboardOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                recyclerView.hideKeyboard()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                (f as? LiveContentRecyclerViewFragment)?.recyclerView?.addOnScrollListener(hideKeyboardOnScrollListener)
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                super.onFragmentStopped(fm, f)
                (f as? LiveContentRecyclerViewFragment)?.recyclerView?.removeOnScrollListener(hideKeyboardOnScrollListener)
            }
        }, true)
    }

    override fun onResume() {
        super.onResume()

        if (binding?.searchInput?.text?.toString()?.isEmpty() == true) {
            binding?.searchInput?.requestFocus()
            showKeyboard()
        }
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboard()
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // TODO Handle cases where action bar is already present

        (activity as? AppCompatActivity)?.setSupportActionBar(binding?.toolbar)
        binding?.appBarLayout?.doOnPreDraw { ViewCompat.setElevation(it, 4.dp2px().toFloat()) }

        theme.toolbarActionColor.get().let { color ->
            binding?.searchInput?.setHintTextColor(ColorUtils.setAlphaComponent(color, 120))
            binding?.searchInput?.setTextColor(color)
            binding?.searchInput?.setCursorDrawableColor(color)
            binding?.toolbar?.navigationIcon?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        bindHint()
        setupPreSearchView()

        theme.apply(binding?.root)

        return binding?.root
    }

    private fun bindHint() {
        val hint = resourceManager.getString("search_hint", getString(R.string.toolbar_search))
        binding?.searchInput?.hint = hint
    }

    private fun setupPreSearchView() {
        val layoutIdentifier = resourceManager.getLayoutIdentifier("search_pre_search_view")
        if (layoutIdentifier > 0) {
            LayoutInflater.from(context).inflate(layoutIdentifier, binding?.preSearchContainer, true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupQueryFlow()
        binding?.searchInput?.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (keyEvent.action == KeyEvent.ACTION_UP && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                getView()?.hideKeyboard()
            }
            true
        }
        val clearSearchIconIdentifier = resourceManager.getDrawableIdentifierOrFallback("action_close", R.drawable.search_clear)
        binding?.clearSearch?.setImageResource(clearSearchIconIdentifier)
        binding?.clearSearch?.setOnClickListener {
            binding?.searchInput?.text?.clear()
            storeLastSearchQuery(null)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (binding?.searchInput?.text.isNullOrEmpty()) {
            val startQuery = LAST_SEARCHES[moduleIdentifier]?.let { lastSearchQuery ->
                restoreLastSearchQuery(lastSearchQuery)
                updateFragment(lastSearchQuery)
                lastSearchQuery
            } ?: run {
                ""
            }
            updateClearButtonVisibility(startQuery)
        }
    }

    private fun setupQueryFlow() = binding?.searchInput?.also { searchInput ->
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            searchInput.textChanges()
                .onEach { updateClearButtonVisibility(it) }
                .debounce(1500)
                .collect {
                    if (it.length >= 2) {
                        updateFragment(it)
                        storeLastSearchQuery(it)
                    }
                }
        }
    }

    private fun updateContainerVisibility() {
        binding?.preSearchContainer?.visibility = View.GONE
        binding?.searchResultsContainer?.visibility = View.VISIBLE
    }

    private fun updateClearButtonVisibility(query: String = "") {
        val visibility = if (query.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        binding?.clearSearch?.visibility = visibility
    }

    private fun restoreLastSearchQuery(query: String) {
        binding?.searchInput?.setText(query)
    }

    private fun storeLastSearchQuery(query: String?) {
        LAST_SEARCHES[moduleIdentifier] = query
    }

    private fun updateFragment(freeTextSearch: String) {
        updateContainerVisibility()
        val fragment = LiveContentRecyclerViewFragment()
        val args = Bundle().apply {
            putString("moduleId", moduleIdentifier)
            val filter = FreeTextFilter(freeTextSearch)
            putSerializable("queryFilters", ArrayList(listOf(filter)))
        }
        fragment.arguments = args
        childFragmentManager.beginTransaction()
            .replace(R.id.search_results_container, fragment, SEARCH_RESULT_TAG)
            .commit()
    }

    override fun onBackPressed() = false

    override fun onAppBarPressed() {
        // TODO ???
    }

    override fun shouldDisplayToolbar() = false

    override fun expandNavigationChrome() {
        appBarLayout.setExpanded(true)
    }

    companion object {
        private const val SEARCH_RESULT_TAG = "SearchResult"

        private val LAST_SEARCHES = mutableMapOf<ModuleId, String?>()
    }
}

private fun EditText.textChanges() = callbackFlow {
    doAfterTextChanged { text ->
        text?.toString()?.let { this.trySend(it) }
    }

    awaitClose {  }
}