package com.webpoint0.ofiscompanion.authentication;

import com.webpoint0.ofiscompanion.authentication.model.AuthRequestDTO;
import com.webpoint0.ofiscompanion.authentication.model.AuthenticationDTO;
import com.webpoint0.ofiscompanion.authentication.model.RefreshTokenDTO;

import retrofit2.Call;
import retrofit2.http.Body;
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

    @POST("refresh")
    Call<AuthenticationDTO> getCoreAccessTokenFromRefreshToken(@Body RefreshTokenDTO refreshTokenDTO);

}
