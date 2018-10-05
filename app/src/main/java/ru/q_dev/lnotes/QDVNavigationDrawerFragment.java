package ru.q_dev.lnotes;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNavigationDrawerFragment extends MvpAppCompatFragment
        implements QDVNavigationDrawerView {
    @InjectPresenter
    QDVNavigationDrawerPresenter navigationDrawerPresenter;

    //TODO
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;

    QDVDbIteratorListViewAdapterExt<QDVDbFolderOrMenuItem> folderListAdapter;
    private QDVDbFolderOrMenuItem selectedFolderOrMenu;

    public QDVDbFolderOrMenuItem getSelectedFolderOrMenu() {
        return selectedFolderOrMenu;
    }

    public void setSelectedFolderOrMenu(QDVDbFolderOrMenuItem selectedFolderOrMenu) {
        this.selectedFolderOrMenu = selectedFolderOrMenu;
        if (folderListAdapter != null){
            folderListAdapter.notifyDataSetChanged();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }

    private boolean isActive = false;

    public QDVNavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        drawerListView = (ListView) inflater.inflate(
                R.layout.drawer_notes, container, false);

        folderListAdapter =
                new QDVDbIteratorListViewAdapterExt<QDVDbFolderOrMenuItem>() {
                    @Override
                    public View getView(int i, View view, ViewGroup viewGroup) {
                        QDVDbFolderOrMenuItem folderOrMenu = getItem(i);
                        if (view == null) {
                            view = getLayoutInflater().inflate(
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

                        if (selectedFolderOrMenu!=null) {
                            if (folderOrMenu.menuItem ==
                                    QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
                                if (folderOrMenu.getId() == selectedFolderOrMenu.getId()) {
                                    drawerListView.setItemChecked(i, true);
                                }
                            } else if (folderOrMenu.menuItem == selectedFolderOrMenu.menuItem) {
                                drawerListView.setItemChecked(i, true);
                            }
                        }

                        return view;
                    }
                };

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

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                QDVNavigationDrawerFragment.this.drawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                navigationDrawerPresenter.userLearned();

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (drawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void loadFolderList(@NotNull CloseableIterator<QDVDbFolderOrMenuItem> dbIterator,
                               @NotNull ArrayList<QDVDbFolderOrMenuItem> itemsAddingToTop,
                               @Nullable QDVDbFolderOrMenuItem selectedFolderOrMenu) {
        this.selectedFolderOrMenu = selectedFolderOrMenu;
        folderListAdapter.loadData(itemsAddingToTop,
                navigationDrawerPresenter.dbIteratotorFoldersQuery());
    }

    @Override
    public void setDrawerOpen(boolean drawerOpen) {
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
}
