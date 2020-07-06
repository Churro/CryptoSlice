package at.tugraz.iaik.cryptoslice.analysis;

import at.tugraz.iaik.cryptoslice.application.Application;
import at.tugraz.iaik.cryptoslice.utils.manifest.AndroidManifest;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.List;

// Check for dangerous permissions
public class PermissionCheckStep extends Step {
  // https://developer.android.com/reference/android/Manifest.permission.html
  private static final ImmutableMap<String, String> DANGEROUS_PERMISSIONS = ImmutableMap.<String, String> builder()
      .put("ACCEPT_HANDOVER", "Can take over active calls") // 28
      .put("ACCESS_BACKGROUND_LOCATION", "Can access location in the background") // 29
      .put("ACCESS_COARSE_LOCATION", "Can access the approximate location of a device") // 1
      .put("ACCESS_FINE_LOCATION", "Can access the precise location of a device") // 1
      .put("ACCESS_MEDIA_LOCATION", "Can access any geographic locations persisted in the user's shared collection") // 29
      .put("ACTIVITY_RECOGNITION", "Can recognize physical activity") // 29
      .put("ADD_VOICEMAIL", "Can add voicemails into the system") // 14
      .put("ANSWER_PHONE_CALLS", "Can answer an incoming phone call") // 26
      .put("BODY_SENSORS", "Can access data from health tracking sensors") // 20
      .put("CALL_PHONE", "Can initiate a phone call without going through the Dialer user interface") // 1
      .put("CAMERA", "Can access the camera device") // 1
      .put("GET_ACCOUNTS", "Can access the list of accounts in the Accounts Service") // 1
      .put("PROCESS_OUTGOING_CALLS", "Can see and redirect outgoing calls") // 1 -> 29
      .put("READ_CALENDAR", "Can read the user's calendar data") // 1
      .put("READ_CALL_LOG", "Can read the user's call log") // 16
      .put("READ_CONTACTS", "Can read the user's contacts data") // 1
      .put("READ_EXTERNAL_STORAGE", "Can read from external storage") // 16
      .put("READ_PHONE_NUMBERS", "Can read access the device's phone number(s)") // 26
      .put("READ_PHONE_STATE", "Can read phone number(s), current cellular network information, status of any ongoing calls") // 1
      .put("READ_SMS", "Can read SMS messages") // 1
      .put("RECEIVE_MMS", "Can monitor incoming MMS messages") // 1
      .put("RECEIVE_SMS", "Can receive SMS messages") // 1
      .put("RECEIVE_WAP_PUSH", "Can receive WAP push messages") // 1
      .put("RECORD_AUDIO", "Can record audio") // 1
      .put("SEND_SMS", "Can send SMS messages") // 1
      .put("USE_SIP", "Can use SIP service") // 9
      .put("WRITE_CALENDAR", "Can write the user's calendar data") // 1
      .put("WRITE_CALL_LOG", "Can write (but not read) the user's call log data") // 16
      .put("WRITE_CONTACTS", "Can write the user's contacts data") // 1
      .put("WRITE_EXTERNAL_STORAGE", "Can write to external storage") // 4
      .build();

  public PermissionCheckStep(boolean enabled) {
    this.name = "Permission Check";
    this.enabled = enabled;
  }

  @Override
  public boolean doProcessing(Analysis analysis) throws AnalysisException {
    Application app = analysis.getApp();
    File manifest = app.getAndroidManifestFile();

    LOGGER.debug("Checking AndroidManifest.xml of " + app.getApplicationName());

    if (AndroidManifest.isDebuggable(manifest)) {
      System.out.println("App is debuggable -> Sensitive data can be logged while the application runs.");
    }

    if (AndroidManifest.allowsBackup(manifest)) {
      System.out.println("App allows backups -> Data / files may remain on the device even after app uninstall.");
    }

    // https://developer.android.com/guide/topics/manifest/uses-sdk-element.html
    //System.out.println(AndroidManifest.getMinSdkVersion(app.getAndroidManifestFile()));

    List<String> permissions = AndroidManifest.getPermissions(manifest);
    for (String permission : permissions) {
      String full = permission.substring(19); // trim android.permission.
      if (DANGEROUS_PERMISSIONS.containsKey(full)) {
        System.out.println(full + ": " + DANGEROUS_PERMISSIONS.get(full) + ".");
      }
    }

    return true;
  }
}
