package se.infomaker.frtutilities

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Test
import java.util.HashMap

class MapUtilKtTest2 {
    val before: Map<Any, Any>
    val toMerge: Map<Any, Any>
    val expectedResult: Map<Any, Any>

    val beforeString = """
        {
    "liveContent": {
        "authorField": "Author",
        "catchupLimit": 100,
        "conceptField": "RelatedConceptTagsUuid",
        "dateFormat": "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "defaultPropertyMap": "Article",
        "eventNotifierBroadcastId": "all-gota-test",
        "geoPointsKey": "GeoPoints",
        "infocaster": "wss://infocaster.lcc.infomaker.io/v1/publisher/open/session",
        "locationSearch": {
            "baseQuery": "ConceptImType:place",
            "contentProvider": "framtidningen",
            "contentType": "Concept",
            "geometryKey": "ConceptGeometry",
            "locationNameProperty": "ConceptNameString",
            "sortKey": "ConceptNameString"
        },
        "opencontent": "http://xlibris.test.oc.gota.infomaker.io:8080",
        "opencontentUsername": "bt",
        "pagingLimit": 20,
        "querystreamer": "https://querystreamer.lcc.infomaker.io/v1/stream-provider/gota-test/",
        "querystreamerUsername": "gota-test",
        "search": {
            "baseQuery": "PubStatus:usable AND -Tags:foobar",
            "contentProvider": "framtidningen",
            "contentType": "Article",
            "propertyQueryParams": {
                "AuthorConcepts": {
                    "q": "ConceptStatus:usable"
                },
                "Categories": {
                    "q": "ConceptStatus:usable"
                },
                "Geometries": {
                    "q": "ConceptStatus:usable"
                },
                "Stories": {
                    "q": "ConceptStatus:usable"
                },
                "TagConcepts": {
                    "q": "ConceptStatus:usable"
                }
            },
            "publicationDateKey": "Pubdate",
            "sortKey": "Publiceringsdag"
        },
        "storyField": "RelatedConceptStoriesUuid",
        "stream": {
            "baseQuery": {
                "must": [
                    {
                        "term": {
                            "PubStatus": "usable"
                        }
                    },
                    {
                        "match": {
                            "group": "BT"
                        }
                    }
                ]
            },
            "contentProvider": "ignore"
        },
        "streamRefreshThreshold": 2,
        "timeZone": "UTC",
        "transformSettings": {
            "formatReplace": {}
        },
        "typePropertyMap": {
            "Article": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "articleDateline": {
                    "name": "ArticleDateline"
                },
                "ArticleHeadline": {
                    "name": "ArticleHeadline"
                },
                "author": {
                    "name": "AuthorConcepts",
                    "propertyMapReference": "Author"
                },
                "categories": {
                    "name": "Categories",
                    "propertyMapReference": "FollowableConcept"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "geometries": {
                    "name": "Geometries",
                    "propertyMapReference": "FollowableConcept"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "newsML": {
                    "name": "ArticleBodyRaw"
                },
                "priority": {
                    "name": "NewsValue"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "relatedLinks": {
                    "name": "RelatedLinks"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "stories": {
                    "name": "Stories",
                    "propertyMapReference": "FollowableConcept"
                },
                "tags": {
                    "name": "TagConcepts",
                    "propertyMapReference": "FollowableConcept"
                },
                "teaserHeadline": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserImageCrop": {
                    "name": "TeaserImageCrop2to1"
                },
                "teaserImageHeight": {
                    "name": "TeaserImageHeight"
                },
                "teaserImageUuid": {
                    "name": "TeaserImageUuid"
                },
                "teaserImageWidth": {
                    "name": "TeaserImageWidth"
                },
                "teaserLeadin": {
                    "name": "TeaserText",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserText": {
                    "name": "TeaserText"
                }
            },
            "Author": {
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "email": {
                    "name": "Email"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "FollowableConcept": {
                "contentId": {
                    "name": "uuid"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "List": {
                "articles": {
                    "name": "ArticleRelations",
                    "propertyMapReference": "Article"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "type": {
                    "name": "type"
                }
            },
            "Package": {
                "category": {
                    "name": "category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "list": {
                    "name": "ListRelation",
                    "propertyMapReference": "List"
                },
                "pubStart": {
                    "name": "PubStart"
                }
            },
            "SlimPackage": {
                "category": {
                    "name": "category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "pubStart": {
                    "name": "PubStart"
                }
            },
            "TeaserArticle": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "teaserHeadline": {
                    "name": "TeaserVignette",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserLeadin": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                }
            }
        }
    }
}
        """

