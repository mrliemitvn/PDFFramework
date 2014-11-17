package com.cendrex.utils;

import java.io.File;

import android.os.Environment;

public class Consts {

	public static final String KEY_CHOOSE_PDF = "key_choose_pdf";
	public static final String APP_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator + "cendrex";
	public static final String OPEN_LIBRARY_FILE = "open_doc_file";

	/* Parse consts. */
	public static final String PARSE_APP_ID = "1IcfMkJqT62pGsw5EAqNiHo8ch7FoAKTlXoxnrin";
	public static final String PARSE_CLIENT_ID = "YHOIoySwS2IGEkbXRXU91jtAHvkAhmiKdb8ySU4J";
	public static final String PARSE_USER_FILE_NAME = "user.txt";
	public static final String PARSE_SHARE_FILE_NAME = "share.txt";

}
