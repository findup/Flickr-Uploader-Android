package net.swingingblue.flickruploader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
			
			
		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
			
		}
	}

}
