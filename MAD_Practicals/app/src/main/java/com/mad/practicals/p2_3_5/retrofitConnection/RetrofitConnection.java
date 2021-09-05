package com.mad.practicals.p2_3_5.retrofitConnection;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {
    private static RetrofitConnection instance = null;
    private StudentRecordsApi studentRecordsApi;

    private RetrofitConnection(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StudentRecordsApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        studentRecordsApi = retrofit.create(StudentRecordsApi.class);
    }

    public static synchronized RetrofitConnection getInstance(){
        if(instance==null){
            instance = new RetrofitConnection();
        }
        return instance;
    }

    public StudentRecordsApi getStudentRecordsApi() {
        return studentRecordsApi;
    }
}
