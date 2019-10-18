package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.dao.StorageDao;
import cz.vsb.gis.ruz76.android.patracmonitor.domain.Message;

public class Messages extends AppCompatActivity {

    private ArrayAdapter<String> arrayAdapter;
    private ListView listViewMessages;
    public static List<String> messages = new ArrayList<>();
    private ArrayList<Integer> messagesIds = new ArrayList<>();
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        listViewMessages = (ListView) findViewById(R.id.listViewMessages);
        setMessagesAdapter();
        loadMessages();

        FloatingActionButton fab = findViewById(R.id.floatingActionButtonSendMessage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageSend = new Intent(Messages.this, MessageSend.class);
                startActivity(messageSend);
            }
        });

    }

    private void loadMessages() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        loadMessagesFromLocal();
        loadMessagesFromServer();
        arrayAdapter.notifyDataSetChanged();
    }

    private void loadMessagesFromLocal() {
        messages.clear();
        //Log.i("loadMessagesFromLocal", "XXXXXXX" + sharedPrefs.getString("searchId", ""));
        ArrayList<Message> messagesFromLocal = StorageDao.getMessages(getApplicationContext(), sharedPrefs.getString("searchId", ""));
        for (Message message : messagesFromLocal) {
            messages.add(message.toString());
            messagesIds.add(message.getMessageId());
        }
    }

    private void loadMessagesFromServer() {
        String sessionId = sharedPrefs.getString("sessionid", "");
        int lastReceivedMessageId = sharedPrefs.getInt("lastReceivedMessageId", 0);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://gisak.vsb.cz/patrac/message.php?operation=getmessages&lastereceivedmessageid="
                + lastReceivedMessageId
                + "&sessionid=" + sessionId,
                new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                processResponse(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                if (bytes != null) {
                    Log.e("RESPONSE FAILURE", statusCode + ": " + new String(bytes));
                } else {
                    Log.e("RESPONSE FAILURE", "null");
                }
            }
        });
    }

    public void processResponse(String result) {
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(new String(result));
                int messagesCount = jsonObj.getJSONArray("messages").length();
                JSONArray messagesFromServer = jsonObj.getJSONArray("messages");
                int lastMessageId = 0;
                for (int i = 0; i < messagesCount; i++) {
                    Message message = new Message(messagesFromServer.getJSONObject(i));
                    lastMessageId = message.getMessageId();
                    StorageDao.saveReceivedMessageToDB(message, getApplicationContext());
                    messages.add(message.toString());
                    messagesIds.add(message.getMessageId());
                }
                if (messagesCount > 0) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt("lastReceivedMessageId", lastMessageId);
                    editor.commit();
                    arrayAdapter.notifyDataSetChanged();
                }
                Log.i("MMC", String.valueOf(messagesCount));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException exception) {
                // The messages view was killed. No problem, but we need to catch the exception.
            }
        }
    }

    private void setMessagesAdapter() {
        arrayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, messages);

        // DataBind ListView with items from ArrayAdapter
        listViewMessages.setAdapter(arrayAdapter);

        listViewMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent messageView = new Intent(Messages.this, MessageView.class);
                messageView.putExtra("messageid", messagesIds.get(position));
                startActivity(messageView);
            }

        });
    }
}
