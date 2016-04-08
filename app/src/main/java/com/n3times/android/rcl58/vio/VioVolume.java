/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

/**
 * A collection of programs related to each other.
 */
public class VioVolume {
    private String name;
    private VioProgram[] programs;

    /**
     * A volume has a name and a list of programs.
     */
    public VioVolume(String name, VioProgram[] programs) {
        this.name = name;
        this.programs = programs;
    }

    /**
     * Gets the name of the volume, as shown to the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the total number of programs in the module.
     */
    public int getProgramCount() {
        return programs.length;
    }

    /**
     * Gets a program in the volume.
     *
     * @param index the index of the program (0 to the program count - 1)
     * @return the program at given index
     */
    public VioProgram getProgram(int index) {
        return programs[index];
    }
}
