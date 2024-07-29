package se.infomaker.iap.action

import android.content.Context
import io.reactivex.Observable
import io.reactivex.Single
import org.hamcrest.CoreMatchers
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.mockito.Mock
import org.mockito.Mockito
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.ValidationResult
import java.util.concurrent.CountDownLatch

class SwitchActionKtTest {
    @get:Rule
    val errorCollector = ErrorCollector()

    init {
        ActionManager.register("switch", SwitchAction)
        ActionManager.register("test-action", TestAction)
    }

    val action = JSONObject("""
        {
          "action": "switch",
          "parameters": {
            "value": "{{myValue}}",
            "case": {
              "1": {
                "action": "test-action",
                "parameters": {
                  "text1": "First action {{parameter}}"
                }
              },
              "2": {
                "action": "test-action",
                "parameters": {
                  "text2": "Second action {{parameter}}"
                }
              },
              "3": {
                "action": "test-action",
                "parameters": {
                  "text3": "Third action {{parameter}}"
                }
              },
              "4": {
                "action": "test-action",
                "parameters": {
                  "text4": "Fourth action {{parameter}}"
                }
              },
              "default": {
                "action": "test-action",
                "parameters": {
                  "default": "Default action {{parameter}}"
                }
              }
            }
          }
        }""")

    val mockedContext = Mockito.mock(Context::class.java)
    lateinit var valueProvider: ValueProvider

    @Before
    fun init() {
        valueProvider = object : ValueProvider {
            override fun getStrings(keyPath: String): MutableList<String>? = null
            override fun getString(keyPath: String): String? = null
            override fun observeString(keyPath: String) = Observable.just("")
        }
    }

    @Test
    fun testPerform() {
        val latch = CountDownLatch(1)
        var value: ValueProvider? = null
        val provider = ActionValueProvider(valueProvider, mapOf("myValue" to "2"))

        val stepHandler = Mockito.mock(FlowStepHandler::class.java)
        Mockito.`when`(stepHandler.getValueProvider()).then { provider }

        action.createOperation(stepHandler).perform(mockedContext) { result ->
            value = result.value
            latch.countDown()
        }
        latch.await()


        errorCollector.checkThat(value?.getString("text1"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("text2"), CoreMatchers.equalTo("Second action {{parameter}}"))
        errorCollector.checkThat(value?.getString("text3"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("text4"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("default"), CoreMatchers.nullValue())
    }

    @Test
    fun testPerformDefault() {
        val latch = CountDownLatch(1)
        var value: ValueProvider? = null

        val stepHandler = Mockito.mock(FlowStepHandler::class.java)


        action.createOperation(stepHandler).perform(mockedContext) { result ->
            value = result.value
            latch.countDown()
        }
        latch.await()
        errorCollector.checkThat(value?.getString("text1"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("text2"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("text3"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("text4"), CoreMatchers.nullValue())
        errorCollector.checkThat(value?.getString("default"), CoreMatchers.equalTo("Default action {{parameter}}"))
    }

    private object TestAction : ActionHandler {
        override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
            val map = mutableMapOf("count" to (operation.values?.getString("count") ?: "0").toInt().inc().toString())
            map.putAll(operation.parameters.stringMap())
            val value = ActionValueProvider(operation.values, map)
            onResult(Result(true, value))
        }

        override fun canPerform(context: Context, operation: Operation): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}