package se.infomaker.livecontentmanager.query.lcc.querystreamer;


import com.navigaglobal.mobile.di.InstallationIdentifier;
import com.navigaglobal.mobile.di.PackageName;
import com.navigaglobal.mobile.di.VersionName;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Meta {
    private final String platform = "android";
    private final String packageName;
    private final String version;
    private final String identifier;

    @Inject
    public Meta(@PackageName String packageName, @VersionName String version, @InstallationIdentifier String identifier) {
        this.packageName = packageName;
        this.version = version;
        this.identifier = identifier;
    }
}
