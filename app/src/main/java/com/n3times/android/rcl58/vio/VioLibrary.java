/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

import android.content.Context;

import com.n3times.android.rcl58.Rcl58App;
import com.n3times.android.rcl58.utilities.Assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The singleton program library, with the Master Library as well as the Examples Library.
 */
public class VioLibrary {
    private static VioLibrary library;

    private static VioVolume masterVolume;
    private static VioVolume examplesVolume;

    private static final String EXAMPLES_PATH = "ExamplesLib/Default";
    private static final String ML_PATH       = "ModulesLib/ML";

    /**
     * Gets the singleton library.
     *
     * <p>From here, one can access the volumes and its programs.
     */
    public static synchronized VioLibrary instance(Context context) {
        if (library == null) {
            library = new VioLibrary(context);
        }

        return library;
    }

    /**
     * Makes a volume of a given 'name' with programs located at 'path'
     */
    private VioVolume makeVolume(Context context, String name, String path) {
        List<String> dataLib = new ArrayList<>();

        try {
            for (String s : context.getAssets().list(path)) {
                // We are really interested in the directories, so we filter out the
                // files. This is a "hack" that works because we know the name of the files.
                // It could be done cleanly but Android is very very slow in determining
                // whether an asset path is a directory
                if (!s.contains(".") && !s.equals("module")) {
                    dataLib.add(s);
                }
            }

            Collections.sort(dataLib);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
        VioProgram[] programs = new VioProgram[dataLib.size()];
        for (int i = 0; i < dataLib.size(); i++) {
            String nameUri = path + "/" + dataLib.get(i) + "/" + Rcl58App.NAME_FILENAME;
            String programName = Assets.fileAsString(context.getAssets(), nameUri);
            String helpUri = path + "/" + dataLib.get(i) + "/" + Rcl58App.HLP_FILENAME;
            String srcUri = context.getFilesDir().getAbsolutePath()
                    + "/" + dataLib.get(i) + "/" + Rcl58App.SRC_FILENAME;

            programs[i] = new VioProgram(programName, helpUri, srcUri);
        }

        return new VioVolume(name, programs);
    }

    /**
     * Keep constructor private, to ensure we have indeed a singleton.
     */
    private VioLibrary(Context context) {
        masterVolume = makeVolume(context, "Master Library", ML_PATH);
        examplesVolume = makeVolume(context, "Example Programs", EXAMPLES_PATH);
    }

    /**
     * Gets a volume with the programs of the "Master Library" module.
     *
     * <p>This module was provided in the original calculator.
     */
    public VioVolume getMasterVolume() {
        return masterVolume;
    }

    /**
     * Gets a volume with the example programs.
     *
     * <p>These additional programs either illustrate some functionality or are just fun.
     */
    public VioVolume getExamplesVolume() {
        return examplesVolume;
    }
}
