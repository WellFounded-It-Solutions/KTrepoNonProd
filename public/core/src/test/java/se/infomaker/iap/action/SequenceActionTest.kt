package se.infomaker.iap.action

import android.content.Context
import io.reactivex.Observable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.equalTo
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.mockito.Mockito
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.ValidationResult
import se.infomaker.iap.action.display.flow.mustachify
import java.util.concurrent.CountDownLatch

class SequenceActionTest {
    @get:Rule
    val errorCollector = ErrorCollector()

    init {
        ActionManager.register("sequence", SequenceAction)
        ActionManager.register("test-action", TestAction)
    }

    val action = JSONObject("""{
            "action": "sequence",
            "parameters": {
              "actions": [
                {
                  "action": "test-action",
                  "parameters": {
                    "text1": "First action {{count}}"
                  }
                },
                {
                  "action": "test-action",
                  "parameters": {
                    "text2": "Second action {{count}}"
                  }
                },
                {
                  "action": "test-action",
                  "parameters": {
                    "text3": "Third action {{count}}"
                  }
                },
                {
                  "action": "test-action",
                  "parameters": {
                    "text4": "Fourth action {{count}}"
                  }
                }
              ]
            }
          }""")

    val mockedContext = Mockito.mock(Context::class.java)
    lateinit var valueProvider: FlowStepHandler

    @Before
    fun init() {
        valueProvider = object : FlowStepHandler {
            override fun getModuleId() = "test"

            override fun getValues(): JSONObject {
                return JSONObject()
            }

            override fun getValueProvider(): ValueProvider {
                return object : ValueProvider{
                    override fun getStrings(keyPath: String): MutableList<String>? = null
                    override fun getString(keyPath: String): String? = null
                    override fun observeString(keyPath: String) = Observable.just("")

                }
            }

            override fun validateViews(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun validate(): Single<ValidationResult> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun currentView(): String? = null
        }
    }

    @Test(timeout = 2000)
    fun testPerform() {
        val latch = CountDownLatch(1)
        var value: ValueProvider? = null
        action.createOperation(valueProvider).perform(mockedContext) { result ->
            value = result.value
            latch.countDown()
        }
        latch.await()
        errorCollector.checkThat(value?.getString("count"), equalTo("4"))
        errorCollector.checkThat(value?.getString("text1"), equalTo("First action "))
        errorCollector.checkThat(value?.getString("text2"), equalTo("Second action 1"))
        errorCollector.checkThat(value?.getString("text3"), equalTo("Third action 2"))
        errorCollector.checkThat(value?.getString("text4"), equalTo("Fourth action 3"))
    }

    private object TestAction : ActionHandler {
        override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
            val map = mutableMapOf("count" to (operation.values?.getString("count") ?: "0").toInt().inc().toString())
            map.putAll(operation.parameters.stringMap().map { it.key to it.value.mustachify(operation.values) })
            val value = ActionValueProvider(operation.values, map)
            onResult(Result(true, value))
        }

        override fun canPerform(context: Context, operation: Operation): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

fun JSONObject.stringMap(): Map<String, String> {
    return keys().asSequence().toList().mapNotNull {
        (get(it) as? String)?.let { case ->
            it to case
        }
    }.toMap()
}

