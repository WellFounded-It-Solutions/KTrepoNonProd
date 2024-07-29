package se.infomaker.iap.provisioning

import androidx.fragment.app.Fragment
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.display.DisplayProvider
import se.infomaker.iap.provisioning.ui.LoginFragment

class DisplayLoginHandler : DisplayProvider{
    override fun create(operation: Operation): Fragment {
        return LoginFragment()
    }
}
