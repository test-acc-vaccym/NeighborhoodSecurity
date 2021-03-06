package com.moscowmuleaddicted.neighborhoodsecurity.controller.rest;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.android.gms.internal.zzt.TAG;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.AUTH_TOKEN;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.SERVICE_KEY;

/**
 * Implementation of {@link Interceptor} in order to attach extra content to the request header.
 * New headers:
 * <ul>
 *     <li>Accept: application/json</li>
 *     <li>service-key: SERVICE_KEY</li>
 *     <li>auth-token: AUTH_TOKEN</li>
 * </ul>
 *
 * @author Simone Ripamonti
 * @version 1
 */

public final class HeaderRequestInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String token = "";
        if (user != null){
            Task<GetTokenResult> getTokenTask = user.getToken(true);
            try {
                Tasks.await(getTokenTask);
                token = getTokenTask.getResult().getToken();
                Log.i(TAG, "interceptHeaderRequest:found user token "+token);
            } catch (ExecutionException | InterruptedException e) {
                Log.w(TAG, "interceptHeaderRequest:failure in finding user token", e);
            }

        }

        Request originalRequest = chain.request();
        Request newRequest = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header(SERVICE_KEY, "moscowmule")
                .header(AUTH_TOKEN, token).build();
        Log.i(TAG, "interceptHeaderRequest:proceeding request" );
        return chain.proceed(newRequest);
    }
}
