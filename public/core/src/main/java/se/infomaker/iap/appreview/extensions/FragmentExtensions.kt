package se.infomaker.iap.appreview.extensions

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.navigaglobal.mobile.R
import se.infomaker.iap.appreview.fragments.AlertDialogFragment

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun FragmentActivity.addFragment(fragment: AlertDialogFragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        ).add(frameId, fragment)
    }
}

fun FragmentActivity.replaceFragment(fragment: AlertDialogFragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        ).replace(frameId, fragment)
    }
}

fun FragmentActivity.removeFragment(fragment: AlertDialogFragment) {
    supportFragmentManager.inTransaction { remove(fragment) }
}