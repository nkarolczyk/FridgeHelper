package com.example.fridgehelper.di

import android.content.Context
import androidx.room.Room
import com.example.fridgehelper.data.api.OpenFoodApi
import com.example.fridgehelper.data.api.SpoonacularApi
import com.example.fridgehelper.data.db.CachedRecipeDao
import com.example.fridgehelper.data.db.FridgeDatabase
import com.example.fridgehelper.data.db.ProductDao
import com.example.fridgehelper.data.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ROOM

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FridgeDatabase =
        Room.databaseBuilder(
            context,
            FridgeDatabase::class.java,
            "fridge_database"
        )
            .addMigrations(
                FridgeDatabase.MIGRATION_1_2,
                FridgeDatabase.MIGRATION_2_3
            )
            .build()

    @Provides
    fun provideProductDao(db: FridgeDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCachedRecipeDao(db: FridgeDatabase): CachedRecipeDao = db.cachedRecipeDao()

    // Open Food Facts
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "FridgeHelper/1.0 (Android; nataliakarolczyk@example.com)"
                    )
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodApi(retrofit: Retrofit): OpenFoodApi =
        retrofit.create(OpenFoodApi::class.java)


    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences =
        UserPreferences(context)

    @Provides
    @Singleton
    @Named("spoonacular")
    fun provideSpoonacularRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSpoonacularApi(@Named("spoonacular") retrofit: Retrofit): SpoonacularApi =
        retrofit.create(SpoonacularApi::class.java)
}