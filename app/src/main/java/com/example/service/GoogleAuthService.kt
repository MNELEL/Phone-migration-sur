package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Base64

data class GoogleAccountInfo(
    val email: String,
    val displayName: String?
)

sealed class GoogleSignInResult {
    data class Success(val account: GoogleAccountInfo) : GoogleSignInResult()
    data class Failure(val message: String) : GoogleSignInResult()
}

/**
 * Real Google Sign-In using Credential Manager (the current Android/Google
 * recommended API, replacing the deprecated GoogleSignInClient) and Firebase
 * Auth. This performs an actual OAuth round-trip and returns the real signed-in
 * account — it does not accept a typed email and mark itself "connected".
 *
 * IMPORTANT — setup required before this works:
 *  1. A real Firebase project with a real google-services.json placed at
 *     app/google-services.json (the demo config in CloudSyncService is a
 *     local-testing fallback only and cannot authenticate real users).
 *  2. Google Sign-In enabled as a provider in that Firebase project's
 *     Authentication settings.
 *  3. A Web Client ID (OAuth 2.0 client, type "Web application") from that
 *     Firebase project's associated Google Cloud project, passed in as
 *     [webClientId] below.
 * Without these, signIn() will fail with a clear error rather than silently
 * pretending to succeed.
 */
class GoogleAuthService(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun currentAccount(): GoogleAccountInfo? {
        val user = firebaseAuth.currentUser ?: return null
        return GoogleAccountInfo(email = user.email ?: return null, displayName = user.displayName)
    }

    suspend fun signIn(webClientId: String): GoogleSignInResult {
        if (webClientId.isBlank() || webClientId.contains("REPLACE_WITH")) {
            return GoogleSignInResult.Failure(
                "לא הוגדר Web Client ID אמיתי. יש להקים פרויקט Firebase, להפעיל Google Sign-In, ולהעתיק את ה-Web Client ID לקוד."
            )
        }

        return try {
            val nonce = generateNonce()
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return GoogleSignInResult.Failure("סוג האישור שהתקבל אינו נתמך")
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user

            if (user?.email == null) {
                GoogleSignInResult.Failure("ההתחברות הצליחה אך לא התקבלה כתובת אימייל מהחשבון")
            } else {
                GoogleSignInResult.Success(GoogleAccountInfo(email = user.email!!, displayName = user.displayName))
            }
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuthService", "Credential retrieval failed: ${e.message}")
            GoogleSignInResult.Failure("ההתחברות בוטלה או נכשלה: ${e.message ?: "שגיאה לא ידועה"}")
        } catch (e: GoogleIdTokenParsingException) {
            GoogleSignInResult.Failure("שגיאה בפענוח פרטי החשבון")
        } catch (e: Exception) {
            Log.e("GoogleAuthService", "Sign-in failed", e)
            GoogleSignInResult.Failure("ההתחברות נכשלה: ${e.message ?: "שגיאה לא ידועה"}")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
