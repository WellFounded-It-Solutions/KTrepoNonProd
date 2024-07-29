package com.navigaglobal.mobile.ad.taboola

data class TaboolaAdProviderConfigWrapper(val adProviders: List<TaboolaAdProviderWrapper>)

data class TaboolaAdProviderWrapper(val provider: String, val config: TaboolaAdConfigWrapper)

data class TaboolaAdConfigWrapper (val publisherName: String)

data class TaboolaAdPlacementProperties(val placementName: String,
                                  val pageType: String,
                                  val pageUrl: String,
                                  val targetType: String,
                                  val mode: String)