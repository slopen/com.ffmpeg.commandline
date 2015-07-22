package com.ffmpeg.commandline;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.ffmpeg.commandline.ShellUtils.ShellCallback;
import com.ffmpeg.commandline.FFMPEGWrapper;

import android.os.Environment;
import android.content.Context;
import android.util.Log;

public class FFMPEGCommandline extends CordovaPlugin {

    private CallbackContext cbContext = null;

    @Override
    public boolean execute(
        String action,
        final JSONArray command,
        final CallbackContext callbackContext
    ) throws JSONException {

        this.cbContext = callbackContext;

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);

        try {
            final FFMPEGWrapper ffmpeg;
            final ShellUtils.ShellCallback sc = new ShellUtils.ShellCallback ()
            {
                @Override
                public void shellOut(char[] shellout) {
                    String line = new String(shellout);
                    sendUpdate(line);
                }
            };

            List<String> list = new ArrayList<String>();
            for(int i = 0; i < command.length(); i++){
                list.add(command.getString(i));
            }
            final String[] commandArray = list.toArray(new String[list.size()]);

            ffmpeg = new FFMPEGWrapper(cordova.getActivity().getApplicationContext());

            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try{

                        String[] compiledCommand = ffmpeg.processVideo(commandArray, sc);
                        sendUpdate(Arrays.toString(compiledCommand));

                    } catch (Exception e) {

                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();

                        callbackContext.error(exceptionAsString);
                    }
                }
            });

            return true;

        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

            callbackContext.error(exceptionAsString);
        }

        return false;
    }

    private void sendUpdate(String data) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
        result.setKeepCallback(true);
        this.cbContext.sendPluginResult(result);
    }

}
