/*
 * Copyright 2014-2016 Paul Novaes
 */

package com.n3times.android.rcl58.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.res.AssetManager;

/**
 * Assets utilities.
 */
public class Assets {
    /**
     * Reads the contents of an asset file into a String.
     *
     * <p>Returns null in case of error, such as the absence of such file.
     */
    public static String fileAsString(AssetManager assetManager, String path) {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(assetManager.open(path)));

            StringBuffer s = new StringBuffer();
            while (true) {
                String a = in.readLine();
                if (a == null) break;
                s.append(a);
            }

            return new String(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Copies an asset to the file system.
     *
     * @param assetManager the asset manager
     * @param fromAssetPath the asset we want to copy
     * @param toPath where we want to copy the asset in the file system
     * @return true if the copy was successful
     */
    public static boolean copyAsset(AssetManager assetManager,
            String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(fromAssetPath);
            out = new FileOutputStream(toPath);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            out.flush();
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
