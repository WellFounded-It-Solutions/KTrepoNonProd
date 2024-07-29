package se.infomaker.iap.articleview.preprocessor

import java.util.UUID
import kotlin.random.Random

val Any?.reproducibleUuid: UUID
    get() = UUID.nameUUIDFromBytes(Random(hashCode()).nextBytes(16))