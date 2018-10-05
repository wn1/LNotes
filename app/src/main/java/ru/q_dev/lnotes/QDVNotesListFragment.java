package ru.q_dev.lnotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.j256.ormlite.dao.CloseableIterator;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */

public class QDVNotesListFragment extends MvpAppCompatFragment implements QDVNotesListView {
    private Unbinder unbinder;
    public static final String ARG_SECTION_ID = "section_id";
    public static final String ARG_FILTER_BY_FOLDER = "filterByFolder";

    @BindView(R.id.notesList)
    ListView notesList;
    @BindView(R.id.findTextViewLabel)
    TextView findTextViewLabel;
    @BindView(R.id.layoutFindOptions)
    View layoutFindOptions;

    @InjectPresenter
    QDVNotesListPresenter notesListPresenter;

    QDVNotesListState state;
    QDVDbIteratorListViewAdapter<QDVDbNote> notesListAdapter;

    View rootView;

    DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

    public static QDVNotesListFragment newInstance(long sectionNumber) {
        QDVNotesListFragment fragment = new QDVNotesListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SECTION_ID, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static QDVNotesListFragment newInstance(QDVFilterByFolderState filterByFolderState)
    {
        QDVNotesListFragment fragment = new QDVNotesListFragment();
        Bundle args = new Bundle();
        if (filterByFolderState == null) {
            filterByFolderState = new QDVFilterByFolderState();
        }
        args.putSerializable(ARG_FILTER_BY_FOLDER, filterByFolderState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("state", state);
    }

    public QDVNotesListFragment() {
    }

    void onClickMoveToFolder(final QDVDbNote note) {
        final ArrayList<QDVDbFolderOrMenuItem> itemsAddingToTop = new ArrayList<>();
        itemsAddingToTop.add(new QDVDbFolderOrMenuItem(
                getString(R.string.category_unknown), QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN));

        final QDVDbIteratorListViewAdapterExt<QDVDbFolderOrMenuItem> adapter =
                new QDVDbIteratorListViewAdapterExt<QDVDbFolderOrMenuItem>(itemsAddingToTop) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                QDVDbFolderOrMenuItem folder = getItem(i);
                if (view == null) {
                    view = getLayoutInflater().inflate(
                            android.R.layout.simple_list_item_activated_1,
                            viewGroup, false);
                }
                if (view == null) {
                    return null;
                }
                if (folder == null) {
                    view.setVisibility(View.INVISIBLE);
                    return view;
                }
                ((TextView) view.findViewById(android.R.id.text1)).setText(folder.getLabel());
                return view;
            }
        };
        adapter.loadDbIterator(notesListPresenter.dbIteratotorFoldersQuery());

        new AlertDialog.Builder(getActivity()).setTitle(
                note.getContent()!=null ? note.getContent() : "").setCancelable(true)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        try {
                            QDVDbFolderOrMenuItem folderOrMenu = adapter.getItem(position);
                            if (folderOrMenu == null) {
                                return;
                            }
                            if (folderOrMenu.menuItem ==
                                    QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN) {
                                note.setFolderId(QDVDbFolder.Special.UNKNOWN_FOLDER.getId());
                                notesListPresenter.doUpdateNote(note);
                                return;
                            }
                            if (folderOrMenu.menuItem!=
                                    QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
                                return;
                            }
                            note.setFolderId(folderOrMenu.getId());
                            notesListPresenter.doUpdateNote(note);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    void onClickDelete(final QDVDbNote note) {
        new AlertDialog.Builder(getActivity())
                .setTitle(note.getContent()!=null ? note.getContent() : "")
                .setMessage(R.string.delete_confirm).setCancelable(true)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notesListPresenter.doDeleteNote(note);
                    }
                })
                .setNegativeButton(R.string.action_no, null).show();
    }


    void onClickSetStatusOfExecution(final QDVDbNote note) {
        new AlertDialog.Builder(getActivity())
                .setTitle(note.getContent()!=null ? note.getContent() : "")
                .setCancelable(true)
                .setItems(new String[]{getString(R.string.set_done),
                        getString(R.string.set_no_needed),
                        getString(R.string.set_in_work)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                QDVDbNote.StatusOfExecution status = QDVDbNote.StatusOfExecution.CREATED;
                switch (i) {
                    case 0:
                        status = QDVDbNote.StatusOfExecution.COMPLETED;
                        break;

                    case 1:
                        status = QDVDbNote.StatusOfExecution.NOT_NEED;
                        break;

                    case 2:
                        status = QDVDbNote.StatusOfExecution.CREATED;
                        break;
                }
                note.setStatusOfExecution(status);
                notesListPresenter.doSetStatusOfExecution(note, status);
            }
        })
        .setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notes, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState!=null) {
            state = (QDVNotesListState) savedInstanceState.getSerializable("state");
        }

        if (state == null) {
            state = new QDVNotesListState();
        }

        notesListAdapter = new QDVDbIteratorListViewAdapter <QDVDbNote> ()  {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = inflater.inflate(R.layout.cell_note, viewGroup, false);
                }

                view.setVisibility(View.VISIBLE);

                try {
                    QDVDbNote note = getItem(i);
                    if (note == null) {
                        view.setVisibility(View.INVISIBLE);
                        return view;
                    }
                    boolean isReadyOrDone =
                            note.getStatusOfExecution()!=QDVDbNote.StatusOfExecution.CREATED;
                    TextView textView = view.findViewById(R.id.text_view_date_left);
                    textView.setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    textView.setText(note.getUpdateTime()!=null ?
                            dateFormat.format(note.getUpdateTime()) : "");

                    textView = view.findViewById(R.id.text_view_note);
                    textView.setAlpha(isReadyOrDone ? 0.4f : 1f);
                    textView.setText(note.getContent());

                    textView = view.findViewById(R.id.text_view_date_right);
                    textView.setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    textView.setVisibility(isReadyOrDone ? View.VISIBLE : View.INVISIBLE);
                    textView.setText(note.getCompleteTime()!=null ?
                            dateFormat.format(note.getCompleteTime()) : "");

                    view.findViewById(R.id.imageView_ready).setVisibility(
                            note.getStatusOfExecution()==QDVDbNote.StatusOfExecution.COMPLETED ?
                                    View.VISIBLE : View.GONE);

                } catch (Exception e) {
                    view.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                }
                return view;
            }
        };

        notesList.setAdapter(notesListAdapter);

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = notesList.getAdapter().getItem(i);
                if (!(object instanceof QDVDbNote)) {
                    return;
                }
                QDVDbNote note = (QDVDbNote) object;
                EventBus.getDefault().post(new QDVNotesHomePresenter.DoEditNoteEvent(note));
            }
        });

        Object obj = getArguments().getSerializable(ARG_FILTER_BY_FOLDER);
        if (obj instanceof QDVFilterByFolderState) {
            QDVFilterByFolderState filter = (QDVFilterByFolderState) obj;
            state.setFilterByFolderState(filter);
            Long folderIdForAdding = null;

            switch (filter.getFilterType()) {
                case FOLDER:
                    folderIdForAdding = filter.getFolder().getId();
                    break;
                case FOLDER_ID:
                    folderIdForAdding = filter.getFolderId();
                    break;
            }
            state.setFolderIdForNotesAdding(folderIdForAdding);

        } else {
            long folderId = getArguments().getLong(ARG_SECTION_ID);
            if (folderId > 0) {
                state.getFilterByFolderState().setFilterType(
                        QDVFilterByFolderState.FilterType.FOLDER_ID);
                state.getFilterByFolderState().setFolderId(folderId);
            } else if (folderId == QDVNotesHomeActivity.action_categories_all_id) {
                state.getFilterByFolderState().setFilterType(
                        QDVFilterByFolderState.FilterType.ALL_FOLDER);
            } else {
                state.getFilterByFolderState().setFilterType(
                        QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED);
            }

            state.setFolderIdForNotesAdding(folderId > 0 ? folderId : null);
        }

        notesListPresenter.initWithState(state);

        notesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = notesList.getAdapter().getItem(i);
                if (!(object instanceof QDVDbNote)) {
                    return false;
                }
                final QDVDbNote note = (QDVDbNote) object;
                    new AlertDialog.Builder(getActivity())
                            .setTitle(note.getContent()!=null ? note.getContent() : "")
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, null)
                            .setItems(
                                    new String[]{getString(R.string.menu_move),
                                            getString(R.string.menu_delete),
                                            getString(R.string.menu_set_done)},
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            switch (i) {
                                                case 0:
                                                    onClickMoveToFolder(note);
                                                    break;
                                                case 1:
                                                    onClickDelete(note);
                                                    break;
                                                case 2:
                                                    onClickSetStatusOfExecution(note);
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }).create().show();
                return true;
            }
        });

        return rootView;
    }

    @OnClick(R.id.buttonFindCancel) void OnClick() {
        notesListPresenter.onUndoSearch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_remove_ads).setVisible(QDVVersionDifference.isFreeVersion());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_note) {
            EventBus.getDefault().post(
                    new QDVNotesHomePresenter.DoAddNoteEvent(state.getFolderIdForNotesAdding()));
            return true;
        }

        if (item.getItemId() == R.id.action_find_notes){
            final EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getActivity()).setTitle(R.string.action_find_notes_title).setCancelable(true)
                    .setView(editText).setPositiveButton(R.string.action_ok,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(
                                editText.getWindowToken(), 0);
                    }
                    notesListPresenter.onSearchText(editText.getText().toString());

                }
            }).setNegativeButton(R.string.cancel,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(
                                editText.getWindowToken(), 0);
                    }
                }
            }).show();
            editText.requestFocus();
            editText.requestFocusFromTouch();
            InputMethodManager inputManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            return true;
        }

        if (item.getItemId() == R.id.action_about){
            String lnotesNameAndVersion = getString(R.string.app_name)+" ";
            try {
                lnotesNameAndVersion = lnotesNameAndVersion +
                        getContext().getPackageManager().getPackageInfo(
                                getContext().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            View alertDialogView = getActivity().getLayoutInflater().inflate(
                    R.layout.about_dialog, null);
            TextView textView = (TextView) alertDialogView.findViewById(R.id.aboutText);
            textView.setText(R.string.about_message);
            new AlertDialog.Builder(getActivity()).setTitle(lnotesNameAndVersion)
                    .setCancelable(true)
                    .setView(alertDialogView)
                    .setPositiveButton(R.string.action_ok, null).show();

            return true;
        }

        if (item.getItemId() == R.id.action_backup_notes){
            Intent sendDataActivityStartIntent = new Intent(getContext(), QDVBackupActivity.class);
            sendDataActivityStartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(sendDataActivityStartIntent);
            //getActivity().finish();
            return true;
        }

        if (item.getItemId() == R.id.action_remove_ads) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=ru.q_dev.LNoteP")));
            }
            catch (Exception ignored) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.app_not_found)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadNotesList(@NotNull final CloseableIterator<QDVDbNote> dbIterator) {
        notesListAdapter.loadDbIterator(dbIterator);
    }

    @Override
    public void setSearchState(@NotNull QDVSearchState searchState) {
        layoutFindOptions.setVisibility(
                state.getSearchState().isSearchActive() ? View.VISIBLE : View.GONE);

        final Integer count = notesListAdapter.getCount();
        String search_label = String.format(
                getString(R.string.finding_label), state.getSearchState().getSearchText()) + "\n"
                + String.format(getString(R.string.finding_label_count), String.valueOf(count));
        findTextViewLabel.setText(search_label);
    }


    @Override
    public void setFolderName(@NotNull String folderName) {
        if (getActivity()!=null && getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar!=null) {
                actionBar.setTitle(folderName);
            }
        }
    }
}
