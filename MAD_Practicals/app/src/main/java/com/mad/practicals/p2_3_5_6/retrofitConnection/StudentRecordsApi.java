package com.mad.practicals.p2_3_5_6.retrofitConnection;

import com.mad.practicals.p2_3_5_6.StudentRecord;

import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StudentRecordsApi {
    String BASE_URL = "http://studentrecordsapi.us-east-2.elasticbeanstalk.com/";
    @GET("fetch_all.php")
    Call<LinkedList<StudentRecord>> fetchAll();
}
