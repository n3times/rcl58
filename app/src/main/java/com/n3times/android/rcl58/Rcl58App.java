/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58;

import android.content.Context;
import android.content.pm.PackageManager;

import com.n3times.android.rcl58.utilities.Assets;
import com.n3times.android.rcl58.utilities.Prefs;
import com.n3times.android.rcl58.vio.Vio;

import java.io.File;
import java.io.IOException;

/**
 * Installs and initializes the Application.
 *
 * <p>Note that the initialization cannot be done by the launcher activity, because it is
 * not guaranteed to be the first activity to be created. This may happen on restart, if the
 * application process was killed while an activity other than the launcher was on top.
 */
public class Rcl58App extends android.app.Application {
    // Location of the read-only assets
    public static final String ASSET_BASE = "file:///android_asset";

    // Locations of the files related to the library programs
    public static final String HELP_DIR = "help";
    public static final String HELP_CSS_FILENAME = "help.css";
    public static final String HLP_FILENAME  = "help.hlp";
    public static final String HTML_MIME_TYPE = "text/html";
    public static final String SRC_FILENAME = "source.src";
    public static final String NAME_FILENAME = "name.txt";

    // Locations of the library volumes
    private static final String LIB_MODULE_PATH = "ModulesLib/ML";
    private static final String LIB_EXAMPLES_PATH = "ExamplesLib/Default";

    // File where the internal state of the engine gets saved
    private static final String STATE_FILENAME = "state";

    /**
     * When starting the app, we note whether we are just restarting the app, installing
     * the app afresh or installing a different version.
     *
     * <p>Possible values:
     * <ul>
     *     <li>FRESH_INSTALL: the application was never installed in the device,
     *     (or it it was, it had been uninstalled)</li>
     *     <li>RESTART: the application was already installed but it is being restarted.
     *     It was killed by Android to make space for other applications. From here we
     *     should be able to restore the state before the application was killed</li>
     *     <li>UPGRADE: an older version of the application was already installed. This is
     *     the first time the new version is started.</li>
     *     <li>DOWNGRADE: a newer version of the application was already installed. Somehow,
     *     the user decided to install an older version.</li>
     * </ul>
     */
    public enum StartType {
        FRESH_INSTALL, RESTART, UPGRADE, DOWNGRADE
    }
    public static StartType startType;

    @Override
    public void onCreate() {
        super.onCreate();

        // Load the native shared library that has the engine, that is
        // the internal state and the logic of the calculator
        System.loadLibrary("RCL58");

        // Determine whether we are installing, upgrading, downgrading or just
        // restarting the application
        final int previousVersion = Prefs.instance(this).getVersion();
        int currentVersion = getVersionCode(this);

        if (previousVersion == 0) {
            startType = StartType.FRESH_INSTALL;
        } else if (previousVersion == currentVersion) {
            startType = StartType.RESTART;
        } else if (previousVersion < currentVersion) {
            startType = StartType.UPGRADE;
        } else {
            startType = StartType.DOWNGRADE;
        }

        // Determine whether we are going to reuse the previous state of the engine
        String statePath = this.getFilesDir().getAbsolutePath() + "/" + STATE_FILENAME;

        boolean hasRestarted = startType == StartType.RESTART;

        if (!hasRestarted) {
            installExamplesLibrary();
            installMasterLibrary();

            Prefs.instance(this).setVersion(Rcl58App.getVersionCode(this));
        }

        boolean loadFromStatePath = hasRestarted;
        if (loadFromStatePath) {
            boolean stateFileOK =
                    Prefs.instance(this).isStateSynced() && (new File(statePath)).exists();

            if (!stateFileOK) {
                // The state wasn't properly synced.
                // Start from scratch to avoid any potential unstable state.
                loadFromStatePath = false;
            }
        }

        // Initialize the engine.
        // If loadFromStatePath is true, the engine will load the contents of the file at statePath.
        // Otherwise, it will only use that path for saving the state later.
        Vio.makeVio(this, getFilesDir() + "/%02d/" + SRC_FILENAME, statePath, loadFromStatePath);
    }

    /**
     * Copies Master Library programs from assets to the file system.
     * This is needed because the cross platform native engine is not aware of Android assets.
     */
    private void installMasterLibrary() {
        String src = LIB_MODULE_PATH;
        String dst = getFilesDir().getAbsolutePath();

        for (int i = 1; i <= 25; i++) {
            try {
                String srcFolder = String.format(src + "/%02d", i);
                String dstFolder = String.format(dst + "/%02d", i);
                String srcFile = srcFolder + "/" + SRC_FILENAME;
                String dstFile = dstFolder + "/" + SRC_FILENAME;
                File file = new File(dstFolder);

                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new IOException();
                    }
                }

                Assets.copyAsset(getAssets(), srcFile, dstFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Copies Examples programs from assets to the file system.
     * This is needed because the cross platform native engine is not aware of Android assets.
     */
    private void installExamplesLibrary() {
        String src = LIB_EXAMPLES_PATH;
        String dst = getFilesDir().getAbsolutePath();

        String[] programs = {"3n1", "Biorhythms", "Fractions", "HelloWorld", "TheMatrix", "TheWave"};

        for (String program : programs) {
            try {
                String srcFolder = src + "/" + program;
                String srcFile = srcFolder + "/" + SRC_FILENAME;

                String dstFolder = dst + "/" + program;
                String dstFile = dstFolder + "/" + SRC_FILENAME;
                File file = new File(dstFolder);

                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new IOException();
                    }
                }

                Assets.copyAsset(getAssets(), srcFile, dstFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the version code of the application.
     */
    public static int getVersionCode(Context context) {
        int v = 0;

        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return v;
    }

    /**
     * Gets the version name of the application.
     */
    public static String getVersionName(Context context) {
        String v = "Unknown version";

        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return v;
    }
}
