package com.cendrex;

import android.content.Context;
import android.content.Intent;

import com.cendrex.activity.IntroActivity;
import com.parse.ParsePushBroadcastReceiver;

public class NotificationReceiver extends ParsePushBroadcastReceiver {

	@Override
	protected void onPushOpen(Context context, Intent intent) {
		Intent i = new Intent(context, IntroActivity.class);
		i.putExtras(intent.getExtras());
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

}
