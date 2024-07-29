package se.infomaker.iap.action.display

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.navigaglobal.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.frtutilities.NavigationChromeOwner
import se.infomaker.frtutilities.ktx.applyStatusBarColor
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.frtutilities.ktx.setStatusBarTranslucent
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.chromeColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.ktx.toolbarActionColor
import se.infomaker.iap.theme.style.text.ThemeTextStyle
import se.infomaker.iap.theme.util.UI
import timber.log.Timber

@AndroidEntryPoint
class DisplayActivity : AppCompatActivity(), NavigationChromeOwner {

    private val resources by resources { operation.moduleID }
    private val theme by theme { operation.moduleID }
    private val operation by lazy { intent.getOperation() }
    private val configuration by lazy { operation.configuration(resources) }

    private val upDrawable: Drawable? by lazy {
        val upOverrideIdentifier = resources.getDrawableIdentifier("action_up")
        AppCompatResources.getDrawable(this, if (upOverrideIdentifier > 0) upOverrideIdentifier else R.drawable.up_arrow)?.let { up ->
            DrawableCompat.setTint(up, theme.getColor("toolbarAction", theme.toolbarActionColor).get())
            return@let up
        }
    }

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var rootCoordinator: CoordinatorLayout

    override lateinit var appBarLayout: AppBarLayout
    override lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (configuration.toolbar() == "transparent" || configuration.toolbar() == "shadow") {
            setContentView(R.layout.display_no_elevation_activity)
        } else {
            setContentView(R.layout.display_activity)
        }

