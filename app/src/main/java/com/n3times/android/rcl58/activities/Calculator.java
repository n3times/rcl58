/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.activities;

import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.n3times.android.rcl58.Rcl58App;
import com.n3times.android.rcl58.R;
import com.n3times.android.rcl58.views.CalculatorMenu;
import com.n3times.android.rcl58.views.Display;
import com.n3times.android.rcl58.views.Keyboard;
import com.n3times.android.rcl58.views.NavigationBar;
import com.n3times.android.rcl58.views.OnItemClickedListener;
import com.n3times.android.rcl58.views.OnKeyActionListener;
import com.n3times.android.rcl58.vio.State;
import com.n3times.android.rcl58.vio.Vio;
import com.n3times.android.rcl58.vio.VioLibrary;
import com.n3times.android.rcl58.vio.VioListener;
import com.n3times.android.rcl58.vio.VioVolume;

/**
 * The main/launcher activity.
 *
 * <p>Displays the keyboard and the display. Allows the user to interact with the keyboard.
 * Allows to navigate to the other activities such as the printer and the listing.
 */
public class Calculator extends BaseActivity implements VioListener {
    private static final String LOG_TAG = "Calculator";

    private static final int MENU_FADE_MS = 300;

    private static final String APP_NAME      = "RCL-58";
    private static final String MORE_INFO     = "For additional info: \nMore > Settings > What's New";
    private static final String WELCOME       = "Welcome to " + APP_NAME;
    private static final String UPGRADED      = "Upgraded to Version %s\n" + MORE_INFO;
    private static final String DOWNGRADED    = "Downgraded to Version %s";
    private static final String NAV_BAR_TITLE = "menu";
    private static final String CANCEL        = "Cancel";
    private static final String OK            = "OK";
    private static final String EXIT          = "Exit";

    private NavigationBar navigationBar;
    private CalculatorMenu menu;
    private Display display;
    private Keyboard keyboard;

    private boolean isKeyPressed;
    private boolean isAnimating;

    // Used so we only update actual changes
    private int lastProgram;
    private long lastProgramId;
    private boolean lastSecondModifier;
    private String lastDisplay = "";
    private int lastDotPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Old work around for Android bug when installing and opening app directly from Google Play
        // See https://code.google.com/p/android/issues/detail?id=26658
        /*if (!isTaskRoot()) {
             finish();
             return;
        }*/

        setContentView(R.layout.activity_calculator);

        display = (Display) findViewById(R.id.listView1);
        navigationBar = (NavigationBar)  findViewById(R.id.mainNavigationBar1);
        menu = (CalculatorMenu) findViewById(R.id.mainToolbar1);
        keyboard = (Keyboard) findViewById(R.id.imageView1);

        if (Rcl58App.startType != Rcl58App.StartType.RESTART) {
            welcome();
        }

