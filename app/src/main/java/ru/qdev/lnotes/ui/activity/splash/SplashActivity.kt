package ru.qdev.lnotes.ui.activity.splash

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.lifecycleScope
import com.example.reply.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.ui.activity.backup.QDVBackupActivity
import ru.qdev.lnotes.ui.activity.notes.QDVNotesHomeActivity
import ru.qdev.lnotes.ui.screen.license.LicenseScreen
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import ru.qdev.lnotes.ui.view.button.SecondaryButtonColors
import src.R
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private var launchTimerJob: Job? = null

    @Inject
    lateinit var preferenceHelper: NotesPreferenceHelper

    private val isLicenseAcceptedState = mutableStateOf(false)
    private val timerState = mutableStateOf<Int?>(null)

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                ActivityContent(timerState.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        fillLicenseState()

        if (preferenceHelper.isLicenseAccepted) {
            startLaunchTimer()
        }
    }

    private fun fillLicenseState() {
        isLicenseAcceptedState.value = preferenceHelper.isLicenseAccepted
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ActivityContent(timer: Int? = null){
        if (isLicenseAcceptedState.value || LocalInspectionMode.current) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(R.drawable.logo_notes_app_144dp),
                    contentDescription = stringResource(R.string.app_name)
                )

                Column (Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = contentHPaddingDp)
                    .navigationBarsPadding()){
                    SButton(
                        modifier = Modifier
                            .zIndex(10f)
                            .padding(bottom = dp14).fillMaxWidth(),
                        onClick = {
                            startBackupActivity()
                        },
                        colors = SecondaryButtonColors(),
                        content = MainButtonContent(
                            text = stringResource(R.string.action_backup_restore),
                            textAlign = TextAlign.Center
                        )
                    )

                    SButton(
                        modifier = Modifier
                            .zIndex(10f)
                            .padding(bottom = dp14).fillMaxWidth(),
                        onClick = {
                            startNotesActivity()
                        },
                        content = MainButtonContent(
                            text = stringResource(R.string.notes_listview_title)
                                    + if (timer != null) " (${timer})" else "",
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
        else {
            LicenseScreen(
                onAccept = {
                    fillLicenseState()
                    if (preferenceHelper.isLicenseAccepted) {
                        startLaunchTimer()
                    }
                }
            )

            BackHandler {
                finishAffinity()
            }
        }
    }

    private fun startBackupActivity() {
        stopLaunchTimer()
        val sendDataActivityStartIntent = Intent(
            this,
            QDVBackupActivity::class.java
        )
        sendDataActivityStartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(sendDataActivityStartIntent)
    }

    private fun startNotesActivity() {
        val sendDataActivityStartIntent = Intent(
            this,
            QDVNotesHomeActivity::class.java
        )
        sendDataActivityStartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(sendDataActivityStartIntent)
    }

    private fun startLaunchTimer() {
        launchTimerJob?.cancel()
        launchTimerJob = lifecycleScope.launch {
            for (i in 3 downTo 1) {
                timerState.value = i
                delay(1.seconds)
            }
            startNotesActivity()
            finish()
        }
    }

    private fun stopLaunchTimer() {
        launchTimerJob?.cancel()
    }

    @ExperimentalMaterial3Api
    @Composable
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        name = "DefaultPreviewDark"
    )
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_NO,
        name = "DefaultPreviewLight"
    )
    private fun ActivityContentPreview() {
        AppTheme {
            ActivityContent(3)
        }
    }
}

