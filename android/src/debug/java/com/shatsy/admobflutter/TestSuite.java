package com.shatsy.admobflutter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.ads.mediationtestsuite.MediationTestSuite;

public class TestSuite {
    private TestSuite() {
    }

    public static void launchTestSuite(Context context, @Nullable String testDevice) {
        if (!TextUtils.isEmpty(testDevice)) {
            MediationTestSuite.addTestDevice(testDevice);
        }
        MediationTestSuite.launch(context);
    }
}
