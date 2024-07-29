package se.infomaker.frt.ui.fragment
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frt.moduleinterface.AppBarElevationHandler
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frtutilities.AppBarOwner
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.NavigationChromeOwner
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.frtutilities.runtimeconfiguration.OnModuleConfigChangeListener
import se.infomaker.frtutilities.setImageResourceWithTint
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.OnThemeUpdateListener
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment
import se.infomaker.storagemodule.Storage
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.config.PickerConfig
import se.infomaker.streamviewer.editpage.EditPageActivity
import se.infomaker.streamviewer.tabs.PickerFactory
import se.infomaker.streamviewer.tabs.TabsPagerAdapter
import se.infomaker.streamviewer.tabs.TopicPickerHelper
import timber.log.Timber

open class FollowFragment : BaseModule(), OnModuleConfigChangeListener, OnThemeUpdateListener, AppBarElevationHandler {

    private val moduleInfo by moduleInfo()
    private val resources by resources()
    private val theme by theme(this)

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            pager?.let {
                val fragment = (it.adapter as? TabsPagerAdapter)?.fragmentAtIndex(it.currentItem)
                (fragment as? LiveContentRecyclerViewFragment)?.scrollToTop()
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabSelected(tab: TabLayout.Tab?) {}
    }

    var emptyView: FrameLayout? = null
    var config: FollowConfig? = null
    private var pager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private var settingsMenuItem: MenuItem? = null
    private var timeLeft: Long = 0
    private var subscriptionId: String = ""
    private var willResetAdapter = false
    private var rootView: View? = null

