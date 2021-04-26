package com.olam.digital.ofis.authentication;

import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.authentication.model.RefreshTokenDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthenticationInterface {
    @POST("oauth/token")
    Call<AuthenticationDTO> Authenticate(@Query("username") String username,
                                         @Query("password") String password,
                                         @Query("grant_type") String grant_type);

    @POST("oauth/token")
    Call<AuthenticationDTO> Refresh(@Query("refresh_token") String refreshToken,
                                    @Query("grant_type") String grant_type);

    @POST("authenticate")
    Call<AuthenticationDTO> getCoreAuthToken(@Body AuthRequestDTO authRequestDTO);

    @POST("token")
    Call<AuthenticationDTO> getRefreshToken(@Body AuthRequestDTO authRequestDTO);

    @POST("refresh")
    Call<AuthenticationDTO> getCoreAccessTokenFromRefreshToken(@Body RefreshTokenDTO refreshTokenDTO);

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