        startInteracting();
    }

    /**
     * Welcome the user in case of a fresh install or an upgrade.
     */
    private void welcome() {
        String message = "?";

        if (Rcl58App.startType == Rcl58App.StartType.UPGRADE) {
            message = String.format(UPGRADED, Rcl58App.getVersionName(this));
        } else if (Rcl58App.startType == Rcl58App.StartType.DOWNGRADE) {
            message = String.format(DOWNGRADED, Rcl58App.getVersionName(this));
        } else if (Rcl58App.startType == Rcl58App.StartType.FRESH_INSTALL) {
            message = WELCOME;
        }

        new AlertDialog.Builder(this)
                .setTitle(APP_NAME)
                .setMessage(message)
                .setPositiveButton("OK", null).create().show();
    }

    /**
     * Sets the views to listen to UI events.
     */
    private void startInteracting() {
        // Handle keyboard
        keyboard.setListener(new OnKeyActionListener() {
            @Override
            public void onKeyPressed(int row, int col) {
                if (isKeyPressed) {
                    // Only 1 key can be pressed at a given time
                    return;
                }

                if (menu.getVisibility() == View.VISIBLE) {
                    fadeOutMenu();
                } else {
                    Vio.instance().keyPressed(row, col);
                    isKeyPressed = true;
                }
            }

            @Override
            public void onKeyReleased(int row, int col) {
                if (!isKeyPressed) {
                    return;
                }

                Vio.instance().keyReleased();
                isKeyPressed = false;
            }
        });

        // Handle navigation
        navigationBar.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                switch (item) {
                    case LEFT:
                        startActivity(new Intent(Calculator.this, Listing.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                        break;
                    case MIDDLE:
                        fadeInMenu();
                        break;
                    case RIGHT:
                        Intent intent = new Intent(Calculator.this, Printer.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        });

        // Handle menu selection
        menu.setListener(new OnItemClickedListener() {
            @Override
            public void onItemClicked(Item item) {
                Class[] nextActivity = {
                        Listing.class,
                        BaseActivity.lastHelpClass(),
                        BaseActivity.lastLibClass(),
                        BaseActivity.lastSettingsClass(),
                        Printer.class};

                Intent intent = new Intent(Calculator.this, nextActivity[item.ordinal()]);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                // Do the animated transition for listing and printer only
                if (item == Item.LEFT) {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                } else if (item == Item.RIGHT) {
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show navigation bar and display
        navigationBar.setVisibility(View.VISIBLE);
        navigationBar.setAlpha(1.0f);
        display.setVisibility(View.VISIBLE);
        display.setAlpha(1.0f);

        // Hide menu
        menu.setVisibility(View.INVISIBLE);

        updateUi();

        // Start listening to the engine so we can update the display as the state changes
        Vio.instance().addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Vio.instance().removeListener(this);
    }

    @Override
    public void onBackPressed() {
        // Ask the user if they really want to exit the app.
        new AlertDialog.Builder(this)
            .setTitle(EXIT + " " + APP_NAME)
            .setNegativeButton(CANCEL, null)
            .setPositiveButton(OK, new Dialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);
                }
            }).create().show();
    }

    @Override
    public void stateChanged() {
        updateUi();
    }

    /**
     * Updates the parts of UI that have changed
     */
    private void updateUi() {
        char[] text = new char[12];
        State state = Vio.instance().getState();

        // Set the display
        for (int i = 0; i < 12; i++) {
            text[i] = state.displayGetChar(i);
        }
        String newDisplay = new String(text);
        int dotPosition = state.displayGetDotPosition();
        if (!lastDisplay.equals(newDisplay) || lastDotPosition != dotPosition) {
            display.setDisplay(new String(text), state.displayGetDotPosition());
            lastDisplay = newDisplay;
            lastDotPosition = dotPosition;
        }

        // Set the navigation bar
        int currentProgram = state.moduleGetSelectedProgram();
        long currentProgramId = state.programGetId();
        if (lastProgram != currentProgram || lastProgramId != currentProgramId) {
            lastProgram = currentProgram;
            lastProgramId = currentProgramId;
            String title = NAV_BAR_TITLE;
            if (currentProgram > 0) {
                VioVolume masterLibrary = VioLibrary.instance(this).getMasterVolume();
                String programName = masterLibrary.getProgram(currentProgram - 1).getName();
                title = String.format(Locale.ENGLISH, "%02d - %s", currentProgram, programName);
            } else if (currentProgramId > 0) {
                VioVolume examplesLibrary = VioLibrary.instance(this).getExamplesVolume();
                title = examplesLibrary.getProgram((int) currentProgramId - 1).getName();
            }
            navigationBar.setTitle(title);
        }

        // Highlight the right arrow if there are lines the user hasn't seen
        boolean printed = Printer.getLastPrinterCount() != state.printerGetAlltimePrintedCount();
        navigationBar.setHighlightedRightArrow(printed);

        // 2nd key pressed yellows the navigation bar
        boolean currentSecondModifier = state.isSecondModifier();
        if (lastSecondModifier != currentSecondModifier) {
            lastSecondModifier = currentSecondModifier;
            navigationBar.setHighlightedTitle(currentSecondModifier);
        }
    }

    /**
     * Makes the menu disappear, and hides the navigation bar and display.
     */
    private void fadeInMenu() {
        if (isAnimating) {
            Log.d(LOG_TAG, "User is fast! They will have to click again, but no harm");
            return;
        }
        isAnimating = true;

        // Fade out navigation bar and display
        navigationBar.animate().alpha(0).setDuration(MENU_FADE_MS);
        display.animate().alpha(0).setDuration(MENU_FADE_MS);

        // Fade in menu
        menu.setAlpha(0f);
        menu.setVisibility(View.VISIBLE);
        menu.animate().alpha(1).setDuration(MENU_FADE_MS)
        .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                display.setVisibility(View.INVISIBLE);
                navigationBar.setVisibility(View.GONE);
                isAnimating = false;
            }
        });		
    }

    /**
     * Makes the menu disappear, bringing back the navigation bar and display.
     */
    private void fadeOutMenu() {
        if (isAnimating) {
            Log.d(LOG_TAG, "User is fast! They will have to click again, but no harm");
            return;
        }
        isAnimating = true;

        // Fade in navigation bar and display
        navigationBar.setVisibility(View.VISIBLE);
        navigationBar.animate().alpha(1f).setDuration(MENU_FADE_MS);
        display.setVisibility(View.VISIBLE);
        display.animate().alpha(1f).setDuration(MENU_FADE_MS);

        // Fade out menu
        menu.animate().alpha(0f).setDuration(MENU_FADE_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        menu.setVisibility(View.GONE);
                        isAnimating = false;
                    }
                });
    }
}
