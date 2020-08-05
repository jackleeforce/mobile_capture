package com.jacklee.mobilecapture;

import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void httpBtnClick(View v) {

        HttpCommunication httpCommunication = new HttpCommunication(false,"http://test.jacklee.work",false,this);

        httpCommunication.getRetrofit().create()



        Toast.makeText(this,"httpClick",Toast.LENGTH_SHORT).show();
    }


    public void oneWayBtnClick(View v) {

        Toast.makeText(this,"oneWayBtnClick",Toast.LENGTH_SHORT).show();

    }


    public void twoWayBtnClick(View v) {
        Toast.makeText(this,"twoWayBtnClick",Toast.LENGTH_SHORT).show();
    }

}

/**
 * Interface for retrofit request.
 */
public interface HttpService {
    /**
     * send RequestBody and receive ResponseBody
     *
     * @param body request data
     * @return ObservableSource which emits ResponseBody
     */
    @GET("/")
    Call<ResponseBody> unsecurity(@Body RequestBody body);


    /**
     * 银联直连交易
     *
     * @param body
     * @return
     */
    @POST("/unp/webtrans/WPOS")
    @Headers({
            "User-Agent: Donjin Http 0.1",
            "Cache-Control: no-cache",
            "Content-Type:x-ISO-TPDU/x-auth",
            "Accept: */*"
    })
    Observable<ResponseBody> transactionUnionPayDirectLink(@Body RequestBody body);


    /**
     * 图片上传测试
     * @param cidImage
     * @param selfieImage
     * @param customerData
     * @return
     */
    @POST("/xxx")
    @Headers({
            "xxx: xxx",
    })
    Observable<ResponseBody> transactionUploadImage(@Part("cidImage") RequestBody cidImage, @Part("selfieImage") RequestBody selfieImage
            , @Part("customerData") RequestBody customerData);
}

