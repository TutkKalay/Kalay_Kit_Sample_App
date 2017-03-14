package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cloud on 10/19/15.
 */
public class Utils {

    public static String generateUDID (Context context) {
        SharedPreferences settings = context.getSharedPreferences("setting", 0);

        if (settings.getString("device_imei", "").equals("")) {
            String serial_number = "";
            if (Build.VERSION.SDK_INT >= 9) {
                serial_number = android.os.Build.SERIAL;
            }

            String mac_address = "";
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    mac_address = wifiInfo.getMacAddress();
                }
            }

            if ((serial_number == null || serial_number.equals(""))) {
                int random_number = (int) (Math.random() * 1 + 1) + (int) (Math.random() * 10 + 1) + (int) (Math.random() * 100 + 1) + (int) (Math.random() * 1000 + 1) + (int) (Math.random() * 10000 + 1) + (int) (Math.random() * 100000 + 1) + (int) (Math.random() * 1000000 + 1) + (int) (Math.random() * 10000000 + 1);
                if (random_number < 10000000) {
                    random_number = random_number + 10000000;
                }
                serial_number = String.valueOf(random_number);

            }

            if ((mac_address == null || mac_address.equals(""))) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                mac_address = formatter.format(curDate);
            }

            mac_address = mac_address.replace(":", "");
            String[] new_mac_address = new String[2];
            new_mac_address[0] = mac_address.substring(0, 6);
            new_mac_address[1] = mac_address.substring(6);
            String[] new_serial_number = new String[2];
            new_serial_number[0] = serial_number.substring(0, 4);
            new_serial_number[1] = serial_number.substring(4);
            settings.edit().putString("device_imei", "AN" + new_mac_address[0] + new_serial_number[1] + new_mac_address[1] + new_serial_number[0]).apply();

            return "AN" + new_mac_address[0] + new_serial_number[1] + new_mac_address[1] + new_serial_number[0];
        } else {
            return settings.getString("device_imei", "");
        }
    }

    public static String generateUIDJsonText(String[] UIDs) {
        String result = "";
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();
        try {
            for (int i = 0; i < UIDs.length; i++) {
                json.put("uid", UIDs[i]);
                json.put("interval", 3);
                array.put(json);
            }

            result = array.toString();
        } catch (Exception e) {

        }
        return result;
    }
}
