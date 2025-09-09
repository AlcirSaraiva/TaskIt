package com.awesome.taskit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    // TODO temporary
    private final String myID = "0ab3-4120-b7ed-2e30";

    // General
    String TAG = "LogCat TaskIt: ";
    private final String fS = "10FXS01";
    private final String newLine = "10NXL01";

    // UI
    private final int  LOGIN = 0;
    private final int  CHOOSE_ROLE = 1;
    private final int  TASK_MASTER_TASKERS = 2;
    private final int  TASK_MASTER_TASKS = 3;
    private final int  TASKER_TASKS = 4;
    private final int  TASK_MASTER_ADD = 5;
    private final int  TASKER_ADD = 6;
    private final int  TASK_MASTER_NEW_TASK = 7;
    private int currentScreen;

    private TextView info, addTaskerIdField, addTaskMasterIdField;
    private LinearLayout loginCard, addTaskMasterCard, taskMasterCard, taskerCard, addTaskerCard, taskMasterTaskersCard, taskMasterTasksCard, taskerTasksCard, addTaskCard;
    private ImageButton backButton, taskMasterTaskersCardButton, taskMasterTasksCardButton, taskerTasksCardButton;
    private Button signInButton, addTaskMasterCardButton, addTaskerCardButton, addTaskerGenerateIdButton, addTaskerAddButton, addTaskMasterGenerateIdButton, addTaskMasterAddButton, addTaskCardButton;
    private EditText addTaskerNameField, addTaskMasterNameField;

    private TextView taskersList, theirTasksList, myTasksList;

    // network
    private boolean isOnline;
    private BroadcastReceiver receiver;
    private final String emptyData = "emptyData";
    private final String addTaskMasterPHP = "https://www.solvaelys.com/taskit/add_task_master.php";
    private final String addTaskerPHP = "https://www.solvaelys.com/taskit/add_tasker.php";
    private final String loadTaskersPHP = "https://www.solvaelys.com/taskit/load_taskers.php";
    private final String loadTheirTasksPHP = "https://www.solvaelys.com/taskit/load_their_tasks.php";
    private final String loadMyTasksPHP = "https://www.solvaelys.com/taskit/load_my_tasks.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        assignViews();
        assignViewListeners();

        prepareNetwork();

        changeScreen(LOGIN);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backButtonPressed();
            }
        });
    }
    
    // UI

    private void assignViews() {
        backButton = findViewById(R.id.back_button);
        info = findViewById(R.id.info);

        loginCard = findViewById(R.id.login_card);
        addTaskMasterCard = findViewById(R.id.add_task_master_card);
        taskMasterCard = findViewById(R.id.task_master_card);
        taskerCard = findViewById(R.id.tasker_card);
        taskMasterTaskersCard = findViewById(R.id.task_master_taskers_card);
        addTaskerCard = findViewById(R.id.add_tasker_card);
        taskMasterTasksCard = findViewById(R.id.task_master_tasks_card);
        taskerTasksCard = findViewById(R.id.tasker_tasks_card);

        signInButton = findViewById(R.id.sign_in_button);
        addTaskMasterCardButton = findViewById(R.id.add_task_master_card_button);

        addTaskMasterNameField = findViewById(R.id.add_task_master_name_field);
        addTaskMasterIdField = findViewById(R.id.add_task_master_id_field);
        addTaskMasterGenerateIdButton = findViewById(R.id.add_task_master_generate_id_button);
        addTaskMasterAddButton = findViewById(R.id.add_task_master_add_button);

        taskMasterTaskersCardButton = findViewById(R.id.task_master_taskers_card_button);
        addTaskerCardButton = findViewById(R.id.add_tasker_card_button);

        taskMasterTasksCardButton = findViewById(R.id.task_master_tasks_card_button);
        addTaskCardButton = findViewById(R.id.add_task_card_button);
        theirTasksList = findViewById(R.id.their_tasks_list);
        addTaskCard = findViewById(R.id.add_task_card);

        taskerTasksCardButton = findViewById(R.id.tasker_tasks_card_button);
        myTasksList = findViewById(R.id.my_tasks_list);

        taskersList = findViewById(R.id.taskers_list);

        addTaskerNameField = findViewById(R.id.add_tasker_name_field);
        addTaskerIdField = findViewById(R.id.add_tasker_id_field);
        addTaskerGenerateIdButton = findViewById(R.id.add_tasker_generate_id_button);
        addTaskerAddButton = findViewById(R.id.add_tasker_add_button);
    }

    private void assignViewListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonPressed();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(CHOOSE_ROLE);
            }
        });
        addTaskMasterCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_ADD);
            }
        });

        addTaskMasterGenerateIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskMasterIdField.setText(generateID());
            }
        });
        addTaskMasterAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskMasterAdd();
            }
        });

        taskMasterTaskersCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKERS);
                loadTaskers();
            }
        });
        addTaskerCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_ADD);
            }
        });

        taskMasterTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKS);
                loadTheirTasks();
            }
        });

        taskerTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_TASKS);
                loadMyTasks();
            }
        });
        addTaskCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_NEW_TASK);
            }
        });

        addTaskerGenerateIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskerIdField.setText(generateID());
            }
        });
        addTaskerAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskerAdd();
            }
        });
    }
    
    private void changeScreen(int newScreen) {
        switch (newScreen) {
            case LOGIN:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                addTaskMasterCard.setVisibility(View.GONE);
                loginCard.setVisibility(View.VISIBLE);
                break;
            case CHOOSE_ROLE:
                loginCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.GONE);
                taskMasterCard.setVisibility(View.VISIBLE);
                taskerCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKERS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                addTaskerCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                addTaskCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.VISIBLE);
                break;
            case TASKER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_ADD:
                loginCard.setVisibility(View.GONE);
                addTaskMasterCard.setVisibility(View.VISIBLE);
                addTaskMasterNameField.setText("");
                addTaskMasterIdField.setText(generateID());
                break;
            case TASKER_ADD:
                taskMasterTaskersCard.setVisibility(View.GONE);
                addTaskerCard.setVisibility(View.VISIBLE);
                addTaskerNameField.setText("");
                addTaskerIdField.setText(generateID());
                break;
            case TASK_MASTER_NEW_TASK:
                taskMasterTasksCard.setVisibility(View.GONE);
                addTaskCard.setVisibility(View.VISIBLE);
                break;
        }
        currentScreen = newScreen;
    }

    private void backButtonPressed() {
        switch (currentScreen) {
            case LOGIN:
                finish();
                break;
            case CHOOSE_ROLE:
                changeScreen(LOGIN);
                break;
            case TASK_MASTER_TASKERS:
                changeScreen(CHOOSE_ROLE);
                break;
            case TASK_MASTER_TASKS:
                changeScreen(CHOOSE_ROLE);
                break;
            case TASKER_TASKS:
                changeScreen(CHOOSE_ROLE);
                break;
            case TASK_MASTER_ADD:
                changeScreen(LOGIN);
                break;
            case TASKER_ADD:
                changeScreen(TASK_MASTER_TASKERS);
                break;
            case TASK_MASTER_NEW_TASK:
                changeScreen(TASK_MASTER_TASKS);
                break;
        }
    }

    private String getCurrentScreen() {
        switch (currentScreen) {
            case LOGIN:
                return "LOGIN";
            case CHOOSE_ROLE:
                return "CHOOSE_ROLE";
            case TASK_MASTER_TASKERS:
                return "TASK_MASTER_TASKERS";
            case TASK_MASTER_TASKS:
                return "TASK_MASTER_TASKS";
            case TASKER_TASKS:
                return "TASKER_TASKS";
            case TASK_MASTER_ADD:
                return "TASK_MASTER_ADD";
            case TASKER_ADD:
                return "TASKER_ADD";
            case TASK_MASTER_NEW_TASK:
                return "TASK_MASTER_NEW_TASK";
        }
        return "NONE";
    }

    // network

    private void prepareNetwork() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            System.out.println(TAG + "{prepareNetwork} " + e.getMessage());
        }
        isOnline = isNetworkAvailable();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context cxt, Intent intent) {
                isOnline = isNetworkAvailable();
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private String contactServer(String phpAddress, String rawData) {
        String response = "";
        try {
            URL url = new URL(phpAddress);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            rawData = rawData.replace("'", "''");

            String data = URLEncoder.encode("rawdata", "UTF-8") + "=" + URLEncoder.encode(rawData, "UTF-8");

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            //System.out.println(TAG + responseCode);
            String line;
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                response += line + "\n";
            }
            inputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            response = e.getMessage();
        } finally {

        }
        return response;
    }

    // general

    private void taskerAdd() {
        addTaskerAddButton.setEnabled(false);
        if (!addTaskerNameField.getText().toString().isEmpty()) {
            String rawData = addTaskerNameField.getText().toString() + fS + addTaskerIdField.getText().toString();
            String response = contactServer(addTaskerPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);
            if (response.contains("New record created successfully")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTaskerAddButton.setEnabled(true);
                        info.setText("");
                        changeScreen(TASK_MASTER_TASKERS);
                        loadTaskers();
                    }
                }, 1500);
            } else if (response.contains("Tasker name already registered")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("");
                        addTaskerAddButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            info.setText("ERROR\nName field cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                    addTaskerAddButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void taskMasterAdd() {
        addTaskMasterAddButton.setEnabled(false);
        if (!addTaskMasterNameField.getText().toString().isEmpty()) {
            String rawData = addTaskMasterNameField.getText().toString() + fS + addTaskMasterIdField.getText().toString();
            String response = contactServer(addTaskMasterPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);
            if (response.contains("New record created successfully")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTaskMasterAddButton.setEnabled(true);
                        info.setText("");
                        changeScreen(LOGIN);
                    }
                }, 1500);
            } else if (response.contains("Task Master name already registered")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("");
                        addTaskMasterAddButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            info.setText("ERROR\nName field cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                    addTaskMasterAddButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void loadTheirTasks() {
        info.setText("Contacting server...  ");
        theirTasksList.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadTheirTasksPHP, Java_AES_Cipher.encryptSimple(myID));
                response = response.replaceAll(newLine, "\n");
                response = response.replaceAll(fS, " - ");
                theirTasksList.setText(response);
                info.setText("");
            }
        }, 100);
    }

    private void loadMyTasks() {
        info.setText("Contacting server...  ");
        myTasksList.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadMyTasksPHP, Java_AES_Cipher.encryptSimple(myID));
                response = response.replaceAll(newLine, "\n");
                response = response.replaceAll(fS, " - ");
                myTasksList.setText(response);
                info.setText("");
            }
        }, 100);
    }

    private void loadTaskers() {
        info.setText("Contacting server...  ");
        taskersList.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadTaskersPHP, Java_AES_Cipher.encryptSimple(emptyData));
                response = response.replaceAll(newLine, "\n");
                response = response.replaceAll(fS, " - ");
                taskersList.setText(response);
                info.setText("");
            }
        }, 100);
    }

    private String generateID() {
        return UUID.randomUUID().toString().substring(9, 28);
    }
}