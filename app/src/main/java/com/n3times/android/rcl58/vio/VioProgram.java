/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * A program with name, source and help file.
 */
public class VioProgram {
    private String name;
    private String helpUri;
    private String srcUri;

    /**
     * A program is defined by its name, and its source and help files.
     */
    public VioProgram(String name, String helpUri, String srcUri) {
        this.name = name;
        this.helpUri = helpUri;
        this.srcUri = srcUri;
    }

    /**
     * Gets the name of the program.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the URI of the help file for the program.
     */
    public String getHelpUri() {
        return helpUri;
    }

    /**
     * Returns the URI of the source file for the program.
     */
    public String getSrcUri() {
        return srcUri;
    }
}