        val fragment: androidx.fragment.app.Fragment = supportFragmentManager.findFragmentByTag("MAIN")
                ?: DisplayManager.create(operation).also { fragment ->
                    supportFragmentManager.beginTransaction().replace(R.id.displayActivityContainer, fragment, "MAIN").commit()
                }
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbar = findViewById(R.id.toolbar)
        rootCoordinator = findViewById(R.id.root_coordinator)
        appBarLayout = findViewById(R.id.appbar)
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout)
        configuration.setBackground(rootCoordinator, operation)
        initToolbar(fragment)
    }

    private fun initToolbar(fragment: androidx.fragment.app.Fragment) {
        (fragment as? ModuleInterface)?.let {
            if (it.shouldDisplayToolbar()) {
                toolbar.setBackgroundColor(theme.getColor("toolbarColor", theme.chromeColor).get())
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayShowTitleEnabled(false)
                toolbarTitle.text = operation.parameters.title()
                theme.getText("toolbarTitle", ThemeTextStyle.DEFAULT).apply(theme, toolbarTitle)
                toolbar.visibility = View.VISIBLE
            }
            else {
                toolbar.visibility = View.GONE
            }
            theme.apply(window)
            return
        }

        setSupportActionBar(toolbar)
        theme.getText("toolbarTitle", ThemeTextStyle.DEFAULT).apply(theme, toolbarTitle)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.text = configuration.title()
        when (configuration.toolbar()) {
            "transparent" -> {
                toolbar.setBackgroundColor(Color.TRANSPARENT)
                appBarLayout.setBackgroundColor(Color.TRANSPARENT)
                if (backgroundIsLayout()) {
                    // TODO: This could probably be handled better with WindowInsets and window.translucentStatusBar()
                    window.applyStatusBarColor(Color.BLACK)
                }
                else {
                    val backgroundColor = getBackgroundColor()
                    if (backgroundColor != null) {
                        window.applyStatusBarColor(backgroundColor)
                    }
                    else {
                        // TODO: This could probably be handled better with WindowInsets and window.translucentStatusBar()
                        window.applyStatusBarColor(Color.BLACK)
                    }
                }
            }
            "shadow" -> {
                toolbar.setBackgroundColor(Color.TRANSPARENT)
                appBarLayout.setBackgroundColor(Color.TRANSPARENT)
                ContextCompat.getDrawable(this, R.drawable.transparent_toolbar)?.let { translucent ->
                    supportActionBar?.setBackgroundDrawable(translucent)
                }
                // TODO: This could probably be handled better with WindowInsets and window.translucentStatusBar()
                window.applyStatusBarColor(Color.BLACK)
            }
            "none" -> {
                toolbar.visibility = View.GONE
                if (backgroundIsLayout()) {
                    window.setStatusBarTranslucent()
                }
                else {
                    val backgroundColor = getBackgroundColor()
                    if (backgroundColor != null) {
                        window.applyStatusBarColor(backgroundColor)
                    }
                    else {
                        window.setStatusBarTranslucent()
                    }
                }
            }
            else -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(theme.getColor("toolbarColor", theme.chromeColor).get()))
                theme.apply(window)
            }
        }
    }

    private fun getBackgroundColor(): Int? {
        val color = configuration.optString("background", null)
        val themeColor = operation.theme(this).getColor(color, null)
        if (themeColor != null) {
            return themeColor.get()
        } else if (!color.isNullOrEmpty()) {
            try {
                return Color.parseColor(color)
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Could not parse color")
            }
        } else {
            theme.getColor("appBackground", null)?.let { appBackground ->
                return appBackground.get()
            }
        }
        return null
    }

    private fun backgroundIsLayout(): Boolean {
        return configuration.optJSONObject("background") != null
    }

    /**
     * Sets the background defined in operation to the view group
     */
    private fun JSONObject.setBackground(viewGroup: ViewGroup, operation: Operation) {
        val theme = operation.theme(viewGroup.context)
        val resourceManager = operation.resourceManager(viewGroup.context)
        val backgroundObject = optJSONObject("background")
        if (backgroundObject != null) {
            backgroundObject.optString("image", null)?.let { image ->
                val themeImage = theme.getImage(image, null)
                val drawableIdentifier: Int
                if (themeImage == null) {
                    drawableIdentifier = resourceManager.getDrawableIdentifier(image)
                    if (drawableIdentifier < 1) {
                        return@let
                    }
                } else {
                    drawableIdentifier = themeImage.resourceId
                }
                val backgroundView = ImageView(viewGroup.context)
                backgroundView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                viewGroup.addView(backgroundView, 0)
                backgroundView.scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundView.setImageResource(drawableIdentifier)
            }
            backgroundObject.optString("layout", null)?.let { layout ->
                val layoutIdentifier = resourceManager.getLayoutIdentifier(layout)
                if (layoutIdentifier < 1) {
                    return@let
                }

                val view = LayoutInflater.from(viewGroup.context).inflate(layoutIdentifier, viewGroup, false)
                view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                viewGroup.addView(view, 0)
                theme.apply(view)
            }
        } else {
            getBackgroundColor()?.let { backgroundColor ->
                viewGroup.setBackgroundColor(backgroundColor)
            }
        }
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        if (configuration.allowBack()) {
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(upDrawable)
            }
        } else {
            toolbarTitle.setPadding(UI.dp2px(16f).toInt(), 0, 0, 0)
        }
    }

    override fun onBackPressed() {
        if (configuration.allowBack()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (configuration.allowBack()) {
                return if (onSupportNavigateUp()) {
                    true
                } else {
                    finish()
                    true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandNavigationChrome() {
        (supportFragmentManager.findFragmentByTag("MAIN") as? ModuleInterface)?.let { module ->
            if (!module.shouldDisplayToolbar() && module is NavigationChromeOwner) {
                module.expandNavigationChrome()
                return
            }
        }
        appBarLayout.setExpanded(true)
    }

    companion object {
        val ACTION = "action"
        val PARAMETERS = "parameters"
        val MODULE_ID = "moduleId"
        val VALUE_PROVIDER = "valueProvider"

        fun start(context: Context, operation: Operation) {
            context.startActivity(operation.toIntent(context, DisplayActivity::class.java))
        }
    }
}

/**
 * Creates an intent for an operation with explicit class
 *
 * @param context
 * @param clazz Activity/Service to create intent for
 *
 * @return An intent usable to start the activity
 */
fun Operation.toIntent(context: Context, clazz: Class<*>): Intent {
    return Intent(context, clazz).apply {
        putExtras(toBundle())
    }
}

/**
 * Creates a bundle containing operation
 */
fun Operation.toBundle(): Bundle {
    return Bundle().apply {
        putString(DisplayActivity.ACTION, action)
        putString(DisplayActivity.PARAMETERS, parameters.toString())
        putString(DisplayActivity.MODULE_ID, moduleID)
        putSerializable(DisplayActivity.VALUE_PROVIDER, values)
    }
}

/**
 * Extract operation from intent
 */
fun Intent.getOperation(): Operation = extras?.getOperation() ?: Operation("", null, JSONObject(), null)

/**
 * Extract operation from bundle
 */
fun Bundle.getOperation(): Operation {
    val stringExtra = getString(DisplayActivity.PARAMETERS)
    val parameters: JSONObject
    parameters = if (stringExtra != null) {
        JSONObject(stringExtra)
    } else {
        JSONObject()
    }
    return Operation(getString(DisplayActivity.ACTION) ?: "", getString(DisplayActivity.MODULE_ID), parameters, getSerializable(DisplayActivity.VALUE_PROVIDER) as ValueProvider)
}