package se.infomaker.livecontentmanager.query.lcc.opencontent

data class Result(val hits: HitsResult)

data class HitsResult(val hits: List<Hit>)

data class Hit(val id: String)