package se.infomaker.livecontentui.livecontentdetailview.actionoperation

class ResponseModel {
    var callback: String? = null
    lateinit var operation: Operation
}

class Operation {
    lateinit var action: String
    lateinit var parameters: Map<String, String>
}