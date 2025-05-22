package com.example.diaviseo.network.body

import com.example.diaviseo.network.body.dto.req.BodyRegisterRequest
import com.example.diaviseo.network.body.dto.req.BodyUpdateRequest
import com.example.diaviseo.network.body.dto.res.BodyInfoResponse
import com.example.diaviseo.network.body.dto.res.MonthlyAverageBodyInfoResponse
import com.example.diaviseo.network.body.dto.res.OcrBodyResultResponse
import com.example.diaviseo.network.body.dto.res.WeeklyAverageBodyInfoResponse
import com.example.diaviseo.network.common.dto.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BodyApiService {
    @POST("bodies")
    suspend fun registerBodyData(
        @Body request: BodyRegisterRequest
    ): ApiResponse<BodyInfoResponse>

    @Multipart
    @POST("bodies/ocr")
    suspend fun uploadBodyOcrImage(
        @Part image: MultipartBody.Part
    ): ApiResponse<OcrBodyResultResponse>

    @GET("bodies/weekly")
    suspend fun fetchDailyBodyStatistic(
        @Query("endDate") endDate: String
    ): ApiResponse<List<OcrBodyResultResponse>>

    @GET("bodies/weekly-avg")
    suspend fun fetchWeeklyBodyStatistic(
        @Query("date") date: String
    ): ApiResponse<List<WeeklyAverageBodyInfoResponse>>

    @GET("bodies/monthly-avg")
    suspend fun fetchMonthlyBodyStatistic(
        @Query("date") date: String
    ): ApiResponse<List<MonthlyAverageBodyInfoResponse>>

    //체성분 날짜 조회
    @GET("bodies/date")
    suspend fun loadBodyData(
        @Query("date") date: String
    ): ApiResponse<BodyInfoResponse>

    // 최신 체성분 조회
    @GET("bodies")
    suspend fun fetchLatestBodyData(
        @Query("date") date: String? = null
    ): ApiResponse<List<BodyInfoResponse>>   // 리스트이긴 한데 요소 하나만 있음

    // 체성분 수정
    @PATCH("/api/bodies/{bodyId}")
    suspend fun updateBodyInfo(
        @Body request: BodyUpdateRequest,
        @Path("bodyId") bodyId: Int
    ): ApiResponse<BodyInfoResponse>

}