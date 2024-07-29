package se.infomaker.iap.action.display.tabs


import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.navigaglobal.mobile.databinding.FragmentTabsBinding
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.iap.action.display.DisplayManager
import se.infomaker.iap.theme.ktx.theme
import java.lang.ref.WeakReference

open class TabsFragment : Fragment(), ModuleInterface {

    private val theme by theme()

    var tabsConfig: TabsConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            tabsConfig = bundle.getSerializable(MENU_ITEM_KEY) as? TabsConfig
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTabsBinding.inflate(inflater, container, false)
        val view = binding.root

        (activity as? AppCompatActivity)?.also {
            it.setSupportActionBar(binding.toolbar)
        }

        tabsConfig?.let { config ->
            binding.toolbarTitle.text = config.title
            binding.tabLayout.setupWithViewPager(binding.pager, true)

            tabsConfig?.tabs?.let { tabs ->
                context?.let { context ->
                    binding.pager.adapter = TabsAdapter(context, childFragmentManager, tabs)
                    binding.pager.currentItem = tabs.indexOfFirst { it.defaultSelected }
                }
            }
        }

        theme.apply(view)

        return view
    }

    override fun onBackPressed(): Boolean = false

    override fun onAppBarPressed() {}

    override fun shouldDisplayToolbar(): Boolean = false

    companion object {
        private const val MENU_ITEM_KEY = "menu_item"

        @JvmStatic
        fun newInstance(tabsConfig: TabsConfig) =
            TabsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(MENU_ITEM_KEY, tabsConfig)
                }
            }
    }
}

private class TabsAdapter(val context: Context, fragmentManager: FragmentManager, val tabs: List<TabOperation>) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragments: SparseArray<WeakReference<Fragment>> = SparseArray()

    override fun getItem(position: Int): Fragment {
        return tabs[position].let { operation ->
            fragments.get(position)?.get()
                    ?: DisplayManager.create(operation.asOperation(GlobalValueManager.getGlobalValueManager(context)))
        }
    }

    override fun getPageTitle(position: Int): CharSequence? = tabs[position].title

    override fun getCount(): Int = tabs.size
}