package se.infomaker.livecontentui.extensions

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import se.infomaker.livecontentmanager.parser.PropertyObject

class PropertyObjectExtensionsText {

    private val listObjects = mutableListOf(
            PropertyObject(JSONObject(),"6d92fa55-b6e4-445c-932a-6ab6737b8b1d"),
            PropertyObject(JSONObject(),"b9aee065-0cf5-415a-9889-a25a078df7b6"),
            PropertyObject(JSONObject(),"ae0ec6c2-b1f0-468b-9c2f-9115235fe451"),
            PropertyObject(JSONObject(),"0cdb95f8-0c53-49ec-8902-fd7cd875987c"),
            PropertyObject(JSONObject(),"140425aa-a889-40d8-9ea3-f56a0481f126"),
            PropertyObject(JSONObject(),"eeec5b49-21da-4197-b756-30bce140b259"),
            PropertyObject(JSONObject(),"819dadf5-6c93-46b0-9b55-0efb080c8b2e"),
            PropertyObject(JSONObject(),"1237408a-5b43-44d5-b8a8-90ac4295d935"),
            PropertyObject(JSONObject(),"88c42cdc-a967-442c-b479-b20de1202b60"),
            PropertyObject(JSONObject(),"01f7c082-854e-4c5c-9d06-6fc270b823e4"),
            PropertyObject(JSONObject(),"6738a120-490f-4048-961d-d6c18e66f68a"),
            PropertyObject(JSONObject(),"9af6ae81-0eea-49a6-a1dc-58c4b32013ce"),
            PropertyObject(JSONObject(),"743a05d8-e2f0-42d1-b0af-2e3f05259289"),
            PropertyObject(JSONObject(),"78a713d7-4746-4101-af28-c901006bb1cd")
    )

    private val contentOrder = listOf(
            "6d92fa55-b6e4-445c-932a-6ab6737b8b1d",
            "78a713d7-4746-4101-af28-c901006bb1cd",
            "743a05d8-e2f0-42d1-b0af-2e3f05259289",
            "b9aee065-0cf5-415a-9889-a25a078df7b6",
            "ae0ec6c2-b1f0-468b-9c2f-9115235fe451",
            "0cdb95f8-0c53-49ec-8902-fd7cd875987c",
            "140425aa-a889-40d8-9ea3-f56a0481f126",
            "eeec5b49-21da-4197-b756-30bce140b259",
            "819dadf5-6c93-46b0-9b55-0efb080c8b2e",
            "1237408a-5b43-44d5-b8a8-90ac4295d935",
            "88c42cdc-a967-442c-b479-b20de1202b60",
            "01f7c082-854e-4c5c-9d06-6fc270b823e4",
            "6738a120-490f-4048-961d-d6c18e66f68a",
            "9af6ae81-0eea-49a6-a1dc-58c4b32013ce"
    )

    @Test
    fun testSort() {
        listObjects.sort(contentOrder)
        Assert.assertEquals("6d92fa55-b6e4-445c-932a-6ab6737b8b1d", listObjects.first().id)
        Assert.assertEquals("78a713d7-4746-4101-af28-c901006bb1cd", listObjects[1].id)
        Assert.assertEquals("ae0ec6c2-b1f0-468b-9c2f-9115235fe451", listObjects[4].id)
        Assert.assertEquals("9af6ae81-0eea-49a6-a1dc-58c4b32013ce", listObjects.last().id)
    }
}