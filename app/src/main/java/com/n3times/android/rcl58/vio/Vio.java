/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.n3times.android.rcl58.utilities.Debug;
import com.n3times.android.rcl58.utilities.Prefs;

/**
 * Interface between the client and the native engine.
 *
 * <p>The client should call the public methods of Vio, whenever the user takes
 * some action such as pressing a key of the keyboard. This will result in a call
 * to a native method of the engine.
 *
 * <p>After a call, the client should query the internal state of the engine in
 * order to update its views.
 *
 * <p>Finally the client should look at the return value (Next) to know if it should call
 * next() on the engine and when. It's up to the client to create the actual threading
 * mechanism. The engine dictates the tempo, that is when the client should call next()
 * and when it should wait.
 */
public class Vio {
    private static final String MESSAGE_RESUME = "Resuming";
    private static final String MESSAGE_START  = "Starting";
    private static final String MESSAGE_SAVE   = "Saving";

    private static final int MINIMUM_CALC_MS = 20;

    /**
     * Singleton engine.
     */
    private static Vio vio;

    /**
     * A pointer to the native implementation.
     *
     * <p>Used by the native interface.
     */
    private long nativeImpl;

    /**
     * Singleton state.
     */
    private State state;

    /**
     * Handler where we will be posting our delayed messages.
     */
    private Handler timer;

    /**
     * A runnable that will be calling next().
     *
     * <p>The engine requested that next() should be called within a given delay.
     */
    private Runnable delayedNextRunnable;

    /**
     * A runnable that will be calling resume.
     *
     * <p>The engine is paused and there has been a request to resume it after some delay.
     */
    private Runnable delayedResumeRunnable;

    /**
     * next() needs to be called on resume.
     */
    private Next pendingNext;

    /**
     * The state needs to be synced.
     */
    private boolean dirty;

    /**
     * The application context.
     */
    private Context context;

    /**
     * The path used to save the state.
     */
    private String statePath;

    /**
     * The list of engine listeners.
     */
    private ArrayList<VioListener> listeners = new ArrayList<>();

