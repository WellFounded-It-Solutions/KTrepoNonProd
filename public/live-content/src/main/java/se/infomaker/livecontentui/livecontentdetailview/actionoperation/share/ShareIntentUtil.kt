package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class ShareIntentUtil {
    companion object {
        /**
         * Creates an intent and returns it
         * If callback is set, we also return it through the callback in another thread
         * @param pkg package name to create intent to
         * @param text text for the intent (for example body in email)
         * @param subject subject for the intent
         * @param type content type of the intent data
         * @param callback if set, we also return the intent through it from another thread
         * @return the intent created, same as returned in callback
         */
        fun createIntent(pkg: String?, text: String, subject: String?, type: String = "text/plain", callback: ShareBuilder.IntentCreatedCallback? = null): Intent {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_TEXT, text)
            if (subject != null) {
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            }

            intent.type = type
            if (pkg != null) {
                intent.`package` = pkg
            }
            if (callback != null) {
                thread {
                    callback.onDone(intent)
                }
            }
            return intent
        }

        /**
         * Creates an intent with an image
         * Since images needs to be saved to disc before used in intents, this is done async
         * The created intent will therefore be returned via the callback
         * @param context context to use when saving the image
         * @param pkg package name to create intent to
         * @param text text for the intent (for example body in email)
         * @param subject subject for the intent
         * @param imageUrl url of the image to use in the intent
         * @param callback to return the intent via from another thread
         */
        fun createImageIntent(context: Context, pkg: String?, text: String, subject: String?, imageUrl: String, callback: ShareBuilder.IntentCreatedCallback) {
            RequestOptions()
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : SimpleTarget<Bitmap>() {

                        override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                            thread {
                                try {
                                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png")
                                    val out = FileOutputStream(file)
                                    resource.compress(Bitmap.CompressFormat.PNG, 90, out)
                                    out.close()
                                    val bmpUri = Uri.fromFile(file)

                                    val intent = createIntent(pkg, text, subject, "image/*", callback)
                                    intent.putExtra(Intent.EXTRA_STREAM, bmpUri)
                                    callback.onDone(intent)
                                } catch (e: IOException) {
                                    callback.onDone(null)
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            callback.onDone(null)
                            throw UnsupportedOperationException("not implemented")
                        }
                    })
        }

        fun isPackageExisted(context: Context, targetPackage: String): Boolean {
            val pm = context.packageManager
            try {
                pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
            return true
        }
    }
}