package se.infomaker.coremedia.slideshow

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import timber.log.Timber

class SlideshowPagerAdapter(supportFragmentManager: FragmentManager, val images: List<ImageObject>, val currentImage: String?) : androidx.fragment.app.FragmentStatePagerAdapter(supportFragmentManager) {
    val registeredFragments = SparseArray<ImageFragment>()

    override fun getItem(pos: Int): ImageFragment {
        val imageObject = images[pos]
        return ImageFragment.newInstance(imageObject, imageObject.cropUrl.containsUUID(currentImage) || imageObject.url.containsUUID(currentImage))
    }

    fun isStartImage(url: String?): Boolean = currentImage?.let { url?.contains(currentImage) } ?: false

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as ImageFragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        Timber.d("Destroying item in position %s", position)
        super.destroyItem(container, position, `object`)
    }

    fun getFragment(position: Int): ImageFragment? {
        return registeredFragments[position]
    }
}