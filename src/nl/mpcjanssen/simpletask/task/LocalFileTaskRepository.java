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
 */
package nl.mpcjanssen.simpletask.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.os.FileObserver;
import nl.mpcjanssen.simpletask.util.TaskIo;
import nl.mpcjanssen.simpletask.util.Util;
import android.os.Environment;
import android.util.Log;


/**
 * A task repository for interacting with the local file system
 *
 * @author Tim Barlotta
 */
public class LocalFileTaskRepository {
    private static final String TAG = LocalFileTaskRepository.class
            .getSimpleName();
    private FileObserver mFileObserver;
    File TODO_TXT_FILE;

    public File get_todo_file() {
        return TODO_TXT_FILE;
    }

    private final TaskBag.Preferences preferences;

    public void setFileObserver (FileObserver observer) {
        this.mFileObserver = observer;
    }

    public LocalFileTaskRepository(TaskBag.Preferences m_prefs) {
        this.preferences = m_prefs;
        this.mFileObserver = null;
        String todoPath = m_prefs.todoPath();
        if (todoPath == null) {
        	return;
        }
        TODO_TXT_FILE = new File( m_prefs.todoPath());
        try {
            if (!TODO_TXT_FILE.exists()) {
                Util.createParentDirectory(TODO_TXT_FILE);
                TODO_TXT_FILE.createNewFile();
            }
        } catch (IOException e) {
            Log.e (TAG, "Error initializing LocalFile " + e);
        }

    }

    public ArrayList<Task> load() {
        if (!TODO_TXT_FILE.exists()) {
            Log.e(TAG, TODO_TXT_FILE.getAbsolutePath() + " does not exist!");
        } else {
            try {
                return TaskIo.loadTasksFromFile(TODO_TXT_FILE);
            } catch (IOException e) {
                Log.e(TAG, "Error loading from local file" + e);
            }
        }
        return null;
    }

    public void store(ArrayList<Task> tasks) {
        if (mFileObserver!=null) {
            mFileObserver.stopWatching();
            Log.v(TAG, "Stop watching " + TODO_TXT_FILE + " when storing taskbag");
        }
        TaskIo.writeToFile(tasks, TODO_TXT_FILE,
                preferences.isUseWindowsLineBreaksEnabled());
        if (mFileObserver!=null) {
            Log.v(TAG, "Start watching " + TODO_TXT_FILE + " storing taskbag done");
            mFileObserver.startWatching();
        }
    }

    public void archive(ArrayList<Task> tasks, String pathValue) {
        boolean windowsLineBreaks = preferences.isUseWindowsLineBreaksEnabled();

        ArrayList<Task> completedTasks = new ArrayList<Task>(tasks.size());
        ArrayList<Task> incompleteTasks = new ArrayList<Task>(tasks.size());

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                incompleteTasks.add(task);
            }
        }

        // append completed tasks to done.txt
        File archiveFile = new File(pathValue);
        TaskIo.writeToFile(completedTasks, archiveFile, true,
                windowsLineBreaks);

        // write incomplete tasks back to todo.txt
         TaskIo.writeToFile(incompleteTasks, TODO_TXT_FILE, false,
               windowsLineBreaks);
    }
}