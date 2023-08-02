package io.grpc.helloworldexample;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class LogCapturer {
    private Thread captureThread;
    private OutputStream logcatOutput;

    public void startCapture() {
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

    public String[] getCapturedLogs() {
        String buffer = logcatOutput.toString();
        return buffer.split("\n");
    }

    public void stopCapture() {
        if (captureThread != null) {
            captureThread.interrupt();
        }
    }
}
