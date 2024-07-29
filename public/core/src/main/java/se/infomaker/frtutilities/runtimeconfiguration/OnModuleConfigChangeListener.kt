package se.infomaker.frtutilities.runtimeconfiguration

interface OnModuleConfigChangeListener {
    fun onModuleConfigUpdated(modules: Set<String>)
}