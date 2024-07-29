package se.infomaker.frt.ui.fragment

@Deprecated(
    message = "NearMe as a module name for the module where you can follow just does not make sense " +
            "without the location part.",
    replaceWith = ReplaceWith(
        expression = "FollowFragment",
        imports = ["se.infomaker.frt.ui.fragment.FollowFragment"]
    )
)
class NearMeFragment : FollowFragment()