package com.cendrex.utils;

import java.io.File;

import android.os.Environment;

public class Consts {

	public static final String KEY_CHOOSE_PDF = "key_choose_pdf";
	public static final String APP_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator + "cendrex";
	public static final String OPEN_LIBRARY_FILE = "open_doc_file";
}
