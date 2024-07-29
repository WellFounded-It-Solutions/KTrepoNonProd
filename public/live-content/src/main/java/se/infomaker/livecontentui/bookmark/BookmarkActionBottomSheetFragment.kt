package se.infomaker.livecontentui.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.BookmarkBottomSheetBinding
import se.infomaker.datastore.Bookmark
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.extensions.getJSONObjectOrNull
import se.infomaker.livecontentui.extensions.putJSONObject
import se.infomaker.livecontentui.extensions.setOnAllClickListener
import se.infomaker.streamviewer.extensions.getDrawableIdentifierOrFallback

class BookmarkActionBottomSheetFragment : BottomSheetDialogFragment() {

    private val uuid by lazy { arguments?.getString(UUID_KEY) ?: throw IllegalArgumentException("BookmarkActionBottomSheetFragment requires an article uuid.") }
    private val moduleId by lazy { arguments?.getString(MODULE_ID) ?: throw IllegalArgumentException("BookmarkActionBottomSheetFragment requires a moudle id.") }
    private val properties by lazy { arguments?.getJSONObjectOrNull(PROPERTIES_KEY) ?: throw IllegalArgumentException("BookmarkActionBottomSheetFragment requires article properties.") }
    private val viewModel: BookmarkActionViewModel by viewModels { BookmarkActionViewModelFactory(uuid) }
    private val resources by resources()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BookmarkBottomSheetBinding.inflate(inflater, container, false)

        val bookmarkIconIdentifier = resources.getDrawableIdentifierOrFallback("action_bookmark_filled", R.drawable.ic_bookmark)
        binding.actionImage.setImageResource(bookmarkIconIdentifier)
        val cancelIconIdentifier = resources.getDrawableIdentifierOrFallback("action_close", R.drawable.close_button)
        binding.cancelImage.setImageResource(cancelIconIdentifier)
        binding.cancelGroup.setOnAllClickListener { dismiss() }

        val bookmarker = Bookmarker(requireContext(), moduleId)
        val resultChannel = ViewModelProvider(requireActivity()).get(BookmarkingResultChannel::class.java)
        viewModel.isBookmarked.observe(viewLifecycleOwner) { isBookmarked ->
            binding.actionText.text = if (isBookmarked == true) resources.getString("bookmark_remove_bookmark", null) else resources.getString("bookmark_save_bookmark", null)
            binding.actionGroup.setOnAllClickListener {
                val bookmark = Bookmark(uuid, properties, moduleId, false)
                if (isBookmarked == true) {
                    bookmarker.delete(bookmark)
                } else {
                    bookmarker.insert(bookmark)
                }
                resultChannel.submit(bookmark, !isBookmarked) // By now, we would have deleted/inserted so we reverse the state.
                dismiss()
            }
        }

        return binding.root
    }

    companion object {

        private const val MODULE_ID_KEY = "moduleId"
        private const val UUID_KEY = "uuid"
        private const val PROPERTIES_KEY = "properties"
        private const val MODULE_ID = "moduleId"

        @JvmStatic
        fun newInstance(moduleId: String?, propertyObject: PropertyObject): BookmarkActionBottomSheetFragment {
            return BookmarkActionBottomSheetFragment().also { fragment ->
                fragment.arguments = Bundle().also {
                    it.putString(MODULE_ID_KEY, moduleId)
                    it.putString(UUID_KEY, propertyObject.id)
                    it.putString(MODULE_ID, moduleId)
                    it.putJSONObject(PROPERTIES_KEY, propertyObject.properties)
                }
            }
        }
    }
}