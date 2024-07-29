package se.infomaker.library

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import se.infomaker.library.keywords.KeyWord

class KeyWordResolverTest  {
    val content = """{
	"teaserImageWidth": ["3648"],
	"stories": [],
	"author": [{
		"contentId": ["2bfd8d58-2245-4960-b46a-c84860970976"],
		"name": ["Jennie Hilli Sjöqvist"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "2bfd8d58-2245-4960-b46a-c84860970976",
			"name": "Jennie Hilli Sjöqvist",
			"contentType": "Concept"
		}
	}],
	"contentId": ["55016a36-d6ba-42c9-ba38-e738cf48b91d"],
	"teaserMadmansRow": [{
		"contentId": ["411b26ba-aa2e-40c1-ae02-5243c6c8248a"],
		"name": ["Nyheter"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "411b26ba-aa2e-40c1-ae02-5243c6c8248a",
			"name": "Nyheter",
			"contentType": "Concept"
		}
	}],
	"ArticleHeadline": ["Stor medvetenhet inför ceremonier"],
	"newsML": ["<?xml version=\"1.0\" encoding=\"UTF-8\"?><idf xmlns=\"http:\/\/www.infomaker.se\/idf\/1.0\" xmlns:ils=\"http:\/\/www.infomaker.se\/lookupservice\" dir=\"ltr\" xml:lang=\"sv-SE\">\n            <group type=\"body\"><element id=\"headline-d1\" type=\"headline\">Stor medvetenhet inför ceremonier<\/element><element id=\"preamble-18e2bae848a88dd717f9bdd8d23ed570\" type=\"preamble\">Många har skjutit upp sina bröllop under pandemin, andra har minskat antalet besökare, konstaterar prästerna i Amnehärad-Lyrestad.<\/element><element id=\"preamble-73a3e5053f3899c70a5545f55b44627b\" type=\"preamble\">– Det har blivit fler besökare undan för undan, men hela tiden anpassat till situationen, säger Charlotte Holmgren.<\/element><element id=\"paragraph-ce3461f1aa0955650eb25177b866b1b0\" type=\"body\">Coronapandemin har resulterat i framflyttade vigseldatum, anpassade dop och uteblivna fester. Även begravningar har behövt begränsas. Charlotte Holmgren och Reine Medelius, som är präster i Amnehärad-Lyrestads pastorat, har upplevt en stor medvetenhet.<\/element><element id=\"paragraph-5d7d576a856dc2ddef309771d8f73929\" type=\"body\">– I början tänkte man att vi skulle behöva sätta fram handen och säga stopp i dörren, men det har löst sig. Det har inte varit några problem för oss, även om det såklart är tråkigt för de som har behövt flytta fram sina bröllop eller bjuda färre, säger Charlotte Holmgren.<\/element><object id=\"contentrelations-9196ea6867926e19921898172b6f8a49\" title=\"De gifte sig under pandemin\" type=\"x-im\/link\" uuid=\"a82c672b-efc5-48cb-a468-522518861390\"><links><link rel=\"self\" type=\"x-im\/article\" uuid=\"a82c672b-efc5-48cb-a468-522518861390\"\/><\/links><\/object><element id=\"paragraph-2f6053fc5eb59dadff7ab56461aa2c45\" type=\"body\">–  Folk har insett själva att om vi ska kunna genomföra vigseln eller begravningen så kan vi inte bli särskilt många, säger Reine Medelius.<\/element><object id=\"MTA5LDE1MCwxOTMsNDc\" type=\"x-im\/image\" uuid=\"1652be0e-e6ea-5923-8b73-74b55b5e11af\"><links><link rel=\"self\" type=\"x-im\/image\" uri=\"im:\/\/image\/PJl2xWiKEz_r-MCaZ1ncQkc91MM.jpg\" uuid=\"1652be0e-e6ea-5923-8b73-74b55b5e11af\"><data><width>3648<\/width><height>2432<\/height><text>Reine Medelius, präst i Amnehärad-Lyrestads pastorat.<\/text><alttext\/><\/data><links><link rel=\"author\" title=\"Charlotte Ferneman\" type=\"x-im\/author\" uuid=\"00000000-0000-0000-0000-000000000000\"\/><\/links><\/link><\/links><\/object><element id=\"paragraph-b412e4fd83f2e7cbb071f9eb8ac7223d\" type=\"body\">Kyrkan har inte behövt sätta upp särskilda förhållningsorder utan har upplevt sunt förnuft bland de som ska arrangera ceremonier. Färre besökare har varit långväga, många har ställt in och vid begravningar har bara den närmsta kretsen närvarat.<\/element><element id=\"paragraph-fd9e703edfa03ffdf604e6643ec2def6\" type=\"body\">– Samlingar efter begravningar har nästan pausats helt. Människor har insett att det nog inte går, säger Reine Medelius.<\/element><element id=\"paragraph-26b2b6230d1cea925e33502f3f631eec\" type=\"body\">Ett par ceremonier har kunnat hållas utomhus och vissa har sänt live för de som inte har kunnat delta på plats. <\/element><\/group>\n         <\/idf>"],
	"teaserImageUuid": ["1652be0e-e6ea-5923-8b73-74b55b5e11af"],
	"tags": [{
		"contentId": ["0ff8abd7-499a-4009-b91c-5707566f8494"],
		"name": ["Charlotte Holmgren"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "0ff8abd7-499a-4009-b91c-5707566f8494",
			"name": "Charlotte Holmgren",
			"contentType": "Concept"
		}
	}, {
		"contentId": ["eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87"],
		"name": ["Reine Medelius"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87",
			"name": "Reine Medelius",
			"contentType": "Concept"
		}
	}],
	"newsValue": ["4"],
	"relatedArticles": [],
	"teaserHeadline": ["Stor medvetenhet inför ceremonier"],
	"geometries": [{
		"contentId": ["20196d7a-0355-489f-a57b-2857a2c91790"],
		"name": ["Gullspång"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "20196d7a-0355-489f-a57b-2857a2c91790",
			"name": "Gullspång",
			"contentType": "Concept"
		}
	}, {
		"contentId": ["0cf64e95-cfe7-485a-87d3-6573f2468c8d"],
		"name": ["Gullspångs kommun"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "0cf64e95-cfe7-485a-87d3-6573f2468c8d",
			"name": "Gullspångs kommun",
			"contentType": "Concept"
		}
	}, {
		"contentId": ["f95c3381-0243-473e-a6d5-0bb5df084fcd"],
		"name": ["Skaraborg"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "f95c3381-0243-473e-a6d5-0bb5df084fcd",
			"name": "Skaraborg",
			"contentType": "Concept"
		}
	}, {
		"contentId": ["72a155bf-2f83-4a46-9320-a985012a0fbb"],
		"name": ["Västra Götalands län"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "72a155bf-2f83-4a46-9320-a985012a0fbb",
			"name": "Västra Götalands län",
			"contentType": "Concept"
		}
	}],
	"conceptUuids": ["2bfd8d58-2245-4960-b46a-c84860970976", "411b26ba-aa2e-40c1-ae02-5243c6c8248a", "0ff8abd7-499a-4009-b91c-5707566f8494", "eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87", "20196d7a-0355-489f-a57b-2857a2c91790", "0cf64e95-cfe7-485a-87d3-6573f2468c8d", "f95c3381-0243-473e-a6d5-0bb5df084fcd", "72a155bf-2f83-4a46-9320-a985012a0fbb"],
	"isPremium": ["true"],
	"categories": [{
		"contentId": ["411b26ba-aa2e-40c1-ae02-5243c6c8248a"],
		"name": ["Nyheter"],
		"contentType": ["Concept"],
		"INTERNAL-Description": {
			"contentId": "411b26ba-aa2e-40c1-ae02-5243c6c8248a",
			"name": "Nyheter",
			"contentType": "Concept"
		}
	}],
	"contentType": ["Article"],
	"publicationDate": ["2020-09-15T17:30:00Z"],
	"teaserImageHeight": ["2432"],
	"INTERNAL-Description": {
		"teaserImageWidth": "3648",
		"author": "Author",
		"contentId": "55016a36-d6ba-42c9-ba38-e738cf48b91d",
		"teaserMadmansRow": "FollowableConcept",
		"ArticleHeadline": "Stor medvetenhet inför ceremonier",
		"newsML": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><idf xmlns=\"http:\/\/www.infomaker.se\/idf\/1.0\" xmlns:ils=\"http:\/\/www.infomaker.se\/lookupservice\" dir=\"ltr\" xml:lang=\"sv-SE\">\n            <group type=\"body\"><element id=\"headline-d1\" type=\"headline\">Stor medvetenhet inför ceremonier<\/element><element id=\"preamble-18e2bae848a88dd717f9bdd8d23ed570\" type=\"preamble\">Många har skjutit upp sina bröllop under pandemin, andra har minskat antalet besökare, konstaterar prästerna i Amnehärad-Lyrestad.<\/element><element id=\"preamble-73a3e5053f3899c70a5545f55b44627b\" type=\"preamble\">– Det har blivit fler besökare undan för undan, men hela tiden anpassat till situationen, säger Charlotte Holmgren.<\/element><element id=\"paragraph-ce3461f1aa0955650eb25177b866b1b0\" type=\"body\">Coronapandemin har resulterat i framflyttade vigseldatum, anpassade dop och uteblivna fester. Även begravningar har behövt begränsas. Charlotte Holmgren och Reine Medelius, som är präster i Amnehärad-Lyrestads pastorat, har upplevt en stor medvetenhet.<\/element><element id=\"paragraph-5d7d576a856dc2ddef309771d8f73929\" type=\"body\">– I början tänkte man att vi skulle behöva sätta fram handen och säga stopp i dörren, men det har löst sig. Det har inte varit några problem för oss, även om det såklart är tråkigt för de som har behövt flytta fram sina bröllop eller bjuda färre, säger Charlotte Holmgren.<\/element><object id=\"contentrelations-9196ea6867926e19921898172b6f8a49\" title=\"De gifte sig under pandemin\" type=\"x-im\/link\" uuid=\"a82c672b-efc5-48cb-a468-522518861390\"><links><link rel=\"self\" type=\"x-im\/article\" uuid=\"a82c672b-efc5-48cb-a468-522518861390\"\/><\/links><\/object><element id=\"paragraph-2f6053fc5eb59dadff7ab56461aa2c45\" type=\"body\">–  Folk har insett själva att om vi ska kunna genomföra vigseln eller begravningen så kan vi inte bli särskilt många, säger Reine Medelius.<\/element><object id=\"MTA5LDE1MCwxOTMsNDc\" type=\"x-im\/image\" uuid=\"1652be0e-e6ea-5923-8b73-74b55b5e11af\"><links><link rel=\"self\" type=\"x-im\/image\" uri=\"im:\/\/image\/PJl2xWiKEz_r-MCaZ1ncQkc91MM.jpg\" uuid=\"1652be0e-e6ea-5923-8b73-74b55b5e11af\"><data><width>3648<\/width><height>2432<\/height><text>Reine Medelius, präst i Amnehärad-Lyrestads pastorat.<\/text><alttext\/><\/data><links><link rel=\"author\" title=\"Charlotte Ferneman\" type=\"x-im\/author\" uuid=\"00000000-0000-0000-0000-000000000000\"\/><\/links><\/link><\/links><\/object><element id=\"paragraph-b412e4fd83f2e7cbb071f9eb8ac7223d\" type=\"body\">Kyrkan har inte behövt sätta upp särskilda förhållningsorder utan har upplevt sunt förnuft bland de som ska arrangera ceremonier. Färre besökare har varit långväga, många har ställt in och vid begravningar har bara den närmsta kretsen närvarat.<\/element><element id=\"paragraph-fd9e703edfa03ffdf604e6643ec2def6\" type=\"body\">– Samlingar efter begravningar har nästan pausats helt. Människor har insett att det nog inte går, säger Reine Medelius.<\/element><element id=\"paragraph-26b2b6230d1cea925e33502f3f631eec\" type=\"body\">Ett par ceremonier har kunnat hållas utomhus och vissa har sänt live för de som inte har kunnat delta på plats. <\/element><\/group>\n         <\/idf>",
		"teaserImageUuid": "1652be0e-e6ea-5923-8b73-74b55b5e11af",
		"tags": "FollowableConcept, FollowableConcept",
		"newsValue": "4",
		"teaserHeadline": "Stor medvetenhet inför ceremonier",
		"geometries": "FollowableConcept, FollowableConcept, FollowableConcept, FollowableConcept",
		"conceptUuids": "2bfd8d58-2245-4960-b46a-c84860970976, 411b26ba-aa2e-40c1-ae02-5243c6c8248a, 0ff8abd7-499a-4009-b91c-5707566f8494, eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87, 20196d7a-0355-489f-a57b-2857a2c91790, 0cf64e95-cfe7-485a-87d3-6573f2468c8d, f95c3381-0243-473e-a6d5-0bb5df084fcd, 72a155bf-2f83-4a46-9320-a985012a0fbb",
		"isPremium": "true",
		"categories": "FollowableConcept",
		"contentType": "Article",
		"publicationDate": "2020-09-15T17:30:00Z",
		"teaserImageHeight": "2432"
	}
}"""
    @Test
    fun multiLeafTest() {
        val content = """{
	        "conceptUuids": ["2bfd8d58-2245-4960-b46a-c84860970976", "411b26ba-aa2e-40c1-ae02-5243c6c8248a", "0ff8abd7-499a-4009-b91c-5707566f8494", "eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87", "20196d7a-0355-489f-a57b-2857a2c91790", "0cf64e95-cfe7-485a-87d3-6573f2468c8d", "f95c3381-0243-473e-a6d5-0bb5df084fcd", "72a155bf-2f83-4a46-9320-a985012a0fbb"],
        }"""
        val keyWord = KeyWord("out", "content", "content", "conceptUuids", null)
        val value = keyWord.resolveValue(listOf(JSONObject(content)), JSONObject())
        Assert.assertEquals("2bfd8d58-2245-4960-b46a-c84860970976", value)
    }

