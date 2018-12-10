package finalproject.speechtotext.speechtotext_simplelayout;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private SharedPreferences data;
    private ShareActionProvider shareActionProvider;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private String speakerName = "";
    private TextView voiceOutput;
    private Button setNameButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = getPreferences(MODE_PRIVATE);
        voiceOutput = findViewById(R.id.voiceInput);
        if (!"".equals(data.getString("Transcript", ""))) {
            voiceOutput.setText(data.getString("Transcript", "") + "\n——————\n");
        } else {
            voiceOutput.setText(data.getString("Transcript", ""));
        }
        speakerName = data.getString("Speaker", "");
        ImageButton micButton = findViewById(R.id.btnSpeak);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initVoiceInput();
            }
        });
        setNameButton = findViewById(R.id.leftBtn);
        if (!speakerName.equals("")) {
            setNameButton.setText(speakerName);
        }
        setNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserInput();
            }
        });
        Button clearTextButton = findViewById(R.id.middleBtn);
        clearTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptClearText();
            }
        });
        Button rightButton = findViewById(R.id.rightBtn);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptResetName();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = data.edit()
                .putString("Transcript", voiceOutput.getText().toString())
                .putString("Speaker", speakerName);
        editor.apply();
    }
    private void promptUserInput() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Input your name");
        builder.setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userInput = input.getText().toString();
                        if (!userInput.equals("") && !userInput.trim().equals("")) {
                            speakerName = input.getText().toString().trim();
                            setNameButton.setText(speakerName);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }
    private void promptClearText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Text Deletion")
                .setMessage("Are you sure you wish to clear all text recorded?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        voiceOutput.setText("");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
    private void promptResetName() {
        if (!speakerName.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Confirm Name Reset");
            builder.setMessage("Are you sure you wish to remove the name prefix from future recorded text?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            speakerName = "";
                            setNameButton.setText(R.string.leftBtn);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }
    private void initVoiceInput() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "You may begin to speak.");
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            System.out.println("No applicable activity was found.");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && null != data) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String formatOutput = result.get(0).substring(0, 1) + result.get(0).substring(1);
            if (!voiceOutput.getText().toString().equals("")) {
                voiceOutput.setText(voiceOutput.getText() + "\n");
            }
            if (!speakerName.equals("")) {
                voiceOutput.setText(voiceOutput.getText() + speakerName + ": " + formatOutput + "\n");
            } else {
                voiceOutput.setText(voiceOutput.getText() + formatOutput + "\n");
            }
            shareActionProvider.setShareIntent(createIntent());
        }
    }
    private Intent createIntent() {
        return new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Speech-to-Text Transcript")
                .putExtra(Intent.EXTRA_TEXT, voiceOutput.getText().toString());
    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.share_context_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.shareMenu);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (actionProvider != null) {
            actionProvider.setShareIntent(createIntent());
            shareActionProvider = actionProvider;
        }
        return super.onCreateOptionsMenu(menu);
    }
}