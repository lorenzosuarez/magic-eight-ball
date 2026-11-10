package com.example.magiceightball.core.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

/**
 * Tells the API which Android app is calling: its package and the SHA-1 of the certificate it
 * is signed with. With these, the API key can be restricted in Google Cloud to this app, so a
 * key lifted out of it is of no use anywhere else.
 */
class AndroidAppIdentityInterceptor(context: Context) : Interceptor {

    private val packageName: String = context.packageName
    private val certificateSha1: String? by lazy { signingCertificateSha1(context) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header(HEADER_PACKAGE, packageName)
        certificateSha1?.let { request.header(HEADER_CERTIFICATE, it) }
        return chain.proceed(request.build())
    }

    private companion object {
        const val HEADER_PACKAGE = "X-Android-Package"
        const val HEADER_CERTIFICATE = "X-Android-Cert"

        /** Upper-case hex without separators, as the API expects it; null if it cannot be read. */
        @Suppress("DEPRECATION")
        fun signingCertificateSha1(context: Context): String? = try {
            val packageManager = context.packageManager
            val certificate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners?.firstOrNull()
            } else {
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                    .signatures?.firstOrNull()
            }
            certificate?.toByteArray()?.let { bytes ->
                MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02X".format(it) }
            }
        } catch (_: Exception) {
            null
        }
    }
}
