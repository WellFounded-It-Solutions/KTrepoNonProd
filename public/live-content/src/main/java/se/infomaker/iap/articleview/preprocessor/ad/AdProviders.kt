package se.infomaker.iap.articleview.preprocessor.ad

class AdProvidersConfig(val adProviders: List<AdProvider>?)
class AdProvider(val provider: String?, val config: AdProviderConfig?)
class AdProviderConfig(val baseURL: String?)
