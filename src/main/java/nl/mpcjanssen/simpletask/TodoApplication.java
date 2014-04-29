/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 * @copyright 2013- Mark Janssen
 */
package nl.mpcjanssen.simpletask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

import nl.mpcjanssen.simpletask.task.LocalFileTaskRepository;
import nl.mpcjanssen.simpletask.task.TaskBag;
import nl.mpcjanssen.simpletask.util.FileDialog;
import nl.mpcjanssen.simpletask.util.Util;

public class TodoApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = TodoApplication.class.getSimpleName();
    private static Context m_appContext;
    private static SharedPreferences m_prefs;
    public boolean m_pulling = false;
    public boolean m_pushing = false;
    private TaskBag taskBag;
    private Handler handler = new Handler();
    private FileObserver m_observer;
    private File local_todo;

    public static Context getAppContext() {
        return m_appContext;
    }

    public static SharedPreferences getPrefs() {
        return m_prefs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TodoApplication.m_appContext = getApplicationContext();
        TodoApplication.m_prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        local_todo = new File(getTodoFileName());
        initStorage(local_todo);
    }

    private void initStorage(File todoFile) {
        local_todo = todoFile;
        TaskBag.Preferences taskBagPreferences = new TaskBag.Preferences(
                m_prefs);
        LocalFileTaskRepository localTaskRepository = new LocalFileTaskRepository(this, local_todo, taskBagPreferences);
            this.taskBag = new TaskBag(taskBagPreferences, localTaskRepository);
            Log.v(TAG, "Obs: " + localTaskRepository.getTodoTxtFile().getPath());
            m_observer = new FileObserver(localTaskRepository.getTodoTxtFile().getParent(),
                    FileObserver.ALL_EVENTS) {
                @Override
                public void onEvent(int event, String path) {
                    String todoFileName = new File(TodoApplication.this.getTodoFileName()).getName();
                    if (path!=null && path.equals(todoFileName) ) {
                        if( event == FileObserver.CLOSE_WRITE ||
                                event == FileObserver.MOVED_TO) {
                            Log.v(TAG, path + " modified reloading taskbag");
                            taskBag.reload();
                            updateUI();
                        }
                    }
                }
            };
        this.startWatching();
        taskBag.reload();
        updateUI();
    }

    public void openCloudlessFile(Activity act) {

        FileDialog fileDialog = new FileDialog(act,new File(getTodoFileName()).getAbsoluteFile().getParentFile(),showTxtOnly());
        fileDialog.setSelectDirectoryOption(false);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                setTodoFile(file);
                initStorage(file);
            }
        });
        fileDialog.createFileDialog();
    }

    public void startWatching() {
        if (m_observer!=null) {
            m_observer.startWatching();
        }
    }

    public void stopWatching() {
        if (m_observer!=null) {
            m_observer.stopWatching();
        }
    }

    @Override
    public void onTerminate() {
        m_prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onTerminate();
    }


    public TaskBag getTaskBag() {
        return taskBag;
    }

    public boolean showTxtOnly() {
        return m_prefs.getBoolean(getString(R.string.show_txt_only), false);
    }

    public boolean showCompleteCheckbox() {
        return m_prefs.getBoolean(getString(R.string.ui_complete_checkbox), true);
    }

    public boolean showHidden() {
        return m_prefs.getBoolean(getString(R.string.show_hidden), false);
    }

    public boolean showEmptyLists() {
        return m_prefs.getBoolean(getString(R.string.show_empty_lists), true);
    }

    public String getTodoFileName() {
        String default_path;
        default_path = Environment.getExternalStorageDirectory() +"/data/nl.mpcjanssen.simpletask/todo.txt";
        return m_prefs.getString(getString(R.string.todo_file_key), default_path);
    }

    public void setTodoFile(File todo) {
        try {
            m_prefs.edit().putString(getString(R.string.todo_file_key), todo.getCanonicalPath()).commit();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public boolean isAutoArchive() {
        return m_prefs.getBoolean(getString(R.string.auto_archive_pref_key), false);
    }

    public boolean isBackSaving() {
        return m_prefs.getBoolean(getString(R.string.back_key_saves_key), false);
    }

    public boolean hasShareTaskShowsEdit() {
        return m_prefs.getBoolean(getString(R.string.share_task_show_edit), false);
    }

    public boolean hasCapitalizeTasks() {
        return m_prefs.getBoolean(getString(R.string.capitalize_tasks), false);
    }

    public boolean hasColorDueDates() {
        return m_prefs.getBoolean(getString(R.string.color_due_date_key), true);
    }

    public boolean hasRecurOriginalDates() {
        return m_prefs.getBoolean(getString(R.string.recur_from_original_date), true);
    }

    public boolean hasLandscapeDrawers() {
        return (m_prefs.getBoolean(getString(R.string.ui_drawer_fixed_landscape), false) &&
                getResources().getBoolean(R.bool.is_landscape));
    }

    public void setEditTextHint(EditText editText, int resid ) {
        if (m_prefs.getBoolean(getString(R.string.ui_show_edittext_hints), true)) {
            editText.setHint(resid);
        }
    }

    public boolean isAddTagsCloneTags() {
        return m_prefs.getBoolean(getString(R.string.clone_tags_key),false);
    }

    public void setAddTagsCloneTags(boolean bool) {
        m_prefs.edit()
                .putBoolean(getString(R.string.clone_tags_key),bool)
                .commit();
    }

    public boolean isWordWrap() {
        return m_prefs.getBoolean(getString(R.string.word_wrap_key),true);
    }

    public void setWordWrap(boolean bool) {
        m_prefs.edit()
                .putBoolean(getString(R.string.word_wrap_key),bool)
                .commit();
    }

    public void setManualMode(boolean manual) {
    	Editor edit = m_prefs.edit();
        edit.putBoolean(getString(R.string.manual_sync_pref_key), manual);
        edit.commit();
    }

    public void showToast(int resid) {
        Util.showToastLong(this, resid);
    }

    public void showToast(String string) {
        Util.showToastLong(this, string);
    }

    /**
     * Update user interface
     *
     * Update the elements of the user interface. The listview with tasks will be updated
     * if it is visible (by broadcasting an intent). All widgets will be updated as well.
     * This method should be called whenever the TaskBag changes.
     */
    private void updateUI() {
        sendBroadcast(new Intent(Constants.BROADCAST_UPDATE_UI));
        updateWidgets();
    }

    public void updateWidgets() {
        AppWidgetManager mgr = AppWidgetManager.getInstance(getApplicationContext());
        for (int appWidgetId : mgr.getAppWidgetIds(new ComponentName(getApplicationContext(), MyAppWidgetProvider.class))) {
            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetlv);
            Log.v(TAG, "Updating widget: " + appWidgetId);
        }
    }

    private void redrawWidgets(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MyAppWidgetProvider.class));
        if (appWidgetIds.length > 0) {
            new MyAppWidgetProvider().onUpdate(this, appWidgetManager, appWidgetIds);
        }
    }

    public int getActiveTheme() {
        String theme =  getPrefs().getString(getString(R.string.theme_pref_key), "");
        if (theme.equals("android.R.style.Theme_Holo")) {
            return android.R.style.Theme_Holo;
        } else if (theme.equals("android.R.style.Theme_Holo_Light_DarkActionBar")) {
            return android.R.style.Theme_Holo_Light_DarkActionBar;
        } else if (theme.equals("android.R.style.Theme_Holo_Light")) {
            return android.R.style.Theme_Holo_Light;
        } else  {
            return android.R.style.Theme_Holo_Light_DarkActionBar;
        }
    }

    public void setActionBarStyle(Window window) {
        if (getPrefs().getBoolean(getString(R.string.split_actionbar_key), true)) {
            window.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        }
    }

    public boolean isDarkTheme() {
        switch (getActiveTheme()) {
            case android.R.style.Theme_Holo:
                return true;
            case android.R.style.Theme_Holo_Light_DarkActionBar:
            case android.R.style.Theme_Holo_Light:
                return false;
            default:
                return false;
        }
    }

    public boolean isDarkActionbar() {
        switch (getActiveTheme()) {
            case android.R.style.Theme_Holo:
            case android.R.style.Theme_Holo_Light_DarkActionBar:
                return true;
            case android.R.style.Theme_Holo_Light:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.widget_theme_pref_key)) ||
                s.equals(getString(R.string.widget_extended_pref_key)) ||
                s.equals(getString(R.string.widget_background_transparency)) ||
                s.equals(getString(R.string.widget_header_transparency))) {
            redrawWidgets();
        }
    }

    public void showConfirmationDialog(Context cxt, int msgid,
                                              DialogInterface.OnClickListener oklistener, int titleid) {
        boolean show = getPrefs().getBoolean(getString(R.string.ui_show_confirmation_dialogs), true);

        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(titleid);
        builder.setMessage(msgid);
        builder.setPositiveButton(android.R.string.ok, oklistener);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setCancelable(true);
        Dialog dialog = builder.create();
        if (show) {
           dialog.show();
        } else {
            oklistener.onClick(dialog , DialogInterface.BUTTON_POSITIVE);
        }
    }
}
