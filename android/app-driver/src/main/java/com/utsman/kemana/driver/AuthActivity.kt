/*
 * Copyright (c) 2019 Muhammad Utsman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.utsman.kemana.driver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseUser
import com.utsman.easygooglelogin.EasyGoogleLogin
import com.utsman.easygooglelogin.LoginResultListener
import com.utsman.featurerabbitmq.Rabbit
import com.utsman.kemana.base.RABBIT_URL
import com.utsman.kemana.base.RxAppCompatActivity
import com.utsman.kemana.base.intentTo
import com.utsman.kemana.base.logi
import com.utsman.kemana.remote.driver.Driver
import com.utsman.kemana.remote.driver.RemotePresenter
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : RxAppCompatActivity(), LoginResultListener {

    private lateinit var googleLogin: EasyGoogleLogin
    private lateinit var remotePresenter: RemotePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        googleLogin = EasyGoogleLogin(this)
        remotePresenter = RemotePresenter(composite)

        val token = getString(R.string.default_web_client_id)
        googleLogin.initGoogleLogin(token, this)

        btn_google_sign.setOnClickListener {
            googleLogin.signIn(this)
        }
    }

    override fun onStart() {
        super.onStart()
        googleLogin.initOnStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLogin.onActivityResult(this, requestCode, data)
    }

    override fun onLoginSuccess(user: FirebaseUser) {
        Rabbit.setInstance(user.email, RABBIT_URL) {
        }
        

        logi("login success")
        val driver = Driver(
            name = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl.toString()
        )

        btn_google_sign.isEnabled = false
        saveEmail(user.email!!)
        val bundle = bundleOf("driver" to driver)

        remotePresenter.checkRegisteredDriver(user.email) { status ->
            logi("status is --> $status")

            if (status != null) {
                if (status) {
                    intentTo(MainActivity::class.java, bundle)
                    finish()
                } else {
                    intentTo(FormCompleteActivity::class.java, bundle)
                    finish()
                }
            } else {
                btn_google_sign.isEnabled = true
                val bottomDialog = BottomSheetDialog(this)
                bottomDialog.setContentView(R.layout.bottom_dialog_error)
                bottomDialog.show()
            }
        }
    }

    override fun onLogoutSuccess(task: Task<Void>?) {
    }

    override fun onLoginFailed(exception: Exception?) {
    }

    override fun onLogoutError(exception: Exception?) {
    }
}