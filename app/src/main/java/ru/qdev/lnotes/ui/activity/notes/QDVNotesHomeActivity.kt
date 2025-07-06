package ru.qdev.lnotes.ui.activity.notes

//import butterknife.ButterKnife
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.example.reply.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import ru.qdev.lnotes.ui.navigation.QDVNavigator
import ru.qdev.lnotes.ui.navigation.route.note.NoteListScreenRoute
import ru.qdev.lnotes.ui.view.QDVViewFabric
import ru.qdev.lnotes.utils.QDVStatisticState.addTimeForShowUserRatingQuest
import ru.qdev.lnotes.utils.QDVStatisticState.userRatingQuestShownNoNeed
import ru.qdev.lnotes.utils.QDVTempFileSendUtils
import ru.qdev.lnotes.utils.getVersionName
import src.R
import javax.inject.Inject

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

@AndroidEntryPoint
class QDVNotesHomeActivity : ComponentActivity() {

    private val viewModel: QDVNotesHomeViewModel by viewModels<QDVNotesHomeViewModel>()

    private var tempFileSendUtils: QDVTempFileSendUtils? = null

    @Inject
    lateinit var navigator: QDVNavigator

//    @JvmField
//    @InjectPresenter
//    var notesHomePresenter: QDVNotesHomePresenter? = null
//
//    var navigationDrawerFragment: QDVNavigationDrawerFragment? = null
//
//    @JvmField
//    @BindView(R.id.rootLayout)
//    var rootLayout: ViewGroup? = null

    @ExperimentalMaterial3Api
    @UiThread
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.QDVActionBarTheme)
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                navigator.NavigationView(
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    startDestination = NoteListScreenRoute()
                )
            }
        }

//        setContentView(R.layout.home_activity)

//        ButterKnife.bind(this)

//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(true)
//            actionBar.setDisplayShowHomeEnabled(true)
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeButtonEnabled(true)
//        }

//        navigationDrawerFragment =
//            supportFragmentManager.findFragmentById(R.id.navigation_drawer) as QDVNavigationDrawerFragment?
//        navigationDrawerFragment!!.isActive = false
//
//        // Set up the drawer.
//        navigationDrawerFragment!!.setUp(
//            R.id.navigation_drawer,
//            findViewById<View>(R.id.drawer_layout) as DrawerLayout
//        )
    }

    override fun onResume() {
        super.onResume()

        if (tempFileSendUtils == null) {
            tempFileSendUtils = QDVTempFileSendUtils(this)
        }

        tempFileSendUtils!!.deleteUnusedFiles()

        //Test
//        showUserRatingQuest()
    }

    @UiThread
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val needReloadDb = intent.getBooleanExtra(
            QDVNotesHomeViewModel.NEED_RELOAD_DB_FLAG,
            false
        )
        if (needReloadDb) {
            viewModel.reloadDataDb()
        }
    }

    @UiThread
    override fun onStart() {
        super.onStart()
//        val buyPlusVersion = QDVVersionDifference.getLabelBuyPlusVersion(rootLayout)
//        buyPlusVersion?.setOnClickListener(View.OnClickListener { v: View? ->
//            try {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW, Uri.parse(
//                            "https://play.google.com/store/apps/details?id=ru.q_dev.LNoteP"
//                        )
//                    )
//                )
//            } catch (ignored: Exception) {
//                AlertDialog.Builder(this)
//                    .setMessage(R.string.app_not_found)
//                    .setCancelable(true)
//                    .setNegativeButton(R.string.cancel, null)
//                    .show()
//            }
//        })

        val needReloadDb = intent.getBooleanExtra(
            QDVNotesHomeViewModel.NEED_RELOAD_DB_FLAG,
            false
        )
        if (needReloadDb) {
            viewModel.reloadDataDb()
            return
        }
        viewModel.oldDbUpdateIfNeeded()
    }
//    @UiThread
//    override fun setNavigationDrawerFolderEnabled(enabled: Boolean) {
//        navigationDrawerFragment!!.isActive = enabled
//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            if (enabled) {
//                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp)
//            } else {
//                actionBar.setHomeAsUpIndicator(null)
//            }
//        }
//    }

//    @UiThread
//    override fun initNotesList(filterByFolderState: QDVFilterByFolderState?) {
//        val fragmentManager = supportFragmentManager
//        val backStackCount = fragmentManager.backStackEntryCount
//        if (backStackCount > 0) {
//            for (i in 0 until backStackCount) {
//                fragmentManager.popBackStack()
//            }
//        } else {
//            fragmentManager.beginTransaction()
//                .replace(
//                    R.id.container, QDVNotesListFragment.newInstance(filterByFolderState),
//                    QDVNotesListFragment.FRAGMENT_TAG
//                )
//                .commit()
//        }
//        val actionBar = supportActionBar
//        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_24dp)
//        navigationDrawerFragment!!.isActive = true
//    }

