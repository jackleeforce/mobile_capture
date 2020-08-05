package com.jacklee.mobilecapture;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Connection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Author JackLee
 */
class HttpCommunication {

    private boolean sslEnabled = false;
    private Connection connection;
    private String serviceUrl;
    private boolean twoWayAuthEnabled = false;
    private Context context;
    private String certificateFile;
    private String certificatePassword = "";
    private String caFile;
    private String privateKeyFile;
    private String protocol = "TLSv1.3";


    public HttpCommunication(boolean sslEnabled, String serviceUrl, boolean twoWayAuthEnabled, Context context) {
        this.sslEnabled = sslEnabled;
        this.serviceUrl = serviceUrl;
        this.twoWayAuthEnabled = twoWayAuthEnabled;
        this.context = context;
    }

    private X509TrustManager getTrustManager(){

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            return trustManager;
        }
        catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    /**
     * Get retrofit.
     *
     * @return Retrofit instance
     */
    public Retrofit getRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false);

        if (sslEnabled) {
            SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
            builder = builder.sslSocketFactory(sslSocketFactory, getTrustManager())
                    .hostnameVerifier((s, sslSession) -> true);
        }

        OkHttpClient okHttpClient = builder.addNetworkInterceptor(chain -> {

            connection = chain.connection();

            Request request = chain.request();

            Response response = chain.proceed(request);

            return response;
        }).build();


        return new Retrofit.Builder()
                .baseUrl(serviceUrl)
                .client(okHttpClient)
                .build();

    }

    /**
     * Get SSL Socket Factory
     *
     * @return SSLSocketFactory instance
     */
    private SSLSocketFactory getSSLSocketFactory() {

        try {
            AssetManager assetManager = context.getAssets();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            SSLContext sslContext = SSLContext.getInstance(protocol);
            Timber.i("--->使用的证书：" + certificateFile);

            if (twoWayAuthEnabled) {

                InputStream clientCertIn = assetManager.open(certificateFile);
                InputStream clientPrivateKeyIn = assetManager.open(privateKeyFile);
                InputStream caFileIn = assetManager.open(caFile);


                Certificate certificate = certificateFactory.generateCertificate(caFileIn);
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                trustStore.setCertificateEntry("ca", certificate);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);

                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                final X509TrustManager origTrustmanager = (X509TrustManager) trustManagers[0];

                TrustManager[] wrappedTrustManagers = new TrustManager[]{
                        new X509TrustManager() {

                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                origTrustmanager.checkClientTrusted(x509Certificates, s);
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                origTrustmanager.checkServerTrusted(x509Certificates, s);
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return origTrustmanager.getAcceptedIssuers();
                            }
                        }
                };
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);

                Certificate clientCert = certificateFactory.generateCertificate(clientCertIn);

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                byte[] data = new byte[4096];
                int count = -1;
                while ((count = clientPrivateKeyIn.read(data, 0, 4096)) != -1)
                    outStream.write(data, 0, count);

                String key = new String(outStream.toByteArray(), "ISO-8859-1");

                StringBuilder pkcs8Lines = new StringBuilder();
                BufferedReader rdr = new BufferedReader(new StringReader(key));
                String line;
                while ((line = rdr.readLine()) != null) {
                    pkcs8Lines.append(line);
                }

                String pkcs8Pem = pkcs8Lines.toString();
                pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
                pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
                pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

                byte[] encoderByte = Base64.decode(pkcs8Pem, Base64.DEFAULT);

                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoderByte);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = kf.generatePrivate(keySpec);
                keyStore.setKeyEntry("client", privateKey, null, new Certificate[]{clientCert});

                keyManagerFactory.init(keyStore, null);

                sslContext.init(keyManagerFactory.getKeyManagers(), wrappedTrustManagers, null);

                clientCertIn.close();
                clientPrivateKeyIn.close();
                caFileIn.close();

                return sslContext.getSocketFactory();
            } else {
                InputStream inputStream = assetManager.open(certificateFile);

                Certificate certificate = certificateFactory.generateCertificate(inputStream);

                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, certificatePassword.toCharArray());
                keystore.setCertificateEntry("ca", certificate);

                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
                trustManagerFactory.init(keystore);

                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                inputStream.close();

                return sslContext.getSocketFactory();

                // If you want to accept any server certificate, use the following code.
//                AcceptAllTrustManager tm = new AcceptAllTrustManager();
//                sslContext.init(null, new TrustManager[] { tm }, null);
//                return sslContext.getSocketFactory();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
