package com.sedsoftware.yaptalker.data.repository

import com.sedsoftware.yaptalker.data.exception.RequestErrorException
import com.sedsoftware.yaptalker.data.mappers.LoginSessionInfoMapper
import com.sedsoftware.yaptalker.data.mappers.ServerResponseMapper
import com.sedsoftware.yaptalker.data.network.site.YapLoader
import com.sedsoftware.yaptalker.domain.entity.BaseEntity
import com.sedsoftware.yaptalker.domain.entity.base.ServerResponse
import com.sedsoftware.yaptalker.domain.repository.LoginSessionRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class YapLoginSessionRepository @Inject constructor(
  private val dataLoader: YapLoader,
  private val dataMapper: LoginSessionInfoMapper,
  private val responseMapper: ServerResponseMapper
) : LoginSessionRepository {

  companion object {
    private const val LOGIN_COOKIE_DATE = 1
    private const val LOGIN_REFERRER = "http://www.yaplakal.com/forum/"
    private const val LOGIN_SUBMIT = "Вход"

    private const val SIGN_OUT_SUCCESS_MARKER = "Вы вышли"
    private const val SIGN_IN_SUCCESS_MARKER = "Спасибо"
  }

  override fun getLoginSessionInfo(): Single<BaseEntity> =
    dataLoader
      .loadAuthorizedUserInfo()
      .map(dataMapper)

  override fun requestSignIn(userLogin: String, userPassword: String, anonymously: Boolean): Completable =
    dataLoader
      .signIn(
        cookieDate = LOGIN_COOKIE_DATE,
        privacy = anonymously,
        password = userPassword,
        userName = userLogin,
        referer = LOGIN_REFERRER,
        submit = LOGIN_SUBMIT,
        userKey = "$userLogin${System.currentTimeMillis()}".toMd5()
      )
      .map(responseMapper)
      .flatMapCompletable { response ->
        response as ServerResponse
        if (response.text.contains(SIGN_IN_SUCCESS_MARKER)) {
          Completable.complete()
        } else {
          Completable.error(RequestErrorException("Unable to complete sign in request."))
        }
      }

  override fun requestSignOut(userKey: String): Completable =
    dataLoader
      .signOut(userKey)
      .map(responseMapper)
      .flatMapCompletable { response ->
        response as ServerResponse
        if (response.text.contains(SIGN_OUT_SUCCESS_MARKER)) {
          Completable.complete()
        } else {
          Completable.error(RequestErrorException("Unable to complete sign out request."))
        }
      }


  @Suppress("MagicNumber")
  private fun String.toMd5(): String {

    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.update(this.toByteArray())
    val messageDigest = digest.digest()
    val hexString = StringBuffer()

    for (i in 0 until messageDigest.size) {
      var hex = Integer.toHexString(0xFF and messageDigest[i].toInt())
      while (hex.length < 2)
        hex = "0$hex"
      hexString.append(hex)
    }
    return hexString.toString()
  }
}
