package com.example.compsci.default08_tab;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Amanda M on 11/23/2015.
 */
public class EditPreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
