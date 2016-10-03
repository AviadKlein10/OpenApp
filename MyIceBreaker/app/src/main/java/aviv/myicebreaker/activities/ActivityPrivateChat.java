package aviv.myicebreaker.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import aviv.myicebreaker.R;
import aviv.myicebreaker.Singleton;
import aviv.myicebreaker.module.Adapters.BubbleChatAdapter;
import aviv.myicebreaker.module.BubbleType;
import aviv.myicebreaker.module.CustomObjects.BubbleChatObject;
import aviv.myicebreaker.module.CustomObjects.NewUser;
import aviv.myicebreaker.module.JsonParser;

/**
 * Created by Aviad on 16/09/2016.
 */
public class ActivityPrivateChat extends AppCompatActivity {

    private static final String CONVERSATION_KEY = "conversationKey";
    private EditText mInputMessageView;
    private ImageView btnSendMessage;
    private Socket mSocket;
    private boolean mTyping = false;
    private JSONObject chatIdJson;
    private ListView listChat;
    private ArrayList<BubbleChatObject> arrayListBubbleChat;
    private BubbleChatAdapter bubbleChatAdapter;

    {
        try {
            mSocket = IO.socket("http://79.181.246.70:8082");
        } catch (URISyntaxException e) {
            Log.e("exception socket", e + "");
        }
    }


