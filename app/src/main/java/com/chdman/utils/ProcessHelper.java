package com.chdman.utils;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessHelper {

    static Process process;

    public ProcessHelper(Process p) {
        this.process = p;
    }

    /* Copied from https://stackoverflow.com/questions/13055794/android-runtime-getruntime-exec-get-process-id */

    public static int getPid() {
        int pid = -1;

        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(process);
            f.setAccessible(false);
        } catch (Throwable ignored) {
            try {
                Matcher m = Pattern.compile("pid=(\\d+)").matcher(process.toString());
                pid = m.find() ? Integer.parseInt(m.group(1)) : -1;
            } catch (Throwable ignored2) {
                pid = -1;
            }
        }
        return pid;
    }
}
