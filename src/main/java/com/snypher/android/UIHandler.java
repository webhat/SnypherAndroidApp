/*
 * Copyright (c) 2013 Daniël W. Crompton info+snypher@specialbrands.net, Snypher
 *
 *                 This program is distributed in the hope that it will be useful,
 *                 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.snypher.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * @author webhat
 * @version 1.1
 */
final class UIHandler extends Handler {
    public static final int DISPLAY_UI_TOAST = 0;
    public static final int DISPLAY_UI_DIALOG = 1;
    private Snypher snypher;

    public UIHandler(Snypher snypher, Looper looper) {
        super(looper);
        this.snypher = snypher;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UIHandler.DISPLAY_UI_TOAST: {
                Context context = snypher.getApplicationContext();
                Toast t = Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG);
                t.show();
            }
            case UIHandler.DISPLAY_UI_DIALOG:
                //TBD
            default:
                break;
        }
    }
}
