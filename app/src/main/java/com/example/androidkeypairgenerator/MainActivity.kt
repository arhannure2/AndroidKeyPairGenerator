package com.example.androidkeypairgenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.util.*
import javax.security.auth.x500.X500Principal



class MainActivity : AppCompatActivity() {


    private lateinit var keyguardManager: KeyguardManager
    private lateinit var keyPair: KeyPair
    private lateinit var signatureResult: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        //Check if lock screen has been set up. Just displaying a Toast here but it shouldn't allow the user to go forward.
        //below call requires API level 23
        if (!keyguardManager.isDeviceSecure) {
            Toast.makeText(this,getString(R.string.screen_lock) , Toast.LENGTH_LONG).show()
        }

        //Check if the keys already exists to avoid creating them again
        if (!checkKeyExists()) {
            generateKey()
        }

        signButton.setOnClickListener {
            signData()
        }

        verifyButton.setOnClickListener {
            verifyData()
        }
    }

    private fun generateKey() {
        //We create the start and expiry date for the key
        val startDate = GregorianCalendar()
        val endDate = GregorianCalendar()
        endDate.add(Calendar.YEAR, 10) // you can take year as one or as per your requirment

        //We are creating a RSA key pair and store it in the Android Keystore
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ProjectConstant.ANDROID_KEYSTORE)

        //We are creating the key pair with sign and verify purposes
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(ProjectConstant.KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).run {
            setCertificateSerialNumber(BigInteger.valueOf(222))       //Serial number used for the self-signed certificate of the generated key pair, default is 1
            setCertificateSubject(X500Principal("CN=${ProjectConstant.KEY_ALIAS}"))     //Subject used for the self-signed certificate of the generated key pair, default is CN=fake
            setDigests(KeyProperties.DIGEST_SHA256)                         //Set of digests algorithms with which the key can be used
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1) //Set of padding schemes with which the key can be used when signing/verifying
            setCertificateNotBefore(startDate.time)                         //Start of the validity period for the self-signed certificate of the generated, default Jan 1 1970
            setCertificateNotAfter(endDate.time)                            //End of the validity period for the self-signed certificate of the generated key, default Jan 1 2048
            setUserAuthenticationRequired(true)                             //Sets whether this key is authorized to be used only if the user has been authenticated, default false
            setUserAuthenticationValidityDurationSeconds(45)                //Duration(seconds) for which this key is authorized to be used after the user is successfully authenticated
            build()
        }

        //Initialization of key generator with the parameters we have specified above
        keyPairGenerator.initialize(parameterSpec)

        //Generates the key pair
        keyPair = keyPairGenerator.genKeyPair()
    }

    private fun checkKeyExists(): Boolean {
        //We get the Keystore instance
        val keyStore: KeyStore = KeyStore.getInstance(ProjectConstant.ANDROID_KEYSTORE).apply {
            load(null)
        }

        //We get the private and public key from the keystore if they exists
        val privateKey: PrivateKey? = keyStore.getKey(ProjectConstant.KEY_ALIAS, null) as PrivateKey?
        val publicKey: PublicKey? = keyStore.getCertificate(ProjectConstant.KEY_ALIAS)?.publicKey

        return privateKey != null && publicKey != null
    }

    private fun signData() {
        try {
            //We get the Keystore instance
            val keyStore: KeyStore = KeyStore.getInstance(ProjectConstant.ANDROID_KEYSTORE).apply {
                load(null)
            }

            //Retrieves the private key from the keystore
            val privateKey: PrivateKey = keyStore.getKey(ProjectConstant.KEY_ALIAS, null) as PrivateKey

            //We sign the data with the private key. We use RSA algorithm along SHA-256 digest algorithm
            val signature: ByteArray? = Signature.getInstance("SHA256withRSA").run {
                initSign(privateKey)
                update("TestString".toByteArray())
                sign()
            }

            if (signature != null) {
                //We encode and store in a variable the value of the signature
                signatureResult = Base64.encodeToString(signature, Base64.DEFAULT)
                resultTextView.text = getString(R.string.signed_successful)
            }

        } catch (e: UserNotAuthenticatedException) {
            //Exception thrown when the user has not been authenticated.
            showAuthenticationScreen()
        } catch (e: KeyPermanentlyInvalidatedException) {
            //Exception thrown when the key has been invalidated for example when lock screen has been disabled.
            Toast.makeText(this, getString(R.string.keys_invalidated) + e.message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun verifyData() {
        //We get the Keystore instance
        val keyStore: KeyStore = KeyStore.getInstance(ProjectConstant.ANDROID_KEYSTORE).apply {
            load(null)
        }

        //We get the certificate from the keystore
        val certificate: Certificate? = keyStore.getCertificate(ProjectConstant.KEY_ALIAS)

        if (certificate != null) {
            //We decode the signature value
            val signature: ByteArray = Base64.decode(signatureResult, Base64.DEFAULT)

            //We check if the signature is valid. We use RSA algorithm along SHA-256 digest algorithm
            val isValid: Boolean = Signature.getInstance("SHA256withRSA").run {
                initVerify(certificate)
                update("TestString".toByteArray())
                verify(signature)
            }

            if (isValid) {
                resultTextView.text =getString(R.string.verify_success)
            } else {
                resultTextView.text = getString(R.string.verify_fail)
            }
        }
    }

    private fun showAuthenticationScreen() {
        //This will open a screen to enter the user credentials (fingerprint, pin, pattern). We can display a custom title and description
        val intent: Intent? = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.keystore_sign_and_verify),getString(R.string.keystore_sign_and_verify_msg)
            )
        if (intent != null) {
            startActivityForResult(intent, ProjectConstant.REQUEST_CODE_FOR_CREDENTIALS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ProjectConstant.REQUEST_CODE_FOR_CREDENTIALS) {
            if (resultCode == Activity.RESULT_OK) {
                signData()
            } else {
                Toast.makeText(this, getString(R.string.auth_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

