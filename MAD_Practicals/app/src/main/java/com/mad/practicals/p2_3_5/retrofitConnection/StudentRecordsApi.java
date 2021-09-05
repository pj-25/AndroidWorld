package com.mad.practicals.p2_3_5.retrofitConnection;

import com.mad.practicals.p2_3_5.StudentRecord;

import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StudentRecordsApi {
    String BASE_URL = "http://studentrecords.us-east-2.elasticbeanstalk.com/";
    @GET("fetch_all.php")
    Call<LinkedList<StudentRecord>> fetchAll();
}
