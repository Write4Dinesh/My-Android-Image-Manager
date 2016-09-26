package com.shrijay.image.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

/**
 * Created by dinesh.k.masthaiah on 12-04-2016.
 */
public class ShrijayUtility {
   /* public static boolean isConnected() {
        NetworkInfo info = ((ConnectivityManager) HorizonsEventApplication.mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }*/

    public static String getPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        if (!isStringNullOrEmpty(phoneNumber)) {
            //ShrijayLogger.debugLog("HorizonsUtility", "getPhoneNumber(), the phone number =" + phoneNumber);
        } else {
            //ShrijayLogger.debugLog("HorizonsUtility", "getPhoneNumber(), the phone number is empty or null");
        }
        return telephonyManager.getLine1Number();
    }

    public static final boolean isStringNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static int getDiskCacheSize() {
        StatFs fileSystemStat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) fileSystemStat.getBlockSizeLong() * (long) fileSystemStat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        //ShrijayLogger.debugLog("HorizonsUtility", "Internal Memory(MBs) = " + megAvailable);
        int diskCacheSize = ((int) megAvailable / 100) * 2;// 2%
        diskCacheSize = diskCacheSize > 50 ? 50 : diskCacheSize;
        //ShrijayLogger.debugLog("HorizonsUtility", "Disk Cache Space in MBs = " + diskCacheSize);
        return diskCacheSize;
    }

    /*public static int getServerConfiguredPageSize(String pageSizeType) {
        //\\MainConfig pageSizeConfig = HorizonsDataStore.getInstance().getMainConfig(pageSizeType);
        if (pageSizeConfig != null && !isStringNullOrEmpty(pageSizeConfig.mType)) {
            return Integer.parseInt(pageSizeConfig.mType);
        }
        return 0;
    }*/

    public static boolean isPreMarshMellow() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M);
    }

}