//    @UiThread
//    override fun initEditNote(note: QDVDbNote) {
//        navigationDrawerFragment!!.isActive = false
//        val fragmentManager = supportFragmentManager
//        var fragment = fragmentManager.findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG)
//        if (fragment == null) {
//            val noteEditorState = QDVNoteEditorState()
//            noteEditorState.setState(
//                QDVNoteEditorState.EditorMode.EDITING,
//                note.folderId, note.id
//            )
//            fragment = QDVNoteEditorFragment()
//            supportFragmentManager.beginTransaction().addToBackStack(null)
//                .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit()
//        }
//        val actionBar = supportActionBar
//        actionBar?.setHomeAsUpIndicator(null)
//    }

//    @UiThread
//    override fun initAddNote(folderIdForAdding: Long?) {
//        navigationDrawerFragment!!.isActive = false
//        val fragmentManager = supportFragmentManager
//        var fragment = fragmentManager.findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG)
//        if (fragment == null) {
//            val noteEditorState = QDVNoteEditorState()
//            noteEditorState.setState(
//                QDVNoteEditorState.EditorMode.ADDING,
//                folderIdForAdding, null
//            )
//            fragment = QDVNoteEditorFragment()
//            supportFragmentManager.beginTransaction().addToBackStack(null)
//                .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit()
//        }
//        val actionBar = supportActionBar
//        actionBar?.setHomeAsUpIndicator(null)
//    }

    @UiThread
    private fun showUserRatingQuest() {
        val ratingView = QDVViewFabric(this, layoutInflater).createRatingView()

        AlertDialog.Builder(this@QDVNotesHomeActivity)
            .setTitle(R.string.like_app_quest_title)
            .setMessage(getString(R.string.like_app_quest_text))
            .setView(ratingView)
            .setCancelable(false)
            .setPositiveButton(
                R.string.open_google_play,
                DialogInterface.OnClickListener { dialog, which ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.google_play_link))
                        )
                    )
                    userRatingQuestShownNoNeed = true
                })
            .setNeutralButton(
                R.string.later,
                DialogInterface.OnClickListener { dialog, which -> addTimeForShowUserRatingQuest() })
            .setNegativeButton(
                R.string.no_but_thanks,
                DialogInterface.OnClickListener { dialog, which ->
                    userRatingQuestShownNoNeed =
                        true
                })
            .show()
    }

    fun showAboutAppDialog() {
        val logStr = "showAboutAppDialog"

        var lnotesNameAndVersion = getString(R.string.app_name)+" "

        try {
            lnotesNameAndVersion += packageManager.getPackageInfo(
                packageName,
                0
            ).versionName
        } catch (e: Throwable) {
            Log.e(TAG, "$logStr, $e", e)
        }

        val alertDialogView = layoutInflater.inflate(
            R.layout.about_dialog, null)

        val aboutTextView: TextView = alertDialogView.findViewById(R.id.aboutText)
        aboutTextView.setText(R.string.about_message)

        val ratingView = QDVViewFabric(this, layoutInflater).createRatingView()
        ((alertDialogView as ViewGroup).findViewById<RelativeLayout>(R.id.layoutForView))
            .addView(ratingView)

        val rateQuestText: TextView = alertDialogView.findViewById(R.id.rateQuestText)
        rateQuestText.text = getString(R.string.like_app_quest_text)

        AlertDialog.Builder(this)
            .setTitle(lnotesNameAndVersion)
            .setCancelable(true)
            .setView(alertDialogView)
//            .setPositiveButton(R.string.open_google_play,
//                object : DialogInterface.OnClickListener {
//
//                    @Override
//                    override fun onClick(p0: DialogInterface?, p1: Int) {
//                        startActivity(
//                            Intent(Intent.ACTION_VIEW,
//                            getString(R.string.google_play_link).toUri())
//                        )
//                        QDVStatisticState.userRatingQuestShownNoNeed = true
//                    }
//                })
//            .setNeutralButton(R.string.action_thanks, null).show();
            .setPositiveButton(R.string.action_thanks, null).show();
    }

    fun contactToDeveloper() {
        val logStr = "contactToDeveloper"
        Log.i(TAG, logStr)

        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/html")
        intent.putExtra(Intent.EXTRA_EMAIL,
            listOf(getString(R.string.developer_email)).toTypedArray())
        intent.putExtra(Intent.EXTRA_SUBJECT,
            getString(R.string.email_to_developer_subject))

        val appInfo = getString(R.string.app_name) + " v" + getVersionName(this) +
                "\nAndroid " + Build.VERSION.RELEASE
        val mailText = String.format(getString(R.string.email_to_developer_text), appInfo)
        intent.putExtra(Intent.EXTRA_TEXT, mailText)
        startActivity(intent)
    }

    fun moveToBackground(){
        val logStr = "moveToBackground"
        Log.i(TAG, logStr)

        moveTaskToBack(true)
    }

    fun sendText(text: String) {
        val logStr = "sendText"
        Log.i(TAG, logStr)

        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        catch (e: Throwable) {
            Log.e(TAG, "sendText $e", e)
        }
    }

    companion object {
        const val TAG = "QDVNotesHomeActivity"
    }
}
