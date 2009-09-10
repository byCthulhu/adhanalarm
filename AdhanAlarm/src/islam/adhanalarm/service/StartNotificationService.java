package islam.adhanalarm.service;

import java.lang.Runnable;
import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class StartNotificationService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {

		/**
		 * We do the actual work in a separate thread since a Service has a limited life and we want to guarantee completion
		 */
		final class StartNotificationTask implements Runnable {

			private Context context;
			private Intent intent;

			public StartNotificationTask(Context c, Intent i) {
				context = c; intent = i;
			}

			public void run() {
				if(VARIABLE.settings == null) VARIABLE.settings = context.getSharedPreferences("settingsFile", Context.MODE_PRIVATE); // Started on device bootup

				if(!VARIABLE.mainActivityIsRunning) {
					StartNotificationReceiver.setNext(context);
				} else {
					Intent i = new Intent(context, AdhanAlarm.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i); // Update the gui marker and set the notification for the next prayer
				}

				short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
				long actualTime = intent.getLongExtra("actualTime", (long)0);
				if(timeIndex == -1) {
					WakeLock.release(); // Got here from boot so no need to notify current			
				} else {
					Notifier.start(context, timeIndex, actualTime); // Notify the user for the current time, need to do this last since it releases the WakeLock
				}
			}
		}

		new Thread(new StartNotificationTask(this, intent)).start();
	}
}