package se.infomaker.livecontentui.section.detail

import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionState

data class SectionDetailState(val minSectionState: SectionState, val items: List<SectionItem>)