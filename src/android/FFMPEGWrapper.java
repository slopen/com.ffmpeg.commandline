package com.ffmpeg.commandline;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.ffmpeg.commandline.ShellUtils.ShellCallback;
import com.ffmpeg.commandline.BinaryInstaller;

import android.content.Context;
import android.util.Log;

import java.util.*;
import java.io.*;
import java.util.regex.*;

class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    OutputStream os;
    ShellCallback sc;
    Process process;

    StreamGobbler(Process process, ShellCallback sc, InputStream is, String type)
    {
        this(process, sc, is, type, null);
    }
    StreamGobbler(Process process, ShellCallback sc, InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        this.os = redirect;
        this.sc = sc;
        this.process = process;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            char[] chars = new char[4*1024];
            int len;

            while((len = isr.read(chars))>=0) {
                if (sc != null)
                    sc.shellOut(chars);
            }
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            process.destroy();
        }
    }
}

public class FFMPEGWrapper {

    String[] libraryAssets = {"ffmpeg"};
    File fileBinDir;
    Context context;

    public FFMPEGWrapper(Context _context) throws FileNotFoundException, IOException {
        context = _context;
        fileBinDir = context.getDir("bin",0);

        if (!new File(fileBinDir,libraryAssets[0]).exists())
        {
            BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
            bi.installFromRaw();
        }
    }



    private void execProcess(String[] cmds, ShellCallback sc) throws Exception {

            sc.shellOut("starting ffmpeg...".toCharArray());

            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.redirectErrorStream(true);
            Process process = pb.start();


            StreamGobbler errorGobbler = new
                StreamGobbler(process, sc, process.getErrorStream(), "ERROR");

            StreamGobbler outputGobbler = new
                StreamGobbler(process, sc, process.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();
    }

    public String[] processVideo(
        String[] command,
        ShellCallback sc
    ) throws Exception {

        String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
        // String[] ffmpegCommand = {"nice", "-n", "19", ffmpegBin};
        String[] ffmpegCommand = {ffmpegBin};

        ArrayList<String> temp = new ArrayList<String>();
        temp.addAll(Arrays.asList(ffmpegCommand));
        temp.addAll(Arrays.asList(command));
        String[] concatedArgs = temp.toArray(new String[ffmpegCommand.length+command.length]);

        execProcess(concatedArgs, sc);
        return concatedArgs;

        // execProcess(command, sc);
        // return command;
    }

    class FileMover {

        InputStream inputStream;
        File destination;

        public FileMover(InputStream _inputStream, File _destination) {
            inputStream = _inputStream;
            destination = _destination;
        }

        public void moveIt() throws IOException {

            OutputStream destinationOut = new BufferedOutputStream(new FileOutputStream(destination));

            int numRead;
            byte[] buf = new byte[1024];
            while ((numRead = inputStream.read(buf) ) >= 0) {
                destinationOut.write(buf, 0, numRead);
            }

            destinationOut.flush();
            destinationOut.close();
        }
    }

}