    override var onAppBarElevationChanged: ((Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        timeLeft = savedInstanceState?.getLong("timeLeft") ?: 0
        subscriptionId = savedInstanceState?.getString("subscription") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view: View
        if (activity is AppBarOwner) {
            view = inflater.inflate(R.layout.fragment_tabs_pager_tab, container, false)
        } else {
            view = inflater.inflate(R.layout.fragment_tabs, container, false)
            view?.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
                (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
            }
        }

        rootView = view
        pager = view?.findViewById(R.id.pager)
        pager?.pageMargin = UI.dp2px(16f).toInt()
        emptyView = view?.findViewById(R.id.emptyView)
        pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                (pager?.adapter as? TabsPagerAdapter)?.let { adapter ->
                    adapter.registerStatsEvent(position)
                    adapter.getSubscriptionIdAtPosition(position)?.let {
                        if (it.isNotEmpty()) {
                            subscriptionId = it
                        }
                    }
                }
            }
        })

        view?.findViewById<TabLayout>(R.id.tab_layout)?.let {
            OneShotPreDrawListener.add(it) { ViewCompat.setElevation(it, 4f) }
            it.setupWithViewPager(pager)
            it.addOnTabSelectedListener(onTabSelectedListener)
            tabLayout = it
        }

        setupEmptyView()
        loadConfig()

        theme.apply(view)

        val toolbarText = ConfigManager.getInstance(context?.applicationContext).mainMenuConfig.mainMenuItems.firstOrNull {
            it.id == moduleInfo.identifier
        }?.title

        view?.findViewById<ThemeableTextView>(R.id.toolbar_title)?.let {
            it.text = toolbarText
        }

        theme.getColor(null, "background", "appBackground")?.let {
            view?.setBackgroundColor(it.get())
        }

        (pager?.adapter as? TabsPagerAdapter)?.let { tabsPagerAdapter ->

            pager?.currentItem = getStreamIdFromArguments()?.let {
                tabsPagerAdapter.getSubscriptionIndex(it)
            }
                ?: subscriptionId.takeIf { it.isNotEmpty() }?.let { tabsPagerAdapter.getSubscriptionIndex(it) }
                        ?: 0
        }
        return view
    }

    private fun loadConfig() {
        config = ConfigManager.getInstance(activity).getConfig(moduleInfo.identifier, FollowConfig::class.java).apply {
            setupAdapter(this)
        }
    }

    override fun onStart() {
        super.onStart()
        ConfigManager.getInstance().registerOnModuleConfigChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        ConfigManager.getInstance().removeOnModuleConfigChangeListener(this)
    }

    private fun getStreamIdFromArguments(): String? {
        val notification = (arguments?.getSerializable("notification") as? HashMap<String, String>)
        notification?.let { extras ->
            try {
                extras.getValue("streamId").let { streamId ->
                    return try {
                        Storage.getSubscriptions().single { it.remoteStreamId == streamId }.uuid
                    } catch (e: Exception) {
                        Timber.w(e, "Received notification for remote streamId: $streamId, but no subscription exists for that stream.")
                        null
                    }
                }
            }
            catch (e: NoSuchElementException) {
                Timber.e(e, "Received stream notification without streamId! Notification: $extras")
            }
        }
        return null
    }

    private fun setupAdapter(config: FollowConfig) {
        val moduleId = moduleInfo.identifier ?: return
        pager?.adapter = TabsPagerAdapter(childFragmentManager, moduleId, config, onChange = {
            updateEmptyState()
            willResetAdapter = true
        })
        updateEmptyState()
    }

    override fun onPause() {
        super.onPause()
        timeLeft = System.currentTimeMillis()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeft", timeLeft)
        outState.putString("subscription", subscriptionId)
    }

    override fun onResume() {
        super.onResume()

        if (willResetAdapter) {
            willResetAdapter = false
            val config = config ?: return
            val moduleId = moduleInfo.identifier ?: return
            val pager = pager
            pager?.adapter = TabsPagerAdapter(childFragmentManager, moduleId, config) {
                updateEmptyState()
                willResetAdapter = true
            }
        }
        if (System.currentTimeMillis() - timeLeft > 500) {
            (pager?.adapter as? TabsPagerAdapter)?.registerStatsEvent(0)
        }
    }

    private fun updateEmptyState() {
        val count = pager?.adapter?.count ?: 0
        if (count > 0) {
            activity?.collapsingToolbarLayoutParams()?.scrollFlags = DEFAULT_SCROLL_FLAGS

            emptyView?.visibility = View.GONE
            pager?.visibility = View.VISIBLE
            tabLayout?.visibility = View.VISIBLE
            settingsMenuItem?.isVisible = true

            onAppBarElevationChanged?.invoke(0)
        } else {
            activity?.collapsingToolbarLayoutParams()?.scrollFlags = EMPTY_LAYOUT_SCROLL_FLAGS

            emptyView?.visibility = View.VISIBLE
            pager?.visibility = View.GONE
            tabLayout?.visibility = View.GONE
            settingsMenuItem?.isVisible = false

            onAppBarElevationChanged?.invoke(4.dp2px())
        }
    }

    /**
     * Creating a dialog based of the current activity and the passed moduleId
     * Depending on the moduleid passed to this fragment the [PickerFactory] will return the corresponding intent
     */
    private fun promptPickerDialog(moduleId: String) {

        config?.let { config ->

            val color = theme.brandColor.get()

            val alertDialogLayout = LayoutInflater.from(context).inflate(R.layout.alert_dialog_list, null)
            val alertDialogTitleLayout = LayoutInflater.from(context).inflate(R.layout.alert_dialog_title, null)

            val dialogTitleTextView: TextView = alertDialogTitleLayout.findViewById<TextView>(R.id.alert_dialog_title_text_view)
            dialogTitleTextView.setPadding(72, 48, 0, 16)
            dialogTitleTextView.setText(R.string.picker_dialog_title)
            dialogTitleTextView.setTextColor(color)

            val builder = AlertDialog.Builder(context)
                .setView(alertDialogLayout)
                .setCustomTitle(dialogTitleTextView)
            val listView = alertDialogLayout.findViewById<ListView>(R.id.list_view)

            val alertDialog = builder.create()

            val pickers = TopicPickerHelper().getPickers(config)

            val adapter = object : ArrayAdapter<PickerConfig>(
                requireContext(), R.layout.alert_dialog_item, R.id.picker_title_text_view, pickers) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                    val rootView = super.getView(position, convertView, parent)
                    val container = rootView.findViewById<View>(R.id.picker_container) as LinearLayout
                    val tv = rootView.findViewById<View>(R.id.picker_title_text_view) as TextView
                    val image = rootView.findViewById<View>(R.id.picker_icon_image_view) as ImageView

                    var title = ""
                    val resourceIcon = ResourceManager(context, moduleId)

                    if (pickers[position].type == "topic") {
                        title = if (pickers[position].title.isNullOrEmpty()) {
                            getString(R.string.topic)
                        } else {
                            pickers[position].title.toString()
                        }
                        if (pickers[position].icon == null) {
                            image.setImageResourceWithTint(R.drawable.heart, color)
                        } else {
                            image.setImageResourceWithTint(resourceIcon.getDrawableIdentifier(pickers[position].icon), color)
                        }
                    }

                    tv.text = title

                    container.setOnClickListener {
                        PickerFactory.createIntent(context, moduleId, pickers[position]).apply {
                            this?.let { it1 ->
                                activity?.startActivityForResult(
                                    it1,
                                    PickerFactory.PICKER_REQUEST_CODE
                                )
                            }
                            alertDialog.dismiss()
                        }
                    }

                    return rootView
                }
            }
            listView.adapter = adapter
            alertDialog.show()
        }
    }

    private fun setupEmptyView() {
        var layoutIdentifier = resources.getLayoutIdentifier("empty_layout")
        if (layoutIdentifier < 1) {
            layoutIdentifier = R.layout.empty_layout
        }
        if (layoutIdentifier > 0) {
            val view = LayoutInflater.from(context).inflate(layoutIdentifier, emptyView)
            theme.apply(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        settingsMenuItem = if (isVisible) {
            inflater.inflate(R.menu.fragment_tabs_menu, menu)
            menu.findItem(R.id.settings)
        } else {
            null
        }

        loadCustomIcons(menu)

        config?.let { config ->
            if (config.pickers.size == 1) {
                config.pickers.first().icon?.let {

                    val drawableResource = resources.getDrawableIdentifier(it)

                    if (drawableResource != 0) {
                        context?.let { context ->
                            menu.findItem(R.id.topicPicker)?.icon = AppCompatResources.getDrawable(context, drawableResource)
                        }
                    }
                }
            }
        }

        theme.getColor("toolbarAction", null)?.let { iconColor ->
            menu.mapItemIconsNotNull().forEach {
                it.mutate()
                it.setColorFilter(iconColor.get(), PorterDuff.Mode.SRC_ATOP)
            }
        }

        updateEmptyState()
    }

    private fun loadCustomIcons(menu: Menu) {
        loadCustomIcon(menu, R.id.topicPicker, "nearme_topic_picker_icon")
        loadCustomIcon(menu, R.id.settings, "nearme_settings_icon")
    }
    private fun loadCustomIcon(menu: Menu, id: Int, resourceName: String) {
        val drawableIdentifier = resources.getDrawableIdentifier(resourceName)
        if (drawableIdentifier != 0) {
            menu.findItem(id)?.icon = AppCompatResources.getDrawable(requireContext(), drawableIdentifier)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val moduleId: String = moduleInfo.identifier ?: return false
        val config: FollowConfig = config ?: return false

        when (item.itemId) {
            R.id.topicPicker -> {
                if (config.pickers.size <= 1) {
                    context?.let { context ->
                        val picker: PickerConfig = if (config.pickers.size == 0) { TopicPickerHelper().getPickers(config).first() } else { config.pickers.first() }
                        PickerFactory.createIntent(context, moduleId, picker).apply {
                            startActivityForResult(this, PickerFactory.PICKER_REQUEST_CODE)
                        }
                    }
                } else {
                    promptPickerDialog(moduleId)
                }
            }
            R.id.settings -> {
                activity?.let { activity ->
                    activity.startActivityForResult(EditPageActivity.createIntent(activity, moduleId), EditPageActivity.REQUEST_SETTINGS)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        activity?.invalidateOptionsMenu()
    }

    override fun onDestroyView() {
        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PickerFactory.PICKER_REQUEST_CODE) {
            context?.let { context ->
                moduleInfo.identifier?.let { moduleId ->
                    PickerFactory.handleReceivingResult(resultCode, context, moduleId, data, requestCode, TABS_STATUS)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onModuleConfigUpdated(modules: Set<String>) {
        loadConfig()
    }

    override fun onThemeUpdated() {
        rootView?.let {
            theme.apply(it)
        }
    }

    override fun onBackPressed() = false

    override fun onAppBarPressed() {}

    override fun shouldDisplayToolbar() = true

    companion object {
        const val TABS_STATUS = "tabsPicker"
        private const val DEFAULT_SCROLL_FLAGS = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        private const val EMPTY_LAYOUT_SCROLL_FLAGS = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
    }
}

private fun FragmentActivity.collapsingToolbarLayoutParams() = ((this as? NavigationChromeOwner)?.collapsingToolbarLayout?.layoutParams as? AppBarLayout.LayoutParams)

private fun Menu.mapItemIconsNotNull(): List<Drawable> {
    val destination = mutableListOf<Drawable>()
    forEachItem { item -> item.icon?.let { destination.add(it) } }
    return destination
}

private fun Menu.forEachItem(action: (MenuItem) -> Unit) {
    for (i in 0 until size()) action(getItem(i))
}