package se.infomaker.library

import java.util.Date

data class Bid(val demand: Map<String,String>, val size: AdSize?, val created: Date, val onConsumed: () -> Unit)
