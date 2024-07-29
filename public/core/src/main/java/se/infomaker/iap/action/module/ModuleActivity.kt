package se.infomaker.iap.action.module

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.navigaglobal.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.frt.moduleinterface.behaviour.DesiredCollapsingToolbarLayoutBehaviour
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.iap.action.display.module.ModuleFragmentFactory
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.style.text.ThemeTextStyle
import timber.log.Timber

@AndroidEntryPoint
class ModuleActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null
    private var toolbar: Toolbar? = null
    private var moduleId: String? = null
    private var config: BaseModuleConfig? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null

    private val titleFromIntent: String?
        get() {
            val intent = intent
            if (intent != null) {
                val title = intent.getStringExtra("title")
                if (title != null) {
                    return title
                }
            }
            return null
        }

    private var toolbarTitle: TextView? = null

    override fun onStart() {
        super.onStart()
        if (currentFragment is DesiredCollapsingToolbarLayoutBehaviour) {
            toolbarLayout?.let {
                (currentFragment as DesiredCollapsingToolbarLayoutBehaviour).updateBehaviour(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_activity)
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbarLayout = findViewById(R.id.collapsing_toolbar_layout)
        moduleId = intent.getStringExtra(Module.MODULE_ID)
        config = ConfigManager.getInstance(applicationContext).getConfig(moduleId, BaseModuleConfig::class.java)

        val theme = ThemeManager.getInstance(this).getModuleTheme(moduleId)

        currentFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (currentFragment == null) {
            try {
                currentFragment = createModuleFragment(intent).also {
                    val ft = supportFragmentManager.beginTransaction()
                    ft.add(R.id.content_frame, it, FRAGMENT_TAG).commit()
                }

            } catch (e: InvalidModuleException) {
                finish()
                return
            }

        }
        theme.initToolbar()
        theme.apply(this)
    }

    private fun Theme.initToolbar() {
        getText("toolbarTitle", ThemeTextStyle.DEFAULT)?.apply(this, toolbarTitle)
        setSupportActionBar(toolbar)
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            toolbarTitle?.text = titleFromIntent ?: ModuleInformationManager.getInstance().getModuleTitle(moduleId)
            actionBar.setDisplayShowTitleEnabled(false)
            ResourcesCompat.getDrawable(resources, R.drawable.up_arrow, null)?.let { up ->
                getColor("toolbarAction", ThemeColor.DKGRAY)?.get()?.let { tintColor ->
                    DrawableCompat.setTint(up, tintColor)
                }
                actionBar.setHomeAsUpIndicator(up)
            }

            getColor("toolbarColor", null)?.get()?.let {
                toolbar?.setBackgroundColor(it)
                toolbarLayout?.setContentScrimColor(it)
            }
        }
    }

    @Throws(InvalidModuleException::class)
    private fun createModuleFragment(intent: Intent): Fragment {
        val bundle = Bundle(intent.extras)
        val moduleName = intent.getStringExtra(MODULE_NAME)
        val moduleId = bundle.getString(Module.MODULE_ID)

        val fragment = ModuleFragmentFactory.createFragment(this, moduleName, bundle)

        if (fragment !is ModuleInterface) {
            Timber.w("Module does not implement ModuleInterface.")
        }

        return fragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (currentFragment is ModuleInterface) {
            if (!(currentFragment as ModuleInterface).onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FRAGMENT_TAG = "modularFragment"
        const val MODULE_NAME = "moduleName"
    }
}
