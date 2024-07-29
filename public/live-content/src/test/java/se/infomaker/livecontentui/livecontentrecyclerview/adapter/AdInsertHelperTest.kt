package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class AdInsertHelperTest {

    @Test
    fun insertAdPassingNoAdsBeforeStart() {
        val adInsertHelper = AdInsertHelper(3, 3, 8, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "article", "article", "article", "article", "related", "related", "related", "related", "related", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(3, adsInsertionPoints.first())
        Assert.assertEquals(JSONObject().toString(), itemList[3].toString())
    }

    @Test
    fun insertAdPassing1AdBeforeStart() {
        val adInsertHelper = AdInsertHelper(3, 3, 8, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "related", "article", "article", "article", "related", "related", "related", "related", "related", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(4, adsInsertionPoints.first())
        Assert.assertEquals(JSONObject().toString(), itemList[4].toString())
    }

    @Test
    fun insertAdPassingManyAdBlockersBeforeStart() {
        val adInsertHelper = AdInsertHelper(3, 3, 8, null, "article", listOf("related"))
        val itemList1: MutableList<Any> = mutableListOf("article", "related", "related", "related", "article", "related", "related", "related", "related", "related", "related", "article", "article", "related", "related", "related", "related", "related", "article")
        val adsInsertionPoints1 = adInsertHelper.fillAds(itemList1)
        Assert.assertEquals(12, adsInsertionPoints1.first())
        Assert.assertEquals(JSONObject().toString(), itemList1[12].toString())

        val itemList2: MutableList<Any> = mutableListOf("article", "related", "related", "article", "related", "related", "related", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article")
        val adsInsertionPoints2 = adInsertHelper.fillAds(itemList2)
        Assert.assertEquals(8, adsInsertionPoints2.first())
        Assert.assertEquals(JSONObject().toString(), itemList2[8].toString())
    }

    @Test
    fun insertFirstAdAt3AndPassFirstArticleWith3Blockers() {
        val adInsertHelper = AdInsertHelper(3, 6, 6, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "related", "related", "related", "article", "article", "article", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(6, adsInsertionPoints.first())
        Assert.assertEquals(JSONObject().toString(), itemList[6].toString())
    }

    @Test
    fun insertAdAfterRelated() {
        val adInsertHelper = AdInsertHelper(3, 6, 6, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "article", "article", "related", "related", "related", "related", "article", "article", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(7, adsInsertionPoints.first())
        Assert.assertEquals(JSONObject().toString(), itemList[7].toString())
    }

    @Test
    fun insertAd6Articles4RelatedRestArticles() {
        val adInsertHelper = AdInsertHelper(3, 6, 6, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "article", "article", "article", "article", "article", "related", "related", "related", "related", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(3, adsInsertionPoints.first())
        Assert.assertEquals(14, adsInsertionPoints[1])
        Assert.assertEquals(JSONObject().toString(), itemList[3].toString())
        Assert.assertEquals(JSONObject().toString(), itemList[14].toString())
    }

    @Test
    fun `insert ad when there are multiple related portions`() {
        val adInsertHelper = AdInsertHelper(3, 6, 6, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "related", "related", "related", "related", "article", "article", "related", "related", "related", "related", "article", "article", "related", "related", "article", "article", "article", "article", "article", "article", "related", "related", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(11, adsInsertionPoints.first())
        Assert.assertEquals(20, adsInsertionPoints[1])
        Assert.assertEquals(JSONObject().toString(), itemList[11].toString())
        Assert.assertEquals(JSONObject().toString(), itemList[20].toString())
    }

    @Test
    fun insertAdNearEnd() {
        val adInsertHelper = AdInsertHelper(3, 6, 6, null, "article", listOf("related"))
        val itemList: MutableList<Any> = mutableListOf("article", "article", "article", "article", "article", "article", "article", "article", "article", "article")
        val adsInsertionPoints = adInsertHelper.fillAds(itemList)
        Assert.assertEquals(3, adsInsertionPoints.first())
        Assert.assertEquals(10, adsInsertionPoints[1])
        Assert.assertEquals(JSONObject().toString(), itemList[3].toString())
        Assert.assertEquals(JSONObject().toString(), itemList[10].toString())
    }
}