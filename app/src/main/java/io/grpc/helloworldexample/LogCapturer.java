package io.grpc.helloworldexample;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LogCapturer {
    private Thread captureThread;
    private OutputStream logcatOutput;
    private Boolean saveBuffered = true;

    private List<String> buffer;

    public void startCapture(Boolean saveToBuf) {
        this.saveBuffered = saveToBuf;
        startCapture();
    }
    public void startCapture() {
        if (saveBuffered)
            buffer = new ArrayList<String>();

        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("logcat");
                    InputStream inputStream = process.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    logcatOutput = new ByteArrayOutputStream();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        logcatOutput.write(line.getBytes());
                        logcatOutput.write(System.lineSeparator().getBytes());

                        if (saveBuffered) buffer.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        captureThread.start();
    }

    public OutputStream getLogcatOutput() {
        return logcatOutput;
    }

    public List<String> getLogcatOutputBuffered() {
        return buffer;
    }

    public String[] getCapturedLogs() {
        if (logcatOutput == null) {
            return new String[0];
        }

        if (saveBuffered) {
            return buffer.toArray(new String[buffer.size()]);
        }

        String buffer = logcatOutput.toString();
        return buffer.split("\n");
    }

    public void stopCapture() {
        if (captureThread != null) {
            captureThread.interrupt();
        }
    }
}
