/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.helloworldexample;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import grpc.crud.CrudServiceGrpc;
import grpc.crud.TaskArrayResponse;
import grpc.crud.TaskRequest;
import grpc.crud.TaskResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloworldActivity extends AppCompatActivity {
  private Button sendButton;
  private EditText hostEdit;
  private EditText portEdit;
  private EditText messageEdit;
  private TextView resultText;

  private final String[] crudObjects = {
          "addElement",
          "getElementByID",
          "listingElements",
          "updateElementByID",
          "removeElement"
  };

  private String request;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_helloworld);
    sendButton = (Button) findViewById(R.id.send_button);
    hostEdit = (EditText) findViewById(R.id.host_edit_text);
    portEdit = (EditText) findViewById(R.id.port_edit_text);
    messageEdit = (EditText) findViewById(R.id.message_edit_text);
    resultText = (TextView) findViewById(R.id.grpc_response_text);
    resultText.setMovementMethod(new ScrollingMovementMethod());

    Spinner crudSpinner = findViewById(R.id.crud_spinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, crudObjects);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    crudSpinner.setAdapter(adapter);

    crudSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedCrudObject = parent.getItemAtPosition(position).toString();
        switch (selectedCrudObject) {
          case "addElement":
            request = "addElement";
            break;

          case "getElementByID":
            request = "getElementByID";
            break;

          case "listingElements":
            request = "listingElements";
            break;

          case "updateElementByID":
            request = "updateElementByID";
            break;

          case "removeElement":
            request = "removeElement";
            break;
        }
      }

      public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
      }
    });
  }

  public void sendMessage(View view) {
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
        .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
    sendButton.setEnabled(false);
    resultText.setText("");

    String host = hostEdit.getText().toString();
    String message = messageEdit.getText().toString();
    String portStr = portEdit.getText().toString();

    new GrpcTask(this)
            .execute(host, message, portStr, request); // Pass the selected CRUD object as an additional parameter
  }

  private static class GrpcTask extends AsyncTask<String, Void, String> {
    private final WeakReference<Activity> activityReference;
    private ManagedChannel channel;

    private GrpcTask(Activity activity) {
      this.activityReference = new WeakReference<Activity>(activity);
    }

    private static void buildNameAndID(String message, TaskRequest.Builder requestBuilder) {
      message = message.trim();
      String[] parts = message.split("\\s+", 2);
      int id = Integer.parseInt(parts[0]);
      String name = parts[1];
      requestBuilder.setId(id).setName(name);
    }

    @Override
    protected String doInBackground(String... params) {
      String host = params[0];
      String message = params[1];
      String portStr = params[2];
      String request = params[3]; // Get selected CRUD object

      int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
      try {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        CrudServiceGrpc.CrudServiceBlockingStub stub = CrudServiceGrpc.newBlockingStub(channel);

        TaskRequest.Builder requestBuilder = TaskRequest.newBuilder();
        TaskResponse result;

        // Set the selected CRUD object based on the request
        switch (request) {
          case "addElement": {// "any_id name"
            if (!MessageChecker.isFormat(message, MessageChecker.format_id_name))
              return MessageChecker.getRelativeFailedMsg(MessageChecker.format_id_name);

            buildNameAndID(message, requestBuilder);
            result = stub.addElement(requestBuilder.build());
            break;
          }

          case "getElementByID": { // "id"
            if (!MessageChecker.isFormat(message, MessageChecker.format_id))
              return MessageChecker.getRelativeFailedMsg(MessageChecker.format_id);

            requestBuilder.setId(Integer.parseInt(message.trim()));
            result = stub.getElementByID(requestBuilder.build());
            break;
          }

          case "listingElements": { // ""
            TaskArrayResponse arrayResponse = stub.listingElements(requestBuilder.build());
            List<TaskResponse> elementList = arrayResponse.getArrayList();
            return elementList.toString();
          }

          case "updateElementByID": { // "id new_name"
            if (!MessageChecker.isFormat(message, MessageChecker.format_id_name))
              return MessageChecker.getRelativeFailedMsg(MessageChecker.format_id_name);

            buildNameAndID(message, requestBuilder);
            result = stub.updateElementByID(requestBuilder.build());
            break;
          }

          case "removeElement": { // "id"
            if (!MessageChecker.isFormat(message, MessageChecker.format_id))
              return MessageChecker.getRelativeFailedMsg(MessageChecker.format_id);

            requestBuilder.setId(Integer.parseInt(message));
            result = stub.removeElement(requestBuilder.build());
            break;
          }

          default:
            // Handle unknown request
            return "Unknown request.";
        }

        return result.toString();
      } catch (Exception e) {
        String knownErrMsg = getErrMsg(e);
        if (knownErrMsg != null) return knownErrMsg;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return String.format("Failed: %n%s", sw);
      }
    }

    private static String getErrMsg(Exception e) {
      String err = e.toString();
      if (err.equals(GrpcErrorMessages.MARSHALING_NIL))
        return GrpcErrorMessages.getRelativeFailedMsg(GrpcErrorMessages.MARSHALING_NIL);
      else if (err.equals(GrpcErrorMessages.UNAVAILABLE))
        return GrpcErrorMessages.getRelativeFailedMsg(GrpcErrorMessages.UNAVAILABLE);
      return null;
    }

    @Override
    protected void onPostExecute(String result) {
      try {
        channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      Activity activity = activityReference.get();
      if (activity == null) {
        return;
      }
      TextView resultText = (TextView) activity.findViewById(R.id.grpc_response_text);
      Button sendButton = (Button) activity.findViewById(R.id.send_button);
      resultText.setText(result);
      sendButton.setEnabled(true);
    }
  }
}
