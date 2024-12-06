package com.olam.digital.ofis.authentication;

import com.olam.digital.ofis.authentication.model.AuthenticationDTO;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthenticationInterface {

    // Keycloak URL
    @FormUrlEncoded
    @POST("token")
    Call<AuthenticationDTO> getKeyCloakAuthToken(@Field("client_id") String client_id, @Field("username") String username,
                                                 @Field("password") String password, @Field("grant_type") String grant_type,
                                                 @Field("client_secret") String client_secret,@Field("scope") String scope);

    @FormUrlEncoded
    @POST("token")
    Call<AuthenticationDTO> getKeyCloakRefreshToken(@Field("client_id") String client_id, @Field("client_secret") String client_secret,
                                                 @Field("grant_type") String grant_type, @Field("refresh_token") String refresh_token);


}
