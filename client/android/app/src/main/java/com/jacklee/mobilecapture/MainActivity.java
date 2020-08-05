package com.jacklee.mobilecapture;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Interface for retrofit request.
     */
    private interface HttpService {

        @GET("/mobile-capture/v1/unsafe")
        Call<Response> unSafe();
    }

    public static class Response {
        public final String errorCode;
        public final String message;

        public Response(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }




    public void httpBtnClick(View v) {

        Completable.complete()
                .toSingle(() -> {
                    HttpCommunication httpCommunication = new HttpCommunication(false,"http://test.jacklee.work",false,MainActivity.this);

                    HttpService service = httpCommunication.getRetrofit().create(HttpService.class);

                    Call<Response> call = service.unSafe();

                    Response response = call.execute().body();

                    return response;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> Toast.makeText(MainActivity.this,"http response:"+response.message,Toast.LENGTH_SHORT).show(),
                        throwable -> Toast.makeText(MainActivity.this,"http request exception occurred:"+throwable.getMessage(),Toast.LENGTH_SHORT).show());
    }


    public void oneWayBtnClick(View v) {

        Toast.makeText(this,"oneWayBtnClick",Toast.LENGTH_SHORT).show();

    }


    public void twoWayBtnClick(View v) {
        Toast.makeText(this,"twoWayBtnClick",Toast.LENGTH_SHORT).show();
    }

}



