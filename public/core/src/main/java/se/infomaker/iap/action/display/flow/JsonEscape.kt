package se.infomaker.iap.action.display.flow

import java.io.IOException
import java.io.StringWriter

/**
 * Copyright 2018 Emil Gedda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Heavily inspired by the com.google.gson.stream.JsonWriter
 */
object JsonEscape {

    private val REPLACEMENT_CHARS: MutableMap<Char, String> = mutableMapOf()

    init {
        for (i in 0..0x1f) {
            REPLACEMENT_CHARS[i.toChar()] = String.format("\\u%04x", i)
        }
        REPLACEMENT_CHARS['"'] = "\\\""
        REPLACEMENT_CHARS['"'] = "\\\""
        REPLACEMENT_CHARS['\\'] = "\\\\"
        REPLACEMENT_CHARS['\t'] = "\\t"
        REPLACEMENT_CHARS['\b'] = "\\b"
        REPLACEMENT_CHARS['\n'] = "\\n"
        REPLACEMENT_CHARS['\r'] = "\\r"
        REPLACEMENT_CHARS['\u000C'] = "\\f"
    }

    @Throws(IOException::class)
    fun escape(value: String): String {
        StringWriter().use { writer ->
            var last = 0
            val length = value.length
            for (i in 0 until length) {
                val c = value[i]
                val replacement: String?
                if (c.toInt() < 128) {
                    replacement = REPLACEMENT_CHARS[c]
                    if (replacement == null) {
                        continue
                    }
                } else if (c == '\u2028') {
                    replacement = "\\u2028"
                } else if (c == '\u2029') {
                    replacement = "\\u2029"
                } else {
                    continue
                }
                if (last < i) {
                    writer.write(value, last, i - last)
                }
                writer.write(replacement)
                last = i + 1
            }
            if (last < length) {
                writer.write(value, last, length - last)
            }
            return writer.toString()
        }
    }
}
