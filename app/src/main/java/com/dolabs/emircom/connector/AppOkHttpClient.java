package com.dolabs.emircom.connector;



import static com.dolabs.emircom.Config.IS_PRODUCTION;

import androidx.annotation.NonNull;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;


public class AppOkHttpClient {

    private static final String TAG = AppOkHttpClient.class.getSimpleName();
    private static final int connectionTimeout = 100;
    private static final int socketTimeout = 100;

    private static final String UNDEFINED = "UNDEFINED";

    @NonNull
    public static OkHttpClient GetOkHttpClient() {
        return GetOkHttpClientBuilder().build();
    }

    public static OkHttpClient.Builder GetOkHttpClientBuilder() {
        return IS_PRODUCTION ? GetSafeOkHttpClientBuilder() : GetUnsafeOkHttpClientBuilder();
    }

    private static OkHttpClient.Builder GetSafeOkHttpClientBuilder() {

        return new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(socketTimeout, TimeUnit.SECONDS)
                .writeTimeout(socketTimeout, TimeUnit.SECONDS);
    }

    private static OkHttpClient.Builder GetUnsafeOkHttpClientBuilder() {

        try {

            final TrustManager[] trustAllCerts = new TrustManager[] {

                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);

            builder.hostnameVerifier((hostname, session) -> {
                return CheckHostName(hostname);
            });

            builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS);
            builder.readTimeout(socketTimeout, TimeUnit.SECONDS);
            builder.writeTimeout(socketTimeout, TimeUnit.SECONDS);

            return builder;
        }
        catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static boolean CheckHostName(String hostname) {

        /*if(hostname != null && !hostname.isEmpty()) {

            hostname = hostname.toLowerCase();

            if (hostname.endsWith(HOST_NAME) ||

                    hostname.contains("img.youtube.com") ||
                    hostname.contains("data.flurry.com") ||
                    hostname.contains("api.crashlytics.com") ||
                    hostname.contains("settings.crashlytics.com")) {

                return true;
            }
        }

        return false;*/

        return true;
    }
}
