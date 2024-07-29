package se.infomaker.livecontentmanager.query.lcc.infocaster

import com.google.gson.GsonBuilder
import org.junit.Assert
import org.junit.Test
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.gson.typeadapters.RuntimeTypeAdapterFactory

class PublishEventTest {

    private val gson by lazy {
        val typeFactory = RuntimeTypeAdapterFactory
                .of(Event::class.java, "type")
        typeFactory.registerSubtype(PublishEvent::class.java, "publish")
        GsonBuilder()
                .setLenient()
                .registerTypeAdapterFactory(typeFactory)
                .registerTypeAdapter(GenericPayload::class.java, GenericPayloadDeserializer())
                .create()
    }

    @Test
    fun parsePublishEventJsonPayload() {
        val event = gson.fromJson(PUBLISH_EVENT_JSON_PAYLOAD, Event::class.java)
        Assert.assertNotNull(event)
    }

    @Test
    fun parsePublishEventStringPayload() {
        val event = gson.fromJson(PUBLISH_EVENT_STRING_PAYLOAD, Event::class.java)
        Assert.assertNotNull(event)
    }

    @Test
    fun publishEventToJSONObject() {
        val event = gson.fromJson(PUBLISH_EVENT_JSON_PAYLOAD, Event::class.java)
        val publishEvent = event as PublishEvent
        val jsonEvent = publishEvent.toJSONObject()

        val jsonPayload = JSONUtil.getJSONObject(jsonEvent, "data.payload")
        Assert.assertNotNull(jsonPayload)
        Assert.assertEquals("585436d6-9aaf-4e2c-bfd8-3bfa91b3f478", jsonPayload.get("streamId"))

        val jsonResult = JSONUtil.getJSONObject(jsonEvent, "data.payload.result")
        Assert.assertNotNull(jsonResult)
        Assert.assertEquals("Article", jsonResult.get("contentType"))
    }

    companion object {
        private const val PUBLISH_EVENT_JSON_PAYLOAD = "{\n" +
                "    \"type\": \"publish\",\n" +
                "    \"data\": {\n" +
                "        \"channel\": \"\",\n" +
                "        \"payload\": {\n" +
                "            \"result\": {\n" +
                "                \"contentType\": \"Article\",\n" +
                "                \"eventtype\": \"UPDATE\",\n" +
                "                \"uuid\": \"dc990513-b283-4ffa-aa3e-00358bc83b55\",\n" +
                "                \"eventid\": 386834,\n" +
                "                \"editable\": false\n" +
                "            },\n" +
                "            \"parameters\": {},\n" +
                "            \"streamId\": \"585436d6-9aaf-4e2c-bfd8-3bfa91b3f478\"\n" +
                "        }\n" +
                "    }\n" +
                "}"

        private const val PUBLISH_EVENT_STRING_PAYLOAD = "{\n" +
                "    \"type\": \"publish\",\n" +
                "    \"data\": {\n" +
                "        \"channel\": \"\",\n" +
                "        \"payload\": \"This is a string\"\n" +
                "    }\n" +
                "}"
    }
}