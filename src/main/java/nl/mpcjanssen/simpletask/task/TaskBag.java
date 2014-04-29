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
package nl.mpcjanssen.simpletask.task;

import android.content.SharedPreferences;

import org.joda.time.DateTime;

import nl.mpcjanssen.simpletask.Simpletask;
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
    private ArrayList<Task> tasks = new ArrayList<Task>();

    //cached values
    TreeSet<Priority> prios = null;
    TreeSet<String> tags = null;
    TreeSet<String> lists = null;


    public TaskBag(Preferences taskBagPreferences,
                   LocalFileTaskRepository localTaskRepository) {
        this.preferences = taskBagPreferences;
        this.localRepository = localTaskRepository;
    }


    public void invalidateCache() {
        prios = null;
        tags = null;
        lists = null;
    }


    private void store(ArrayList<Task> tasks) {
        localRepository.store(tasks);
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
        invalidateCache();
        this.tasks = localRepository.load();
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
            invalidateCache();
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
            invalidateCache();
        }
    }

    public void delete(Task task) {
        tasks.remove(task);
        invalidateCache();
    }


    public TreeSet<Priority> getPriorities() {
        if (prios == null) {
            prios = new TreeSet<Priority>();
            for (Task item : tasks) {
                prios.add(item.getPriority());
            }
        }
        return prios;
    }

    public TreeSet<String> getContexts() {
        if (lists == null) {
            lists = new TreeSet<String>();
            for (Task item : tasks) {
                lists.addAll(item.getLists());
            }
        }
        return lists;
    }

    public TreeSet<String> getProjects() {
        if (tags == null) {
            tags = new TreeSet<String>();
            for (Task item : tasks) {
                tags.addAll(item.getTags());
            }
        }
        return tags;
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
