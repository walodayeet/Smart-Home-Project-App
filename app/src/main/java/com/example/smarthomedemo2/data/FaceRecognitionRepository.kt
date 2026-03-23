package com.example.smarthomedemo2.data

import com.example.smarthomedemo2.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class RecognizedFaceDto(
    val name: String,
    val confidence: Double,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val left: Int,
)

@JsonClass(generateAdapter = true)
data class RecognitionResponseDto(
    val ownerRecognized: Boolean,
    val message: String,
    val primaryMatch: RecognizedFaceDto? = null,
    val faces: List<RecognizedFaceDto> = emptyList(),
)

interface FaceRecognitionApi {
    @Multipart
    @POST("recognize")
    suspend fun recognize(
        @Part image: MultipartBody.Part,
    ): Response<RecognitionResponseDto>
}

class FaceRecognitionRepository(
    private val api: FaceRecognitionApi = createApi(),
) {
    suspend fun recognizeFace(imageBytes: ByteArray): Result<RecognitionResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
            val imagePart = MultipartBody.Part.createFormData(
                name = "image",
                filename = "camera-frame.jpg",
                body = requestBody,
            )

            val response = api.recognize(imagePart)
            if (!response.isSuccessful) {
                error("Recognition failed with HTTP ${response.code()}")
            }

            response.body() ?: error("Recognition service returned an empty response")
        }
    }

    companion object {
        private fun createApi(): FaceRecognitionApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.FACE_RECOGNITION_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FaceRecognitionApi::class.java)
        }
    }
}
