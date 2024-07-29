package se.infomaker.library

/**
 * Declare component as Destroyable, meaning it holds on to resources
 * and supports receiving a call to release those resources when this
 * component is no longer needed in its current state. 
 */
interface Destroyable {

    /**
     * Perform any cleanup of resources needed to reset the state of
     * the Destroyable component.
     *
     * This can be used to directly clean up resources that would otherwise
     * be clean up by means of lifecycle callbacks, i.e. when a component is
     * about to be reused.
     */
    fun destroy()
}