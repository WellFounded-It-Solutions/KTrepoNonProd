package se.infomaker.iap.action.display.flow.validator

import io.reactivex.Observable
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import se.infomaker.frtutilities.meta.ValueProvider

class MinimumLengthValidatorTest {
    companion object {
        val EMPTY_VALUE_PROVIDER = object : ValueProvider {
            override fun getStrings(keyPath: String) = null
            override fun getString(keyPath: String) = null
            override fun observeString(keyPath: String) = Observable.just("")
        }
        const val MESSAGE = "Lösenordet måste vara minst 8 tecken"
    }

    @Test
    fun enoughCharacters() {
        Assert.assertEquals(null,
                MinimumLengthValidator.validate("1234567", EMPTY_VALUE_PROVIDER, JSONObject("""
            {
                "length": 7,
                "message": "$MESSAGE"
            }
        """.trimIndent())))
    }

    @Test
    fun tooFewCharacters() {
        Assert.assertEquals(MESSAGE,
                MinimumLengthValidator.validate("1234567", EMPTY_VALUE_PROVIDER, JSONObject("""
            {
                "length": 8,
                "message": "$MESSAGE"
            }
        """.trimIndent())))
    }

    @Test
    fun moreCharacters() {
        Assert.assertEquals(null,
                MinimumLengthValidator.validate("1234567891011", EMPTY_VALUE_PROVIDER, JSONObject("""
            {
                "length": 10,
                "message": "$MESSAGE"
            }
        """.trimIndent())))
    }
}