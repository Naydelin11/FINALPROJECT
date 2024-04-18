import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.lang3.StringUtils;

public class ChatGPTClient {

    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        SmartTutor tutor = new SmartTutor();
        tutor.start();
    }

    public static double calculateStringSimilarity(String a, String b) {
        return StringUtils.getJaroWinklerDistance(a, b);
    }

    public static CompletableFuture<String> getChatGPTResponseAsync(String prompt) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}]}";
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                } else {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    future.complete(content);
                }
            }
        });
        return future;
    }
}
