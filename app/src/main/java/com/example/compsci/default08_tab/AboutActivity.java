package com.example.compsci.default08_tab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Amanda on 12/2/2015.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//removes dialog title bar
		setContentView(R.layout.about_layout);

		Button cv_aboutOk = (Button) findViewById(R.id.aboutButton);
		cv_aboutOk.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.aboutButton) {
			finish();
		}
	}
}
