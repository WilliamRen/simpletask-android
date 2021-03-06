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

import android.content.SharedPreferences;

import org.joda.time.DateTime;

import nl.mpcjanssen.simpletask.Simpletask;
import nl.mpcjanssen.simpletask.remote.PullTodoResult;
import nl.mpcjanssen.simpletask.remote.RemoteClientManager;
import nl.mpcjanssen.simpletask.util.TaskIo;
import nl.mpcjanssen.simpletask.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Implementation of the TaskBag
 *
 * @author Tim Barlotta, Mark Janssen
 *         <p/>
 *         The taskbag is the backing store for the task list used by the application
 *         It is loaded from and stored to the local copy of the todo.txt file and
 *         it is global to the application so all activities operate on the same copy
 */
public class TaskBag {
    final static String TAG = Simpletask.class.getSimpleName();
    private Preferences preferences;
    private final LocalFileTaskRepository localRepository;
    private final RemoteClientManager remoteClientManager;
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private Date lastReload = null;
    private Date lastSync = null;

    public TaskBag(Preferences taskBagPreferences,
                   LocalFileTaskRepository localTaskRepository,
                   RemoteClientManager remoteClientManager) {
        this.preferences = taskBagPreferences;
        this.localRepository = localTaskRepository;
        this.remoteClientManager = remoteClientManager;
    }


    private void store(ArrayList<Task> tasks) {
        localRepository.store(tasks);

        lastReload = null;
    }

    public void store() {
        store(this.tasks);
    }

    public void archive(List<Task> tasksToArchive) {
        try {
            tasks=localRepository.archive(tasks, tasksToArchive);
        } catch (Exception e) {
            throw new TaskPersistException(
                    "An error occurred while archiving", e);
        }
    }

    public void reload() {
        if (lastReload == null || localRepository.todoFileModifiedSince(lastReload)) {
            localRepository.init();
            this.tasks = localRepository.load();
            lastReload = new Date();

        }
    }

    public int size() {
        return tasks.size();
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    public Task addAsTask(String input) {
        try {
            Task task = new Task(tasks.size(), input,
                    (preferences.isPrependDateEnabled() ? new DateTime() : null));
	    if (preferences.addAtEnd()) {
		tasks.add(task);
	    } else {
		tasks.add(0,task);
	    }
            store();
            return task;
        } catch (Exception e) {
            throw new TaskPersistException("An error occurred while adding {"
                    + input + "}", e);
        }
    }

    public void updateTask(Task task, String input) {
        int index = tasks.indexOf(task);
        if (index!=-1) {
            tasks.get(index).init(input, null);
            store();
        }
    }

    public void delete(Task task) {
        tasks.remove(task);
    }

    /* REMOTE APIS */
    public void pushToRemote(boolean overwrite) {
        pushToRemote(false, overwrite);
    }

    public void pushToRemote(boolean overridePreference, boolean overwrite) {
        if (this.preferences.isOnline() || overridePreference) {
            File doneFile = null;
            if (localRepository.doneFileModifiedSince(lastSync)) {
                doneFile = localRepository.DONE_TXT_FILE;
            }
            remoteClientManager.getRemoteClient().pushTodo(
                    localRepository.TODO_TXT_FILE,
                    doneFile,
                    overwrite);
            lastSync = new Date();
        }
    }

    public void pullFromRemote(boolean overridePreference) {
        try {
            if (this.preferences.isOnline() || overridePreference) {
                PullTodoResult result = remoteClientManager.getRemoteClient()
                        .pullTodo();
                File todoFile = result.getTodoFile();
                if (todoFile != null && todoFile.exists()) {
                    ArrayList<Task> remoteTasks = TaskIo
                            .loadTasksFromFile(todoFile, preferences);
                    store(remoteTasks);
                    reload();
                }

                File doneFile = result.getDoneFile();
                if (doneFile != null && doneFile.exists()) {
                    localRepository.loadDoneTasks(doneFile);
                } else if (doneFile == null) {
                    // Dropbox has no done file so remove this one
                    localRepository.removeDoneFile();
                }
                lastSync = new Date();
            }
        } catch (IOException e) {
            throw new TaskPersistException(
                    "Error loading tasks from remote file", e);
        }
    }

    public ArrayList<Priority> getPriorities() {
        // TODO cache this after reloads?
        Set<Priority> res = new HashSet<Priority>();
        for (Task item : tasks) {
            res.add(item.getPriority());
        }
        ArrayList<Priority> ret = new ArrayList<Priority>(res);
        Collections.sort(ret);
        return ret;
    }

    public ArrayList<String> getContexts(boolean includeNone) {
        // TODO cache this after reloads?
        Set<String> res = new HashSet<String>();
        for (Task item : tasks) {
            res.addAll(item.getLists());
        }
        ArrayList<String> ret = new ArrayList<String>(res);
        Collections.sort(ret);
        if (includeNone) {
            ret.add(0, "-");
        }
        return ret;
    }

    public ArrayList<String> getProjects(boolean includeNone) {
        // TODO cache this after reloads?
        Set<String> res = new HashSet<String>();
        for (Task item : tasks) {
            res.addAll(item.getTags());
        }
        ArrayList<String> ret = new ArrayList<String>(res);
        Collections.sort(ret);
        if (includeNone) {
            ret.add(0, "-");
        }
        return ret;
    }

    public ArrayList<String> getDecoratedContexts(boolean includeNone) {
        return Util.prefixItems("@", getContexts(includeNone));
    }

    public ArrayList<String> getDecoratedProjects(boolean includeNone) {
        return Util.prefixItems("+", getProjects(includeNone));
    }

    public static class Preferences {
        private final SharedPreferences sharedPreferences;

        public Preferences(SharedPreferences sharedPreferences) {
            this.sharedPreferences = sharedPreferences;
        }

        public void setUseWindowsLineBreaksEnabled(boolean enabled) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("linebreakspref", enabled);
            edit.commit();

        }
        public boolean isUseWindowsLineBreaksEnabled() {
            return sharedPreferences.getBoolean("linebreakspref", false);
        }

        public boolean isPrependDateEnabled() {
            return sharedPreferences.getBoolean("todotxtprependdate", true);
        }

        public boolean isOnline() {
            return !sharedPreferences.getBoolean("workofflinepref", false);
        }

	public boolean addAtEnd() {
	    return sharedPreferences.getBoolean("addtaskatendpref", true);
	}
    }

}
