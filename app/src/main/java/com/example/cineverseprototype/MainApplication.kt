package com.example.cineverseprototype

import android.app.Application
import android.content.Context
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class MainApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            //each plugin you chose above can be configured in a block like this:
            dialog {
                //required
                text = "Sorry, the application is crashed. Please send a report to developer to fix it. If possible please include a comment on how the error happened."
                //optional, enables the dialog title
                title = "App Crash"
                //defaults to android.R.string.ok
                positiveButtonText = "OK"
                //defaults to android.R.string.cancel
                negativeButtonText = "CANCEL"
                //optional, enables the comment input
                commentPrompt = "Add a comment here: "
                //optional, enables the email input
                //emailPrompt = "Enter your mail: "
                //defaults to android.R.drawable.ic_dialog_alert
                resIcon = R.drawable.baseline_error_outline_white_24dp
                //optional, defaults to @android:style/Theme.Dialog
                resTheme = R.style.Theme_AppCompat_Dialog
                //allows other customization
                //reportDialogClass = MyCustomDialog::class.java
            }
            mailSender {
                //required
                mailTo = "eugenceFYP@outlook.com"
            }
        }
    }

}