@file:JvmName("JSONUtil")

package se.infomaker.livecontentmanager.extensions

import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.livecontentmanager.util.HttpDate
import java.util.Date


fun JSONObject.getLastUpdated(): Date? {
    return JSONUtil.optString(this, "payload.lastupdated", null)?.let {
        HttpDate.parse(it)
    }
}

/*
 Copyright (c) 2002 JSON.org
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 The Software shall be used for Good, not Evil.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

fun JSONArray.similar(other: Any?): Boolean {
    if (other !is JSONArray) {
        return false
    }
    val len: Int = this.length()
    if (len != other.length()) {
        return false
    }
    var i = 0
    while (i < len) {
        val valueThis: Any = this.get(i)
        val valueOther: Any = other.get(i)
        if (valueThis === valueOther) {
            i += 1
            continue
        }
        if (valueThis is JSONObject) {
            if (!valueThis.similar(valueOther)) {
                return false
            }
        } else if (valueThis is JSONArray) {
            if (!valueThis.similar(valueOther)) {
                return false
            }
        } else if (valueThis != valueOther) {
            return false
        }
        i += 1
    }
    return true
}

fun JSONObject.similar(other: Any?): Boolean {
    return try {
        if (other !is JSONObject) {
            return false
        }

        val iterator = keys()
        while (iterator.hasNext()) {
            val name = iterator.next()
            val valueThis: Any? = this[name]
            val valueOther: Any? = other[name]
            if (valueThis is JSONObject) {
                if (!valueThis.similar(valueOther)) {
                    return false
                }
            } else if (valueThis is JSONArray) {
                if (!valueThis.similar(valueOther)) {
                    return false
                }
            } else if (valueThis != valueOther) {
                return false
            }
        }
        true
    } catch (exception: Throwable) {
        false
    }
}
