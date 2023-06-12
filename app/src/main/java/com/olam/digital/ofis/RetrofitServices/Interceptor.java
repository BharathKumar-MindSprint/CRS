package com.olam.digital.ofis.RetrofitServices;

import static com.olam.digital.ofis.EnableMultiDex.context;
import android.os.SystemClock;
import android.widget.Toast;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.KeyCloakInfo;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import java.io.IOException;
import okhttp3.Dispatcher;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class Interceptor implements okhttp3.Interceptor {
    private static String accessToken;
    private static Response new_response;
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Authorization","Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken());
        Request request = builder.build();
        Response response = chain.proceed(request);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1);
        if (response.code()==401) {
            AuthenticationDTO authenticationDTO = new AuthenticationHelper(context).getAuthenticationDTO();
            String clientId= Utilities.getKeyCloakClientDetails(context, KeyCloakInfo.Client_Id);
            String clientSecret=Utilities.getKeyCloakClientDetails(context,KeyCloakInfo.Client_Secret);
            String grantType=context.getResources().getString(R.string.refresh_token);
            String refreshToken=authenticationDTO.getRefreshToken();
            // Keycloak Refresh Token Call
            AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, context);
            Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakRefreshToken(clientId,clientSecret,grantType,refreshToken);

            authCall.enqueue(new Callback<AuthenticationDTO>() {
                @Override
                public void onResponse(Call<AuthenticationDTO> call, retrofit2.Response<AuthenticationDTO> response) {
                    if (response.isSuccessful()) {
                        AuthenticationDTO newAuthDTO = (AuthenticationDTO) response.body();
                        newAuthDTO.setWillExpireAt((System.currentTimeMillis()/1000) + newAuthDTO.getExpiresIn() /1000);
                        new AuthenticationHelper(context).setAuthenticationDTO(newAuthDTO);
                        accessToken=newAuthDTO.getAccessToken();
                        Request.Builder builder_res = original.newBuilder()
                                .header("Authorization","Bearer "+accessToken);
                        Request request_res = builder_res.build();
                        try {
                            new_response = chain.proceed(request_res);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(context,R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthenticationDTO> call, Throwable t) {
                    Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            SystemClock.sleep(4000);
            return new_response;
        }
        return response;
    }
}
