package ru.qdev.lnotes.ui.activity.notes

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
//import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import ru.qdev.lnotes.mvp.QDVStatisticState.addTimeForShowUserRatingQuest
import ru.qdev.lnotes.mvp.QDVStatisticState.userRatingQuestShownNoNeed
import ru.qdev.lnotes.ui.fragment.QDVNoteEditorFragment
import ru.qdev.lnotes.ui.fragment.QDVNotesListFragment
import ru.qdev.lnotes.ui.view.QDVViewFabric
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.utils.QDVTempFileSendUtils
import src.R
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

@AndroidEntryPoint
class QDVNotesHomeActivity : ComponentActivity() {

    private val viewModel: QDVNotesHomeViewModel by viewModels<QDVNotesHomeViewModel>()

    private var tempFileSendUtils: QDVTempFileSendUtils? = null

//    @JvmField
//    @InjectPresenter
//    var notesHomePresenter: QDVNotesHomePresenter? = null
//
//    var navigationDrawerFragment: QDVNavigationDrawerFragment? = null
//
//    @JvmField
//    @BindView(R.id.rootLayout)
//    var rootLayout: ViewGroup? = null

    @UiThread
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.QDVActionBarTheme)
        super.onCreate(savedInstanceState)

        setContent {

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

        //Test TODO потестировать AlertDialog
        showUserRatingQuest()
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
//    override fun onBackPressed() {
//        val fragment = supportFragmentManager
//            .findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG)
//        if (fragment is QDVNoteEditorFragment) {
//            fragment.goBackWithConfirm()
//            return
//        }
//        if (navigationDrawerFragment!!.isDrawerOpen) {
//            navigationDrawerFragment!!.isDrawerOpen = false
//            return
//        }
//        super.onBackPressed()
//    }

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

    companion object {

    }
}
