package se.infomaker.frtutilities.connectivity

import io.reactivex.FlowableOnSubscribe

interface ConnectivitySource : FlowableOnSubscribe<Unit>