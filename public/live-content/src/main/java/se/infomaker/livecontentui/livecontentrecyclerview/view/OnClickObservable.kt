package se.infomaker.livecontentui.livecontentrecyclerview.view

import io.reactivex.Observable

interface OnClickObservable {
    fun clicks(): Observable<ViewClick>?
}

data class ViewClick(val identifier: String, val contentId: String? = null)