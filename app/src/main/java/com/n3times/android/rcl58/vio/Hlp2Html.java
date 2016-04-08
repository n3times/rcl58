/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.vio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

/**
 * Translates text from "hlp" format to "html" format.
 *
 * <p>We use the facilities of the native engine to do this. Note that the "hlp"
 * format is a custom format used to write help files for the calculator.
 */
public class Hlp2Html {
    /**
     *  Makes the HTML header.
     */
    private static native String hlp2HtmlInit(String css);

    /**
     * Makes a line of "html" from a line of text in "hlp" format.
     */
    private static native String hlp2HtmlNext(String line);

    /**
     * Makes the HTML footer.
     */
    private static native String hlp2HtmlDone();

    /**
     * Returns the entire help text in HTML format.
     *
     * <p>It's OK because, in the application, the help files are short.
     */
    public static String getHtml(Context context, String cssUri, String hlpUri) {
        StringBuilder html = new StringBuilder();

        html.append(hlp2HtmlInit(cssUri));

        BufferedReader reader = null;

        try {
            InputStream is = context.getAssets().open(hlpUri);

            reader = new BufferedReader(
                    new InputStreamReader(is));

            String line = reader.readLine();

            while (line != null) {
                html.append(hlp2HtmlNext(line));
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        html.append(hlp2HtmlDone());

        return new String(html);
    }
}
