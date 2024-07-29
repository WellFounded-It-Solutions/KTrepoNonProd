package se.infomaker.frtutilities

/**
 * Works like [kotlin.collections.MutableMap.putAll(kotlin.collections.Map)] but traverses the
 * map tree branches if it is a nested map.
 * If a branch is replaced with null in replacementValues we remove the branch.
 */
fun MutableMap<Any, Any>.putRecursive(replacementValues: Map<Any, Any>?) {
    if (replacementValues == null) {
        return
    }
    val receiver = this
    replacementValues.forEach {
        if (receiver.containsKey(it.key) && it.value == null) {
            receiver.remove(it.key)
        } else if (receiver.containsKey(it.key) &&
                receiver[it.key] is MutableMap<*, *> && it.value is MutableMap<*, *>) {
            //Both are map, traverse and do this recursive
            //Don't use checked values, we want it to crash if null
            (receiver[it.key] as MutableMap<Any, Any>).putRecursive(it.value as MutableMap<Any, Any>)
        } else {
            receiver[it.key] = it.value
        }
    }
}