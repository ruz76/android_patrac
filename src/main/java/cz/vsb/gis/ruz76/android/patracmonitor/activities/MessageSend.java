package cz.vsb.gis.ruz76.android.patracmonitor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.vsb.gis.ruz76.android.patracmonitor.R;
import cz.vsb.gis.ruz76.android.patracmonitor.adapters.UsersArrayAdapter;
import cz.vsb.gis.ruz76.android.patracmonitor.domain.User;
import cz.vsb.gis.ruz76.android.patracmonitor.helpers.AdapterHelper;

public class MessageSend extends AppCompatActivity {

    private List<User> users = null;
    private List<String> usersNamesList = null;
    private ArrayAdapter<String> arrayAdapter = null;
    private boolean longClick = true;
    static String fileToUploadPath;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_message_send);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        ImageButton imageButtonAttach = findViewById(R.id.imageButtonAttach);
        imageButtonAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDialog();
            }
        });

        ImageButton imageButtonSend = findViewById(R.id.imageButtonSend);
        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        setUpListOfUsers();
        getListOfUsers();

    }

    private void getListOfUsers() {
        AsyncHttpClient client = new AsyncHttpClient();
        String searchId = sharedPrefs.getString("searchId", "");
        client.get("http://gisak.vsb.cz/patrac/loc.php?searchid=" + searchId, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                String result = new String(bytes);
                String[] lines = result.split("\n");
                for (int lineid = 1; lineid < lines.length; lineid++) {
                    String[] items = lines[lineid].split(";");
                    if (items.length >= 4) {
                        User user = new User(items[0], items[3], false);
                        users.add(1, user);
                        usersNamesList.add(1, items[3]);
                        new AdapterHelper().update((ArrayAdapter) arrayAdapter, new ArrayList<Object>(usersNamesList));
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                Toast toast = Toast.makeText(MessageSend.this, getString(R.string.no_users), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void showFileDialog() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(MessageSend.this, properties);
        dialog.setTitle(getString(R.string.message_attachment_select));
        dialog.show();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                fileToUploadPath = files[0];
                File file = new File(fileToUploadPath);
                long length = file.length();
                if (length == 0) {
                    Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_attachment) + " " + getString(R.string.message_attachment_can_not_be_zero_length), Toast.LENGTH_LONG);
                    toast.show();
                    fileToUploadPath = null;
                    return;
                }
                if (length > 10_000_000L) {
                    Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_attachment) + " " + getString(R.string.message_attachment_is_too_big), Toast.LENGTH_LONG);
                    toast.show();
                    fileToUploadPath = null;
                    return;
                }
                Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_attachment) + " " + getString(R.string.message_attachment_was_append), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void sendMessage() {
        String searchId = sharedPrefs.getString("searchId", "");

        if (searchId.isEmpty()) {
            Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_not_connected), Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        String url = "http://gisak.vsb.cz/patrac/message.php";
        RequestParams params = new RequestParams();
        String ids = "";
        for (int counter = 0; counter < users.size(); counter++) {
            User user = users.get(counter);
            if (user.isSelected()) {
                if (counter == 0) {
                    ids += user.getId();
                } else {
                    ids += ";" + user.getId();
                }
            }
        }

        if (ids.isEmpty()) {
            Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_no_recipient), Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        try {
            String sessionid = sharedPrefs.getString("sessionid", "");
            params.put("operation", "insertmessages");
            params.put("searchid", searchId);
            params.put("from_id", sessionid);
            EditText editTextMessage = (EditText) findViewById(R.id.editTextMessage);
            params.put("message", editTextMessage.getText());
            params.put("ids", ids);
            if (fileToUploadPath != null) {
                File fileToUpload = new File(fileToUploadPath);
                params.put("fileToUpload", fileToUpload);
                Toast toast = Toast.makeText(MessageSend.this, getString(R.string.sending_message) , Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (FileNotFoundException e) {
            Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message_attachment_not_found), Toast.LENGTH_LONG);
            toast.show();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                // handle success response
                String message = getString(R.string.message) + " " + getString(R.string.message_was_sent);
                if (new String(bytes).startsWith("Sorry")) {
                    message = getString(R.string.message_attachment_was_not_uploaded);
                }
                Toast toast = Toast.makeText(MessageSend.this, message, Toast.LENGTH_LONG);
                toast.show();
                fileToUploadPath = null;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                Toast toast = Toast.makeText(MessageSend.this, getString(R.string.message) + " " + getString(R.string.message_was_not_sent), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void setUpListOfUsers() {

        // TODO
        //https://medium.com/mindorks/custom-array-adapters-made-easy-b6c4930560dd

        String searchId = sharedPrefs.getString("searchId", "");
        users = new ArrayList<>();
        User u = new User("coordinator" + searchId, "Štáb", false);
        users.add(u);

        usersNamesList = new ArrayList<>();
        usersNamesList.add(getString(R.string.coordinator));

        // Create an ArrayAdapter from List
        //arrayAdapter = new ArrayAdapter<String>
        //        (this, android.R.layout.simple_list_item_1, usersNamesList);

        arrayAdapter = new UsersArrayAdapter(this, android.R.layout.simple_list_item_1, users, usersNamesList);

        // DataBind ListView with items from ArrayAdapter
        final ListView listViewUsers = (ListView) findViewById(R.id.listViewUsers);
        listViewUsers.setAdapter(arrayAdapter);

        listViewUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (users.get(position).isSelected()) {
                    users.get(position).setSelected(false);
                    listViewUsers.setItemChecked(position, false);
                    view.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    users.get(position).setSelected(true);
                    listViewUsers.setItemChecked(position, true);
                    view.setBackgroundColor(Color.LTGRAY);
                }
                EditText editTextMessage = (EditText) findViewById(R.id.editTextMessage);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);
                //editTextMessage.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        listViewUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (User user : users) {
                    user.setSelected(longClick);
                }
                longClick = !longClick;
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });

    }
}
