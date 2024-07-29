package se.infomaker.frt.integration

import android.content.Context


@Deprecated(
    message = "NearMe as a module name for the module where you can follow just does not make sense " +
            "without the location part.",
    replaceWith = ReplaceWith(
        expression = "FollowIntegration",
        imports = ["se.infomaker.frt.integration.FollowIntegration"]
    )
)
class NearMeIntegration(context: Context, id: String) : FollowIntegration(context, id)