    @Test
    fun multiLeafMappingTest() {
        val content = """{
	        "conceptUuids": ["2bfd8d58-2245-4960-b46a-c84860970976", "411b26ba-aa2e-40c1-ae02-5243c6c8248a", "0ff8abd7-499a-4009-b91c-5707566f8494", "eb8d6b8b-959c-4c7d-a7c3-b46c4ad4ed87", "20196d7a-0355-489f-a57b-2857a2c91790", "0cf64e95-cfe7-485a-87d3-6573f2468c8d", "f95c3381-0243-473e-a6d5-0bb5df084fcd", "72a155bf-2f83-4a46-9320-a985012a0fbb"],
        }"""
        val keyWord = KeyWord("out", null, "content", "conceptUuids", mapOf("0ff8abd7-499a-4009-b91c-5707566f8494" to "success"))
        val value = keyWord.resolveValue(listOf(JSONObject(content)), JSONObject())
        Assert.assertEquals("success", value)
    }

    @Test
    fun multipleBranchesWithLeafsTest() {
        val content = """{"geometries": [{ "contentId": ["20196d7a-0355-489f-a57b-2857a2c91790"]}, {"contentId": ["0cf64e95-cfe7-485a-87d3-6573f2468c8d"]}, {"contentId": ["f95c3381-0243-473e-a6d5-0bb5df084fcd"]}, {"contentId": ["72a155bf-2f83-4a46-9320-a985012a0fbb"]}]}"""
        val keyWord = KeyWord("out", null, "content", "geometries.contentId", mapOf("0cf64e95-cfe7-485a-87d3-6573f2468c8d" to "Götelaborg"))
        val value = keyWord.resolveValue(listOf(JSONObject(content)), JSONObject())
        Assert.assertEquals("Götelaborg", value)
    }
}