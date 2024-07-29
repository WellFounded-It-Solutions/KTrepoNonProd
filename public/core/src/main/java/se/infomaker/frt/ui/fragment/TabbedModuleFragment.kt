package se.infomaker.frt.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.navigaglobal.mobile.R
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.frtutilities.AppBarOwner
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.iap.action.display.module.ModuleFragmentFactory
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.view.ThemeableTextView
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.HashMap

open class TabbedModuleFragment : Fragment(), ModuleInterface {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val moduleId = arguments?.getString("moduleId")
        val moduleName = arguments?.getString("moduleName")

        val theme = ThemeManager.getInstance(context).getModuleTheme(moduleId)
        val view = if (activity is AppBarOwner) {
            inflater.inflate(R.layout.fragment_tabbed_module_pager_tab, container, false)
        }
        else {
            inflater.inflate(R.layout.fragment_tabbed_module, container, false)?.apply {
                findViewById<Toolbar>(R.id.toolbar)?.let {
                    (activity as? AppCompatActivity)?.setSupportActionBar(it)
                }
            }
        }
        theme.apply(view)

        val pager = view?.findViewById<ViewPager>(R.id.pager)
        ConfigManager.getInstance().getConfig(moduleName, moduleId, TabbedModuleConfig::class.java)?.let { config ->

            Timber.d("ConfigManagerData Name: $moduleName, ID: $moduleId, TabbedConfig: %s", TabbedModuleConfig::class.java)

            view?.findViewById<TabLayout>(R.id.tab_layout)?.let {
                it.setupWithViewPager(pager)
                OneShotPreDrawListener.add(it) { ViewCompat.setElevation(it, 4f) }
            }

            Timber.d("TabsDataFetched: %s", config)

            config.tabs.let { tabs ->
                tabs.forEach { tab ->
                    ModuleInformationManager.getInstance().addModuleInformation(tab.id, tab.title, tab.module, null)
                    ModuleInformationManager.getInstance().addNewModuleInformation(tab.id, tab.title, tab.parent, tab.module, null)
                    Timber.d("AddingTabbedModule id: %s, title: %s, module: %s", tab.id, tab.title, tab.module)
                }
                context?.let { context ->
                    pager?.adapter = TabsAdapter(context, childFragmentManager, tabs)
                    pager?.currentItem = tabs.indexOfFirst { it.defaultSelected == true }
                }
            }

            (arguments?.getSerializable("notification") as? HashMap<String, String>)?.let { notification ->
                config.tabs.firstOrNull { it.id == notification["context"] }?.let { tab ->
                    pager?.currentItem = config.tabs.indexOf(tab)
                }
            }
        }

        val toolbarText = ConfigManager.getInstance(context?.applicationContext).mainMenuConfig.mainMenuItems.firstOrNull {
            it.id == moduleId
        }?.title

        view?.findViewById<ThemeableTextView>(R.id.toolbar_title)?.let {
            it.text = toolbarText
        }

        return view
    }

    override fun onBackPressed() = false

    override fun onAppBarPressed() {}

    override fun shouldDisplayToolbar() = true
}

private class TabsAdapter(val context: Context, fragmentManager: FragmentManager, val tabs: List<TabConfig>) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragments: SparseArray<WeakReference<Fragment>> = SparseArray()

    override fun getItem(position: Int): Fragment {
        return tabs[position].let { tab ->
            fragments.get(position)?.get() ?: ModuleFragmentFactory.createFragment(context, tab.module, tab.bundle())
        }
    }

    override fun getPageTitle(position: Int): CharSequence? = tabs[position].title

    override fun getCount(): Int = tabs.size
}