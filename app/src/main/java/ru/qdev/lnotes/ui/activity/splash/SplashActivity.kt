package ru.qdev.lnotes.ui.activity.splash

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.lifecycleScope
import com.example.reply.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.qdev.lnotes.ui.activity.backup.QDVBackupActivity
import ru.qdev.lnotes.ui.activity.notes.QDVNotesHomeActivity
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import src.R

class SplashActivity : AppCompatActivity() {
    private var launchTimerJob: Job? = null

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                ActivityContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        startLaunchTimer()
    }

    @Composable
    private fun ActivityContent(){
        Box (modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(R.drawable.logo_notes_app_144dp),
                contentDescription = stringResource(R.string.app_name)
            )

            SButton (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
                    .padding(bottom = dp14)
                    .navigationBarsPadding(),
                onClick = {
                    startBackupActivity()
                },
                content = MainButtonContent(stringResource(R.string.action_backup_restore))
            )
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
            delay(3000)
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
            ActivityContent()
        }
    }
}

