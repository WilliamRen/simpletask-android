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
package nl.mpcjanssen.todotxtholo;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import nl.mpcjanssen.todotxtholo.util.Util;

public class Preferences extends PreferenceActivity {
    private TodoApplication m_app;
    final static String TAG = TodoApplication.TAG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_app = (TodoApplication)getApplication();
        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) {
		if(preference.getKey()!=null && preference.getKey().equals("logout_dropbox")) {
			Log.v(TAG, "Logging out from Dropbox");
            m_app.logout();
			startActivity(new Intent(this, TodoTxtTouch.class));
            finish();
            return true;
		} else if(preference.getKey()!=null && preference.getKey().equals("archive_now")) {
            Log.v(TAG, "Archiving completed tasks to done.txt");
            Util.showToastLong(this,getString(R.string.archiving_toast));
            m_app.archiveTasks();
            finish();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
