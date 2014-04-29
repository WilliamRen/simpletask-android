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
package nl.mpcjanssen.simpletask;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import nl.mpcjanssen.simpletask.util.Util;

public class Preferences extends ThemedActivity {
    static TodoApplication m_app ;
    final static String TAG = Preferences.class.getSimpleName();
	public static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;
	public static final int RESULT_ARCHIVE = RESULT_FIRST_USER + 2;

	private void broadcastIntentAndClose(String intent, int result) {

		Intent broadcastIntent = new Intent(intent);
		sendBroadcast(broadcastIntent);

		// Close preferences screen
		setResult(result);
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.

        TodoTxtPrefFragment prefFragment = new TodoTxtPrefFragment();

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new TodoTxtPrefFragment())
				.commit();
	}

	public static class TodoTxtPrefFragment extends PreferenceFragment implements
    SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			PackageInfo packageInfo;
			final Preference versionPref = findPreference("app_version");
			try {
				packageInfo = getActivity().getPackageManager().getPackageInfo(
						getActivity().getPackageName(), 0);
				versionPref.setSummary("v" + packageInfo.versionName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
            m_app = (TodoApplication)getActivity().getApplication();
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // just update all
            if ("theme".equals(key)) {
                ThemedActivity act = (ThemedActivity)getActivity();
                act.recreate();
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
			if (preference.getKey() == null) {
				return false;
			}
			if (preference.getKey().equals("archive_now")) {
				Log.v("PREFERENCES",
						"Archiving completed items from preferences");
                m_app.showConfirmationDialog(this.getActivity(), R.string.delete_task_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Preferences) getActivity()).broadcastIntentAndClose(
                                getActivity().getPackageName()+Constants.BROADCAST_ACTION_ARCHIVE,
                                Preferences.RESULT_ARCHIVE);
                    }
                }, R.string.archive_task_title);

			} 
			return true;
		}
	}
}