    private Emitter.Listener hello = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("data msg ", Arrays.toString(args) + "!");

                }
            });
        }
    };
    private Emitter.Listener sendMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("data msg2 ", data + "!");
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e("exception ", e + " ");
                        return;
                    }

                    // add the message to view
                    addMessage(username, message);
                }
            });
        }
    };
    private Emitter.Listener incomingMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("incomingMessage ", data + "!");

                }
            });
        }
    };

    private Emitter.Listener socketError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("socketError ", data + "!");

                }
            });
        }
    };
    private Emitter.Listener typing = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("typing  ", data + "!");

                }
            });
        }
    };
    private Emitter.Listener stoppedTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("stopped typing ", data + "!");

                }
            });
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_private_chat);
        String chatId = "{'chatId':'57c914fd3288e95b0ab2abb2'}";
        try {
            chatIdJson = new JSONObject(chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initSocketListeners();
        initViews();
        initActionBar();
      //  createChat();
        loadConversationSharedPreferences();
        bubbleChatAdapter = new BubbleChatAdapter(this, arrayListBubbleChat);
        listChat.setAdapter(bubbleChatAdapter);
bubbleChatAdapter.notifyDataSetChanged();

        Log.d("after", "connect");

    }

    private void createChat() {
        Log.d("createChat ", "connect");

        arrayListBubbleChat = new ArrayList<>();

        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.OTHER_SIDE_USER, "היי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.LOCAL_USER, "היי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.LOCAL_USER, "היי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.LOCAL_USER, "היdscdscdscי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.QUESTION, "זה עם הפיצה", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.OTHER_SIDE_USER, "היי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.OTHER_SIDE_USER, "אללהוואלה באלה גאלה טאלה שמאלה שאלה בלבלה", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.OTHER_SIDE_USER, "היי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.OTHER_SIDE_USER, "היdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.LOCAL_USER, "היdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמיdscdscdscי מה נשמע?", "13:33", 0));
        arrayListBubbleChat.add(new BubbleChatObject(BubbleType.QUESTION, "זה עם הפיצה", "13:33", 0));

        bubbleChatAdapter = new BubbleChatAdapter(this, arrayListBubbleChat);
        listChat.setAdapter(bubbleChatAdapter);

    }

    private void initViews() {
        mInputMessageView = (EditText) findViewById(R.id.inputMessage);
        mInputMessageViewOnTypingListener();
        listChat = (ListView) findViewById(R.id.listChat);

        btnSendMessage = (ImageView) findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    attemptSend();
                } catch (JSONException e) {
                    Log.e("whylikethis", e.toString());
                }
            }
        });
    }

    private void initSocketListeners() {
        mSocket.on("hello", hello);
        mSocket.on("socketError", socketError);
        mSocket.on("sendMessage", sendMessage);
        mSocket.on("typing", typing);
        mSocket.on("stoppedTyping", stoppedTyping);

        mSocket.on("incomingMessage", incomingMessage);
        mSocket.connect();
        mSocket.emit("joinChat", chatIdJson);
    }

    private void mInputMessageViewOnTypingListener() {
        Log.d("typingListener, ", "on");
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //   if (null == mUsername) return;
                //   if (!mSocket.connected()) return;

                String typingInfo = "{'chatId':'57c914fd3288e95b0ab2abb2','senderId':'57b726b26470e2cb10896ac1'}";

                JSONObject typingJson = null;
                try {
                    typingJson = new JSONObject(typingInfo);
                    if (mInputMessageView.length() > 0 && !mTyping) {
                        Log.d("yourTyping", "true");
                        mSocket.emit("typingEvent", typingJson);
                        mTyping = true;
                    }
                    if (mInputMessageView.length() == 0) {
                        stoppedTypingMethod();
                        mTyping = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //   mTypingHandler.removeCallbacks(onTypingTimeout);
                //   mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void addMessage(String username, String message) {
arrayListBubbleChat.add(new BubbleChatObject(BubbleType.LOCAL_USER,message,receiveCurrentTime(),0));
        bubbleChatAdapter.notifyDataSetChanged();
        scrollToBottom();
        Log.d("username: ", username + " message: " + message);
    }


    private void attemptSend() throws JSONException {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        } else if (mInputMessageView.length() > 0) {
            addMessage("425435243", message);

            stoppedTypingMethod();
        }

        String messageInfo = "{'chatId':'57c914fd3288e95b0ab2abb2','message':'" + mInputMessageView.getText().toString().trim() + "','senderId':'57b726b26470e2cb10896ac1','receiverId':'57bc5bd59d71fb922f3e9b3f'}";
        mInputMessageView.setText("");

        Log.d("chatIdJson", chatIdJson.toString());
        JSONObject messageInfoJson = new JSONObject(messageInfo);
        Log.d("messageInfoJson", messageInfo);

        //  mSocket.emit("new message", message);

        mSocket.emit("sendMessage", messageInfoJson);
    }

    private void stoppedTypingMethod() {
        mTyping = false;
        String typingInfo = "{'chatId':'57c914fd3288e95b0ab2abb2','senderId':'57b726b26470e2cb10896ac1'}";

        JSONObject typingJson = null;
        try {
            typingJson = new JSONObject(typingInfo);
            mSocket.emit("stopTypingEvent", typingJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("socket ", "disconnected");
        mSocket.emit("leaveChat", chatIdJson);

        mSocket.disconnect();
        // mSocket.off("new message", onNewMessage);
    }


    @Override
    public void onStop() {

        Log.d("socket for2", "leave");
        mSocket.emit("leaveChat", chatIdJson);

        mSocket.disconnect();
        saveConversationSharedPreferences(arrayListBubbleChat);
        //   mSocket.off("new message", onNewMessage);
        super.onStop();
    }

    private void initActionBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//TODO change status bar lower than 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            //TODO change color status bar
        }
    }
    private void scrollToBottom() {
        listChat.smoothScrollToPosition(bubbleChatAdapter.getCount()-1);
    }

    private void saveConversationSharedPreferences(ArrayList<BubbleChatObject> arrConversation) {
        if(arrConversation!=null) {
            SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            String strConversation = JsonParser.parseConversationToJson(arrConversation);
            editor.putString(CONVERSATION_KEY, strConversation);
            Log.d("save Conversation ", strConversation);
            editor.apply();
        }
    }

    private void loadConversationSharedPreferences() {
        Log.d("inside load", " well");

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String restoredConversation = sharedPreferences.getString(CONVERSATION_KEY, "");

        if (restoredConversation.length() > 0) {

            Log.d("restoredConve", restoredConversation + " z");
            arrayListBubbleChat = JsonParser.parseConversationToObject(restoredConversation);


            Log.d("run run", arrayListBubbleChat.get(3).getMessageContent());




        }

    }
    private String receiveCurrentTime(){
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        return simpleDateFormat.format(calender.getTime());
    }
}
