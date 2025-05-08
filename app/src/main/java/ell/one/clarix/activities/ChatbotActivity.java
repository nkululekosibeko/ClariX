package ell.one.clarix.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ell.one.clarix.models.ChatMessage;
import ell.one.clarix.R;
import ell.one.clarix.data_adapters.ChatAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private EditText userInput;
    private Button sendButton;
    private RecyclerView chatRecycler;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;

    private final String OPENAI_API_KEY = "";
    private final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userInput = findViewById(R.id.chatInput);
        sendButton = findViewById(R.id.sendButton);
        chatRecycler = findViewById(R.id.chatRecycler);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(new ChatMessage(message, true)); // true = user
                userInput.setText("");
                sendMessageToOpenAI(message);
            }
        });
    }

    private void addMessage(ChatMessage chatMessage) {
        chatList.add(chatMessage);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        chatRecycler.scrollToPosition(chatList.size() - 1);
    }

    private void sendMessageToOpenAI(String userMessage) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful AI tutor assistant."));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage));
            json.put("messages", messages);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(OPENAI_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            addMessage(new ChatMessage("Error: " + e.getMessage(), false)));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "null";
                        runOnUiThread(() ->
                                addMessage(new ChatMessage("API Error: " + response.code() + " → " + errorBody, false)));
                        return;
                    }


                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String botReply = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() ->
                                addMessage(new ChatMessage(botReply.trim(), false)));
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                addMessage(new ChatMessage("Failed to parse AI response.", false)));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error forming request", Toast.LENGTH_SHORT).show();
        }
    }
}
