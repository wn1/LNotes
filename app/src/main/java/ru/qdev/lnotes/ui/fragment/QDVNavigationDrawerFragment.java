package ru.qdev.lnotes.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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

import com.j256.ormlite.dao.CloseableIterator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import ru.qdev.lnotes.*;
import ru.qdev.lnotes.db.QDVDbIteratorListViewAdapterExt;
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem;
import ru.qdev.lnotes.mvp.QDVNavigationDrawerPresenter;
import ru.qdev.lnotes.mvp.QDVNavigationDrawerView;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNavigationDrawerFragment extends MvpAppCompatFragment
        implements QDVNavigationDrawerView {
    @InjectPresenter
    QDVNavigationDrawerPresenter navigationDrawerPresenter;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;

    FolderListAdapter folderListAdapter;

    @UiThread
    public void setSelectedFolderOrMenu(QDVDbFolderOrMenuItem selectedFolderOrMenu) {
        if (folderListAdapter != null){
            folderListAdapter.selectedFolderOrMenu = selectedFolderOrMenu;
        }
    }

    @AnyThread
    public boolean isActive() {
        return isActive;
    }

    @UiThread
    public void setActive(boolean active) {
        isActive = active;
        if (isActive) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigationDrawerPresenter.doDrawerShowIfUserLearn();
                }
            }, 1000);
        }
        if (drawerLayout != null && !isActive) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }

    private boolean isActive = false;

    @Override
    @UiThread
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    static private class FolderListAdapter extends QDVDbIteratorListViewAdapterExt<QDVDbFolderOrMenuItem> {
        public QDVDbFolderOrMenuItem getSelectedFolderOrMenu() {
            return selectedFolderOrMenu;
        }

        public void setSelectedFolderOrMenu(QDVDbFolderOrMenuItem selectedFolderOrMenu) {
            this.selectedFolderOrMenu = selectedFolderOrMenu;
            notifyDataSetChanged();
        }

        private QDVDbFolderOrMenuItem selectedFolderOrMenu;

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            QDVDbFolderOrMenuItem folderOrMenu = getItem(i);
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(
                        android.R.layout.simple_list_item_activated_1,
                        viewGroup, false);
            }
            if (view == null) {
                return null;
            }
            if (folderOrMenu == null) {
                view.setVisibility(View.INVISIBLE);
                return view;
            }
            ((TextView) view.findViewById(android.R.id.text1))
                    .setText(folderOrMenu.getLabel());

            Boolean itemChecked = false;
            if (selectedFolderOrMenu!=null) {
                if (folderOrMenu.menuItem ==
                        QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
                    if (folderOrMenu.getId() == selectedFolderOrMenu.getId()) {
                        itemChecked = true;
                    }
                } else if (folderOrMenu.menuItem == selectedFolderOrMenu.menuItem) {
                    itemChecked = true;
                }
            }

            view.setBackgroundColor(ContextCompat.getColor(viewGroup.getContext(), itemChecked ?
                    R.color.listViewFolderSelectedColor : R.color.transparentColor));

            return view;
        }
    }

    @Override
    @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        drawerListView = (ListView) inflater.inflate(
                R.layout.navigation_drawer, container, false);

        folderListAdapter = new FolderListAdapter();

        drawerListView.setAdapter(folderListAdapter);

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QDVDbFolderOrMenuItem folder = folderListAdapter.getItem(position);
                navigationDrawerPresenter.onClickFolderOrMenu(folder);
                drawerListView.setItemChecked(position, false);
            }
        });

        drawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView,
                                           View view, int position, long l) {
                final QDVDbFolderOrMenuItem folderOrMenu = folderListAdapter.getItem(position);
                if (folderOrMenu==null ||
                        folderOrMenu.menuItem!=QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
                    return false;
                }
                final String folderLabel = folderOrMenu.getLabel() != null
                        ? folderOrMenu.getLabel() : "";
                new AlertDialog.Builder(getActivity()).setTitle(folderLabel).setCancelable(true)
                        .setItems(new String[]{getString(R.string.menu_delete),
                                getString(R.string.menu_rename)},
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        onClickDeleteFolder(folderOrMenu);
                                        break;

                                    case 1:
                                        onClickRenameFolder(folderOrMenu);
                                        break;
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
                return true;
            }
        });

        return drawerListView;
    }

    @UiThread
    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    @UiThread
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        Toolbar drawerToolbar = new Toolbar(getContext());
        drawerToolbar.setNavigationIcon(R.drawable.ic_drawer);

        if (drawerToggle!=null) {
            this.drawerLayout.removeDrawerListener(drawerToggle);
        }

        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                QDVNavigationDrawerFragment.this.drawerLayout,
                drawerToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                navigationDrawerPresenter.userLearned();

                getActivity().invalidateOptionsMenu();
            }
        };

        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (drawerToggle!=null) {
            this.drawerLayout.removeDrawerListener(drawerToggle);
        }
    }

    @Override
    @UiThread
    public void onClickAddFolder() {
        final EditText editText = new EditText(getContext());
        new AlertDialog.Builder(getActivity()).setTitle(R.string.add_category).setCancelable(true)
                .setView(editText).setPositiveButton(R.string.action_ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                String folderName = editText.getText().toString();
                if (folderName.length()>0) {
                    navigationDrawerPresenter.doAddFolder(folderName);
                }
                else
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.folder_name_need_no_empty)
                            .setPositiveButton(R.string.action_ok, null)
                            .show();
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        }).show();
        editText.requestFocus();
        editText.requestFocusFromTouch();
        InputMethodManager inputMananger = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMananger != null) {
            inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @UiThread
    public void onClickDeleteFolder(final QDVDbFolderOrMenuItem folder) {
        if (folder.menuItem!=QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return;
        }
        final String folderLabel = folder.getLabel() != null ? folder.getLabel() : "";
        new AlertDialog.Builder(getActivity())
                .setTitle(String.format(getString(R.string.delete_folder_confirm),
                        folderLabel)).setCancelable(true)
                .setMessage(R.string.delete_folder_confirm_message)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        navigationDrawerPresenter.doRemoveFolder(folder);
                    }
                })
                .setNegativeButton(R.string.action_no, null).show();
    }

    @UiThread
    public void onClickRenameFolder(final QDVDbFolderOrMenuItem folder) {
        if (folder.menuItem!=QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return;
        }
        final String folderLabel = folder.getLabel() != null ? folder.getLabel() : "";
        final EditText editText = new EditText(getContext());
        editText.setText(folderLabel);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.rename_folder_title)
                .setCancelable(true)
                .setView(editText).setPositiveButton(R.string.action_ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                String folderName = editText.getText().toString();
                if (folderName.length()>0) {
                    folder.setLabel(folderName);
                    navigationDrawerPresenter.doUpdateFolder(folder);
                }
                else
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.folder_name_need_no_empty)
                            .setPositiveButton(R.string.action_ok, null)
                            .show();
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        }).show();
        editText.requestFocus();
        editText.requestFocusFromTouch();
        InputMethodManager inputMananger = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMananger != null) {
            inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }


    @Override
    @UiThread
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (drawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.navigation_drawer, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isActive) {
            return super.onOptionsItemSelected(item);
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @UiThread
    public void loadFolderList(@NotNull CloseableIterator<QDVDbFolderOrMenuItem> dbIterator,
                               @NotNull ArrayList<QDVDbFolderOrMenuItem> itemsAddingToTop,
                               @Nullable QDVDbFolderOrMenuItem selectedFolderOrMenu) {
        folderListAdapter.setSelectedFolderOrMenu(selectedFolderOrMenu);
        folderListAdapter.loadData(itemsAddingToTop, dbIterator);
    }

    @Override
    @UiThread
    public void setDrawerOpen(boolean drawerOpen) {
        if (!isActive) {
            drawerLayout.closeDrawer(fragmentContainerView);
            return;
        }
        if (drawerLayout != null) {
            if (drawerOpen) {
                drawerLayout.openDrawer(fragmentContainerView);
            }
            else
            {
                drawerLayout.closeDrawer(fragmentContainerView);
            }
        }
    }

    @Override
    @UiThread
    public void switchDrawerOpenOrClose() {
        if (!isActive) {
            drawerLayout.closeDrawer(fragmentContainerView);
            return;
        }
        if (drawerLayout.isDrawerOpen(fragmentContainerView)) {
            drawerLayout.closeDrawer(fragmentContainerView);
        } else {
            drawerLayout.openDrawer(fragmentContainerView);
        }
    }
}
