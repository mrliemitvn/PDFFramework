package com.pdfframework.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pdfframework.R;
import com.pdfframework.mupdf.ChoosePDFActivity;
import com.pdfframework.utils.Consts;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent intent = new Intent(this, ChoosePDFActivity.class);
		intent.putExtra(Consts.KEY_CHOOSE_PDF, true);
		startActivity(intent);
		finish();
	}
}
