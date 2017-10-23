package istat.android.telephony.sms.tools;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

public final class Util {
    private Util() {

    }

    public static String convertTolocalPhoneNumber(String phone) {
        return phone.replaceFirst("^(00\\d{3})", "").replaceFirst(
                "^(\\+\\d{3})", "");
    }

    public static String convertToInternationalPhoneNumber(String phone,
                                                           String prefix) {
        return prefix + phone;
    }

    public static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    public static int optSmsBodyPartNumber(String body) {
        if (body.length() <= 160) {
            return 1;
        } else {
            SmsManager sms = SmsManager.getDefault();
            return sms.divideMessage(body).size();
        }
    }

    public static int sendSMS(String address, String body,
                              PendingIntent sendPIntent, PendingIntent receiveIntent) {
        SmsManager sms = SmsManager.getDefault();
        if (body == null) {
            throw new IllegalArgumentException("Sms body can't be null.");
        }
        if (TextUtils.isEmpty(address)) {
            throw new IllegalArgumentException("Sms address can't be null or empty.");
        }
        final ArrayList<String> parts = sms.divideMessage(body);
        if (parts.size() <= 1) {
            sms.sendTextMessage(address, null, body, sendPIntent, receiveIntent);
            return 1;
        } else {

            ArrayList<PendingIntent> sendIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
            for (int i = 0; i <= parts.size(); i++) {
                sendIntents.add(sendPIntent);
                deliveryIntents.add(receiveIntent);
            }
            if (sendPIntent == null)
                sendIntents = null;
            if (receiveIntent == null)
                deliveryIntents = null;
            sms.sendMultipartTextMessage(address, null, parts, sendIntents,
                    deliveryIntents);
            return parts.size();
        }
    }

    public static int sendSMS(String address, String body, Intent sendIntent,
                              Intent receiveIntent, Context context) {
        SmsManager sms = SmsManager.getDefault();
        if (body.length() <= 160) {
            sms.sendTextMessage(address, null, body,
                    PendingIntent.getBroadcast(context, 0, sendIntent, 0),
                    PendingIntent.getBroadcast(context, 0, receiveIntent, 0));
            return 1;
        } else {
            ArrayList<String> parts = sms.divideMessage(body);
            ArrayList<PendingIntent> sendIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
            if (sendIntent == null && receiveIntent == null)
                for (int i = 0; i <= parts.size(); i++) {
                    sendIntents.add(PendingIntent.getBroadcast(context, 0,
                            sendIntent, 0));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0,
                            receiveIntent, 0));
                }
            if (sendIntent == null)
                sendIntents = null;
            if (receiveIntent == null)
                deliveryIntents = null;
            sms.sendMultipartTextMessage(address, null, parts, sendIntents,
                    deliveryIntents);
            return parts.size();
        }
    }

    public final static void startSmsIntent(Context context, String smBody, String... number) {
        String finalNumber = number[0];
        for (String num : number) {
            if (!TextUtils.isEmpty(num)) {
                finalNumber += ";" + num;
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
//        {
//            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context); // Need to change the build to API 19
//
//            Intent sendIntent = new Intent(Intent.ACTION_SEND);
//            sendIntent.setType("text/plain");
//            sendIntent.putExtra(Intent.EXTRA_TEXT, smBody);
//
//            if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
//            // any app that support this intent.
//            {
//                sendIntent.setPackage(defaultSmsPackageName);
//            }
//            context.startActivity(sendIntent);
//
//        } else // For early versions, do what worked for you before.
//        {
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", finalNumber);
        smsIntent.putExtra("sms_body", smBody);
        context.startActivity(smsIntent);
//        }
    }
}
