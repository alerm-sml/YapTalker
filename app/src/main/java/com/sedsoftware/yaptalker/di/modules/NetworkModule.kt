package com.sedsoftware.yaptalker.di.modules

import com.sedsoftware.yaptalker.commons.converters.HashSearchConverterFactory
import com.sedsoftware.yaptalker.data.network.site.YapIncubatorLoader
import com.sedsoftware.yaptalker.data.network.site.YapLoader
import com.sedsoftware.yaptalker.data.network.site.YapSearchIdLoader
import com.sedsoftware.yaptalker.data.network.thumbnails.CoubLoader
import com.sedsoftware.yaptalker.data.network.thumbnails.RutubeLoader
import com.sedsoftware.yaptalker.data.network.thumbnails.VkLoader
import com.sedsoftware.yaptalker.data.network.thumbnails.YapFileLoader
import com.sedsoftware.yaptalker.data.network.thumbnails.YapVideoLoader
import com.sedsoftware.yaptalker.di.modules.network.HttpClientsModule
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [(HttpClientsModule::class)])
class NetworkModule {

  companion object {
    private const val SITE_ENDPOINT = "https://www.yaplakal.com/"
    private const val SITE_INCUBATOR_ENDPOINT = "https://inkubator.yaplakal.com/"
    private const val COUB_VIDEO_ENDPOINT = "http://coub.com/"
    private const val RUTUBE_VIDEO_ENDPOINT = "http://rutube.ru/"
    private const val YAP_FILES_ENDPOINT = "http://www.yapfiles.ru/"
    private const val YAP_API_ENDPOINT = "http://api.yapfiles.ru/"
    private const val VK_VIDEO_ENDPOINT = "https://api.vk.com/"
    private const val YAP_FILE_HASH_MARKER = "md5="
    private const val YAP_SEARCH_ID_HASH_MARKER = "searchid="
  }

  @Singleton
  @Provides
  fun provideYapLoader(@Named("siteClient") okHttpClient: OkHttpClient): YapLoader =
      Retrofit
          .Builder()
          .baseUrl(SITE_ENDPOINT)
          .client(okHttpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(JspoonConverterFactory.create())
          .addConverterFactory(ScalarsConverterFactory.create())
          .build()
          .create(YapLoader::class.java)

  @Singleton
  @Provides
  fun provideYapIncubatorLoader(@Named("siteClient") okHttpClient: OkHttpClient): YapIncubatorLoader =
      Retrofit
          .Builder()
          .baseUrl(SITE_INCUBATOR_ENDPOINT)
          .client(okHttpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(JspoonConverterFactory.create())
          .addConverterFactory(ScalarsConverterFactory.create())
          .build()
          .create(YapIncubatorLoader::class.java)

  @Singleton
  @Provides
  fun provideYapSearchIdLoader(@Named("siteClient") okHttpClient: OkHttpClient): YapSearchIdLoader =
      Retrofit
          .Builder()
          .baseUrl(SITE_ENDPOINT)
          .client(okHttpClient)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(HashSearchConverterFactory.create(YAP_SEARCH_ID_HASH_MARKER))
          .build()
          .create(YapSearchIdLoader::class.java)


  @Singleton
  @Provides
  fun provideCoubLoader(): CoubLoader =
      Retrofit
          .Builder()
          .baseUrl(COUB_VIDEO_ENDPOINT)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create(CoubLoader::class.java)

  @Singleton
  @Provides
  fun provideRutubeLoader(): RutubeLoader =
      Retrofit
          .Builder()
          .baseUrl(RUTUBE_VIDEO_ENDPOINT)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create(RutubeLoader::class.java)

  @Singleton
  @Provides
  fun provideYapFileLoader(): YapFileLoader =
      Retrofit.Builder()
          .baseUrl(YAP_FILES_ENDPOINT)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(HashSearchConverterFactory.create(YAP_FILE_HASH_MARKER))
          .build()
          .create(YapFileLoader::class.java)

  @Singleton
  @Provides
  fun provideYapVideoLoader(): YapVideoLoader =
      Retrofit.Builder()
          .baseUrl(YAP_API_ENDPOINT)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create(YapVideoLoader::class.java)

  @Singleton
  @Provides
  fun provideVkLoader(): VkLoader =
      Retrofit.Builder()
          .baseUrl(VK_VIDEO_ENDPOINT)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create(VkLoader::class.java)
}
