package se.infomaker.iap.appreview

class PreferencesHelperTest: PreferencesBase() {

    private var _sessionStart = 0
    private var _sessionEnd = 0
    private var _totalSessionTime = 0
    private var _neverAsk = false
    private var _snoozeStartTime = 0

    override var sessionStart: Int
        get() = _sessionStart
        set(value) {
            _sessionStart = value
        }

    override var sessionEnd: Int
        get() = _sessionEnd
        set(value) {
            _sessionEnd = value
        }

    override var totalUsageTime: Int
        get() = _totalSessionTime
        set(value) {
            _totalSessionTime = value
        }

    override var neverAsk: Boolean
        get() = _neverAsk
        set(value) {
            _neverAsk = value
        }

    override var snoozeStartTime: Int
        get() = _snoozeStartTime
        set(value) {
            _snoozeStartTime = value
        }
}