    val toMergeString = """
    {
    "liveContent": {
        "authorField": "Author",
        "catchupLimit": 100,
        "conceptField": "RelatedConceptTagsUuid",
        "dateFormat": "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "defaultPropertyMap": "Article",
        "eventNotifierBroadcastId": "all-gota-test",
        "geoPointsKey": "GeoPoints",
        "locationSearch": {
            "baseQuery": "ConceptImType:place",
            "contentProvider": "framtidningen",
            "contentType": "Concept",
            "geometryKey": "ConceptGeometry",
            "locationNameProperty": "ConceptNameString",
            "sortKey": "ConceptNameString"
        },
        "opencontent": "http://xlibris.test.oc.gota.infomaker.io:8080",
        "opencontentUsername": "bt",
        "pagingLimit": 50,
        "querystreamer": "https://querystreamer.lcc.infomaker.io/v1/stream-provider/gota-test/",
        "querystreamerUsername": "gota-test",
        "storyField": "RelatedConceptStoriesUuid",
        "stream": {
            "baseQuery": {},
            "contentProvider": "ignore"
        },
        "streamRefreshThreshold": 2,
        "timeZone": "UTC",
        "transformSettings": null,
        "typePropertyMap": {
            "Article": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "articleDateline": {
                    "name": "ArticleDateline"
                },
                "ArticleHeadline": {
                    "name": "ArticleHeadline"
                },
                "author": {
                    "name": "AuthorConcepts",
                    "propertyMapReference": "Author"
                },
                "categories": {
                    "name": "Categories",
                    "propertyMapReference": "FollowableConcept"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "geometries": {
                    "name": "Geometries",
                    "propertyMapReference": "FollowableConcept"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "newsML": {
                    "name": "ArticleBodyRaw"
                },
                "priority": {
                    "name": "NewsValue"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "relatedLinks": {
                    "name": "RelatedLinks"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "stories": {
                    "name": "Stories",
                    "propertyMapReference": "FollowableConcept"
                },
                "tags": {
                    "name": "TagConcepts",
                    "propertyMapReference": "FollowableConcept"
                },
                "teaserHeadline": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserImageCrop": {
                    "name": "TeaserImageCrop2to1"
                },
                "teaserImageHeight": {
                    "name": "TeaserImageHeight"
                },
                "teaserImageUuid": {
                    "name": "TeaserImageUuid"
                },
                "teaserImageWidth": {
                    "name": "TeaserImageWidth"
                },
                "teaserLeadin": {
                    "name": "TeaserText",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                }
            },
            "Author": {
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "email": {
                    "name": "Email"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "FollowableConcept": {
                "contentId": {
                    "name": "uuid"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "List": {
                "articles": {
                    "name": "ArticleRelations",
                    "propertyMapReference": "Article"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "type": {
                    "name": "type"
                }
            },
            "Package": {
                "category": {
                    "name": "category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "list": {
                    "name": "ListRelation",
                    "propertyMapReference": "List"
                },
                "publicationDate": {
                    "name": "pubStart"
                }
            },
            "SlimPackage": {
                "category": {
                    "name": "Category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "publicationDate": {
                    "name": "PubStart"
                }
            },
            "TeaserArticle": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "teaserHeadline": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserLeadin": {
                    "name": "TeaserText",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                }
            }
        }
    }
}
    """