    /**
     * Indicates that the state has changed and needs to be synced to storage.
     */
    private void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            Prefs.instance(context).setStateSynced(!dirty);
        }
    }

    private Vio(Context context, String modulePath, String statePath, boolean resume) {
        nativeImpl = vioNew(resume, resume ? statePath : modulePath);

        // Keep a reference to the context of the application, instead of being tied to an activity
        this.context = context.getApplicationContext();

        this.statePath = statePath;

        // Create a handler so we can post messages to the UI thread
        timer = new Handler();
    }

    /**
     * Creates the Vio object.
     *
     * @param context the application context
     * @param modulePath the location of the master library
     * @param statePath the location of the state file
     * @param resume whether we are just restarting (false if fresh install/upgrade)
     * @return the Vio object
     */
    public static synchronized Vio makeVio (
            Context context, String modulePath, String statePath, boolean resume) {
        if (vio != null) {
            throw new AssertionError();
        }

        String message = resume ? MESSAGE_RESUME : MESSAGE_START;

        Log.d("Vio", message);
        Debug.toast(context, message, true);

        vio = new Vio(context, modulePath, statePath, resume);

        // set vio as "dirty" as soon as possible, so later, in case of early
        // crash, we don't initialize vio from a potentially unstable state
        vio.setDirty(true);

        if (resume) {
            Next next = vio.next();
            vio.handleNext(next);
        }

        return vio;
    }

    /**
     * Gets the singleton Vio object.
     */
    public static Vio instance() {
        return vio;
    }

    /**
     * Gets a reference to the state that the client can query.
     */
    public State getState() {
        if (state == null) {
            state = new State(nativeImpl);
        }

        return state;
    }

    /**
     * Informs the engine that a key was pressed.
     *
     * @param row the row of the key (1 to 9)
     * @param col the column of the key (1 to 5)
     */
    public void keyPressed(int row, int col) {
        Next next = vio.pressKey(row, col);
        handleNext(next);
    }

    /**
     * Informs the engine that the last pressed key was released.
     */
    public void keyReleased() {
        Next next = vio.releaseKey();
        handleNext(next);
    }

    /**
     * Selects a given module program.
     *
     * @param index the index of the program within the module
     */
    public void moduleSelectProgram(int index) {
        Next next = vio.selectModuleProgram(index);
        handleNext(next);
    }

    /**
     * Loads a program into the calculator's memory.
     *
     * <p>The id is an arbitrary positive integer that identifies, in the eyes of the client,
     * the program.
     */
    public void loadProgram(String srcUri, long id) {
        Next next = vio.programLoad(srcUri, id);
        handleNext(next);
    }

    /**
     * Resets the calculator to its original state.
     *
     * <p>>This preserves the libraries.
     */
    public void reset() {
        Next next = vio.calculatorReset();
        handleNext(next);
    }

    /**
     * Saves the current state of the calculator to a given path.
     */
    private void saveState(String path) {
        Next next = vio.stateSave(path);
        handleNext(next);

        Log.d("Vio", MESSAGE_SAVE);
        Debug.toast(context, MESSAGE_SAVE);
    }

    /**
     * Clears printer buffer.
     *
     * <p>Similar to changing the roll of paper.
     */
    public void resetPrinter() {
        Next next = vio.printerReset();
        handleNext(next);
    }

    /**
     * Pauses the engine.
     *
     * <p>Additionally, saves the internal state if needed.
     */
    public void pause() {
        if (dirty) {
            Vio.instance().saveState(statePath);
            setDirty(false);
        }

        if (delayedNextRunnable != null) {
            timer.removeCallbacks(delayedNextRunnable);
            delayedNextRunnable = null;
        }

        if (delayedResumeRunnable != null) {
            timer.removeCallbacks(delayedResumeRunnable);
            delayedResumeRunnable = null;
        }
    }

    /**
     * Resumes the engine.
     */
    public void resume() {
        if (delayedResumeRunnable != null) {
            timer.removeCallbacks(delayedResumeRunnable);
            delayedResumeRunnable = null;
        }

        if (pendingNext != null) {
            handleNext(pendingNext);
        }
    }

    /**
     * Asks the engine to resume in 'ms' milliseconds.
     */
    public void resume(int ms) {
        if (delayedResumeRunnable != null) {
            timer.removeCallbacks(delayedResumeRunnable);
            delayedResumeRunnable = null;
        }

        delayedResumeRunnable = new Runnable() {
            @Override
            public void run() {
                resume();
            }
        };
        timer.postDelayed(delayedResumeRunnable, ms);
    }

    /**
     * Adds a listener so it knows about state changes.
     */
    public void addListener(VioListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(VioListener listener) {
        listeners.remove(listener);
    }

    /**
     * The threading logic.
     */
    private void handleNext(Next next) {
        Next.Type type = next.getType();

        if (type == Next.Type.NEXT_NO_OP) {
            return;
        }

        setDirty(true);

        pendingNext = null;

        if (delayedNextRunnable != null) {
            timer.removeCallbacks(delayedNextRunnable);
            delayedNextRunnable = null;
        }

        if (type == Next.Type.NEXT_NOW) {
            while (type == Next.Type.NEXT_NOW) {
                next = vio.next();
                type = next.getType();
            }
            handleNext(next);
        } else if (type == Next.Type.NEXT_DELAY) {
            delayedNextRunnable = new Runnable() {
                @Override
                public void run() {
                    Next next = vio.next();
                    handleNext(next);
                }
            };

            // Don't go faster than MINIMUM_CALC_MS to keep the UI responsive
            timer.postDelayed(delayedNextRunnable, Math.max(MINIMUM_CALC_MS, next.getMs()));

            pendingNext = next;
        }

        for (VioListener listener : listeners) {
            listener.stateChanged();
        }
    }

    /*********************************************************************************************
     *                                                                                           *
     *                                Native Engine Methods                                      *
     *                                                                                           *
     *********************************************************************************************/

    /**
     * Informs the engine that a given key was pressed.
     *
     * @param row the row of the key (1 to 9)
     * @param col the column of the key (1 to 5)
     */
    private native Next pressKey(int row, int col);

    /**
     * Informs the engine that the currently pressed key was released.
     */
    private native Next releaseKey();

    /**
     * Makes the engine go to its next state.
     */
    private native Next next();

    /**
     * Informs the engine that a given module program was selected by the user.
     *
     * @param index the index of the program within the module (currently 1 to 25)
     */
    private native Next selectModuleProgram(int index);

    /**
     * Loads a program into memory.
     *
     * @param srcUri the location of the source
     * @param id an arbitrary number > 0 that the client can use to identify the program
     */
    private native Next programLoad(String srcUri, long id);

    /**
     * Clears the printer buffer.
     */
    private native Next printerReset();

    /**
     * Clears the state of the engine.
     *
     * <p>Does not uninstall example or master library programs
     */
    private native Next calculatorReset();

    /**
     * Saves the internal state of the engine to a given path.
     */
    private native Next stateSave(String path);

    /**
     * Creates a new engine.
     *
     * @param useState tells whether we are creating a new engine based on a saved state
     * @param path the path to the saved state or to the Master Library volume
     * @return a pointer to the native implementation.
     */
    private native long vioNew(boolean useState, String path);
}