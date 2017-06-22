package pl.llp.aircasting.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.RecordWithoutGPSAlert;
import pl.llp.aircasting.activity.SaveSessionActivity;
import pl.llp.aircasting.activity.StartFixedSessionActivity;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;

/**
 * Created by radek on 21/06/17.
 */
public class ToggleAircastingHelper {
    public AppCompatDelegate delegate;
    private Context context;
    private Activity activity;
    private SessionManager sessionManager;
    private SettingsHelper settingsHelper;
    private LocationManager locationManager;
    private LocationHelper locationHelper;

    public ToggleAircastingHelper(Activity activity,
                                  SessionManager sessionManager,
                                  SettingsHelper settingsHelper,
                                  LocationManager locationManager,
                                  LocationHelper locationHelper,
                                  AppCompatDelegate delegate,
                                  Context context) {
        this.activity = activity;
        this.sessionManager = sessionManager;
        this.settingsHelper = settingsHelper;
        this.locationManager = locationManager;
        this.locationHelper = locationHelper;
        this.delegate = delegate;
        this.context = context;
    }

    public void toggleAirCasting() {
        if (sessionManager.isSessionStarted()) {
            stopAirCasting();
        } else {
            startAirCasting();
        }
    }

    public void stopAirCasting() {
        Session session = sessionManager.getSession();

        if (session.isFixed())
            stopFixedAirCasting(session);
        else
            stopMobileAirCasting(session);
    }

    private void stopMobileAirCasting(Session session) {
        locationHelper.stop();
        Long sessionId = session.getId();
        if (session.isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession(sessionId);
        } else {
            sessionManager.stopSession();
            Intent intent = new Intent(activity, SaveSessionActivity.class);
            intent.putExtra(Intents.SESSION_ID, sessionId);
            activity.startActivityForResult(intent, Intents.SAVE_DIALOG);
        }
    }

    private void stopFixedAirCasting(Session session) {
        locationHelper.stop();
        Long sessionId = session.getId();
        if (session.isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession(sessionId);
        } else {
            sessionManager.stopSession();
            sessionManager.finishSession(sessionId);
        }
    }

    private void startAirCasting() {
        if (settingsHelper.isFixedSessionStreamingEnabled())
            startFixedAirCasting();
        else
            startMobileAirCasting();
    }

    private void startMobileAirCasting() {
        if (settingsHelper.areMapsDisabled()) {
            sessionManager.startMobileSession(true);
        } else {
            if (locationHelper.getLastLocation() == null) {
                RecordWithoutGPSAlert recordAlert = new RecordWithoutGPSAlert(context, delegate, sessionManager, true);
                recordAlert.display();
                return;
            } else {
                sessionManager.startMobileSession(false);

                if (settingsHelper.hasNoCredentials()) {
                    Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (locationHelper.hasNoGPSFix()) {
                        Toast.makeText(context, R.string.no_gps_fix_warning, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, R.string.gps_off_warning, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void startFixedAirCasting() {
        if (settingsHelper.hasNoCredentials())
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        else
            activity.startActivity(new Intent(activity, StartFixedSessionActivity.class));
    }
}