    val expectedResultString = """
    {
    "liveContent": {
        "authorField": "Author",
        "catchupLimit": 100,
        "conceptField": "RelatedConceptTagsUuid",
        "dateFormat": "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "defaultPropertyMap": "Article",
        "eventNotifierBroadcastId": "all-gota-test",
        "geoPointsKey": "GeoPoints",
        "infocaster": "wss://infocaster.lcc.infomaker.io/v1/publisher/open/session",
        "locationSearch": {
            "baseQuery": "ConceptImType:place",
            "contentProvider": "framtidningen",
            "contentType": "Concept",
            "geometryKey": "ConceptGeometry",
            "locationNameProperty": "ConceptNameString",
            "sortKey": "ConceptNameString"
        },
        "opencontent": "http://xlibris.test.oc.gota.infomaker.io:8080",
        "opencontentUsername": "bt",
        "pagingLimit": 50,
        "querystreamer": "https://querystreamer.lcc.infomaker.io/v1/stream-provider/gota-test/",
        "querystreamerUsername": "gota-test",
        "search": {
            "baseQuery": "PubStatus:usable AND -Tags:foobar",
            "contentProvider": "framtidningen",
            "contentType": "Article",
            "propertyQueryParams": {
                "AuthorConcepts": {
                    "q": "ConceptStatus:usable"
                },
                "Categories": {
                    "q": "ConceptStatus:usable"
                },
                "Geometries": {
                    "q": "ConceptStatus:usable"
                },
                "Stories": {
                    "q": "ConceptStatus:usable"
                },
                "TagConcepts": {
                    "q": "ConceptStatus:usable"
                }
            },
            "publicationDateKey": "Pubdate",
            "sortKey": "Publiceringsdag"
        },
        "storyField": "RelatedConceptStoriesUuid",
        "stream": {
            "baseQuery": {
                "must": [
                    {
                        "term": {
                            "PubStatus": "usable"
                        }
                    },
                    {
                        "match": {
                            "group": "BT"
                        }
                    }
                ]
            },
            "contentProvider": "ignore"
        },
        "streamRefreshThreshold": 2,
        "timeZone": "UTC",
        "typePropertyMap": {
            "Article": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "articleDateline": {
                    "name": "ArticleDateline"
                },
                "ArticleHeadline": {
                    "name": "ArticleHeadline"
                },
                "author": {
                    "name": "AuthorConcepts",
                    "propertyMapReference": "Author"
                },
                "categories": {
                    "name": "Categories",
                    "propertyMapReference": "FollowableConcept"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "geometries": {
                    "name": "Geometries",
                    "propertyMapReference": "FollowableConcept"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "newsML": {
                    "name": "ArticleBodyRaw"
                },
                "priority": {
                    "name": "NewsValue"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "relatedLinks": {
                    "name": "RelatedLinks"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "stories": {
                    "name": "Stories",
                    "propertyMapReference": "FollowableConcept"
                },
                "tags": {
                    "name": "TagConcepts",
                    "propertyMapReference": "FollowableConcept"
                },
                "teaserHeadline": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserImageCrop": {
                    "name": "TeaserImageCrop2to1"
                },
                "teaserImageHeight": {
                    "name": "TeaserImageHeight"
                },
                "teaserImageUuid": {
                    "name": "TeaserImageUuid"
                },
                "teaserImageWidth": {
                    "name": "TeaserImageWidth"
                },
                "teaserLeadin": {
                    "name": "TeaserText",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserText": {
                    "name": "TeaserText"
                }
            },
            "Author": {
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "email": {
                    "name": "Email"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "FollowableConcept": {
                "contentId": {
                    "name": "uuid"
                },
                "name": {
                    "name": "ConceptName"
                }
            },
            "List": {
                "articles": {
                    "name": "ArticleRelations",
                    "propertyMapReference": "Article"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "type": {
                    "name": "type"
                }
            },
            "Package": {
                "category": {
                    "name": "category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "list": {
                    "name": "ListRelation",
                    "propertyMapReference": "List"
                },
                "publicationDate": {
                    "name": "pubStart"
                },
                "pubStart": {
                    "name": "PubStart"
                }
            },
            "SlimPackage": {
                "category": {
                    "name": "Category"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "coverArticle": {
                    "name": "CoverRelation",
                    "propertyMapReference": "TeaserArticle"
                },
                "publicationDate": {
                    "name": "PubStart"
                },
                "pubStart": {
                    "name": "PubStart"
                }
            },
            "TeaserArticle": {
                "articleContent": {
                    "name": "ArticleContent"
                },
                "articleContentType": {
                    "name": "ArticleContentType"
                },
                "contentId": {
                    "name": "uuid"
                },
                "contentType": {
                    "name": "contenttype"
                },
                "imageHeight": {
                    "name": "ImageHeights"
                },
                "imageId": {
                    "name": "ImageUuids"
                },
                "imageWidth": {
                    "name": "ImageWidths"
                },
                "publicationDate": {
                    "name": "Pubdate"
                },
                "showContentExplanation": {
                    "name": "ShowContentExplanation"
                },
                "teaserHeadline": {
                    "name": "TeaserHeadline",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                },
                "teaserLeadin": {
                    "name": "TeaserText",
                    "transforms": [
                        "formatReplace",
                        "htmlStrip"
                    ]
                }
            }
        }
    }
}
    """

    val gson = GsonBuilder().create()

    init {
        before = gson.fromJson<Map<Any, Any>>(beforeString, HashMap::class.java)
        toMerge = gson.fromJson<Map<Any, Any>>(toMergeString, HashMap::class.java)
        expectedResult = gson.fromJson<Map<Any, Any>>(expectedResultString, HashMap::class.java)
    }

    @Test
    fun putRecursive() {
        val mutableMap = mutableMapOf<Any, Any>()
        mutableMap.putAll(before)

        mutableMap.putRecursive(toMerge)
        val jsonParser = JsonParser()
        val mapElement = jsonParser.parse(gson.toJson(mutableMap).toString())
        val expectedResultElement = jsonParser.parse(gson.toJson(expectedResult).toString())
        assertEquals(expectedResultElement, mapElement)
        assertFalse(mutableMap.containsKey("cutme"))
    }
}