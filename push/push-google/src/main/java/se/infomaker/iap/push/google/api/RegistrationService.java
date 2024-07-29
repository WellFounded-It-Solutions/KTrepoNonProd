package se.infomaker.iap.push.google.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RegistrationService {
    @POST
    Call<RegistrationResult> register(@Url String url, @Body Registration registration);

    @POST
    Call<RegistrationResult> unregister(@Url String url, @Body Registration registration);
}
