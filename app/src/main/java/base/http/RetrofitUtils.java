package base.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import interceptor.AddCookiesInterceptor;
import interceptor.LoggingInterceptor;
import interceptor.ReceivedCookiesInterceptor;
import interceptor.RetrofitLogInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit工具类
 */
public class RetrofitUtils {
    private static volatile RetrofitUtils mInstance;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;

    private RetrofitUtils() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //混存
//        long maxCacheSize = 100 * 1024 * 1024;//100m
//        File httpCacheDirectory = new File(Environment.get, "okhttpCache");
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new ReceivedCookiesInterceptor())
                .addInterceptor(new AddCookiesInterceptor())
                .addInterceptor(new RetrofitLogInterceptor())
                .addNetworkInterceptor(httpLoggingInterceptor)
//                .cache()//缓存
                .build();

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();


        retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

    }

    public static RetrofitUtils getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitUtils.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitUtils();
                }
            }
        }

        return mInstance;
    }

    /**
     * 动态代理模式，创建请求接口类
     *
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T createService(Class<T> tClass) {

        return retrofit.create(tClass);
    }


}
