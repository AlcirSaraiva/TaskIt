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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    // TODO temporary
    private String myID = "4da7-4151-a485-a33a";
    private boolean taskMaster = true;
    private boolean admin = true;

    // General
    String TAG = "LogCat TaskIt: ";
    private Context context;
    private final String fS = "10FXS01";
    private final String newLine = "10NXL01";

    private ArrayList<String> usersNames;
    private ArrayList<String> usersIds;
    private ArrayList<Boolean> usersTaskMaster;
    private ArrayList<Boolean> usersAdmin;

    // UI
    private final int  LOGIN = 0;
    private final int  CHOOSE_ROLE = 1;
    private final int  TASK_MASTER_TASKERS = 2;
    private final int  TASK_MASTER_TASKS = 3;
    private final int  TASKER_TASKS = 4;
    private final int  USER_ADD = 5;
    private final int  TASK_MASTER_NEW_TASK = 6;
    private final int  CHANGE_PASSWORD = 7;
    private int currentScreen;

    private TextView info, addUserIdField;
    private LinearLayout loginCard, taskMasterCard, taskerCard, adminCard, taskMasterTaskersCard, taskMasterTasksCard, taskerTasksCard, addUserCard, changePasswordCard, addTaskCard;
    private ImageButton backButton, taskMasterTaskersCardButton, taskMasterTasksCardButton, taskerTasksCardButton;
    private Button signInButton, addUserCardButton, addUserGenerateIdButton, addUserAddButton, changePassCardButton, addTaskCardButton, changePasswordChangeButton;
    private EditText addUserNameField, changePasswordOldField, changePasswordNew1Field, changePasswordNew2Field;
    private CheckBox addUserTaskMaster, addUserAdmin;
    private Spinner addTaskTaskerSpinner;

    private TextView usersList, theirTasksList, myTasksList;

    // network
    private boolean isOnline;
    private BroadcastReceiver receiver;
    private final String emptyData = "emptyData";
    private final String addUserPHP = "https://www.solvaelys.com/taskit/add_user.php";
    private final String loadUsersPHP = "https://www.solvaelys.com/taskit/load_users.php";
    private final String loadTheirTasksPHP = "https://www.solvaelys.com/taskit/load_their_tasks.php";
    private final String loadMyTasksPHP = "https://www.solvaelys.com/taskit/load_my_tasks.php";
    private final String changePasswordPHP = "https://www.solvaelys.com/taskit/change_password.php";

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

        context = this;

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
        taskMasterCard = findViewById(R.id.task_master_card);
        taskerCard = findViewById(R.id.tasker_card);
        adminCard = findViewById(R.id.admin_card);

        taskMasterTaskersCard = findViewById(R.id.task_master_taskers_card);
        taskMasterTasksCard = findViewById(R.id.task_master_tasks_card);
        taskerTasksCard = findViewById(R.id.tasker_tasks_card);
        addUserCard = findViewById(R.id.add_user_card);
        changePasswordCard = findViewById(R.id.change_password_card);

        signInButton = findViewById(R.id.sign_in_button);

        taskMasterTaskersCardButton = findViewById(R.id.task_master_taskers_card_button);
        taskMasterTasksCardButton = findViewById(R.id.task_master_tasks_card_button);
        addUserCardButton = findViewById(R.id.add_user_card_button);
        changePassCardButton = findViewById(R.id.change_pass_card_button);
        addTaskCardButton = findViewById(R.id.add_task_card_button);

        addUserNameField = findViewById(R.id.add_user_name_field);
        addUserTaskMaster = findViewById(R.id.add_user_task_master);
        addUserAdmin = findViewById(R.id.add_user_admin);
        addUserIdField = findViewById(R.id.add_user_id_field);
        addUserGenerateIdButton = findViewById(R.id.add_user_generate_id_button);
        addUserAddButton = findViewById(R.id.add_user_add_button);

        addTaskCard = findViewById(R.id.add_task_card);
        theirTasksList = findViewById(R.id.their_tasks_list);

        addTaskTaskerSpinner = findViewById(R.id.add_task_tasker_spinner);

        taskerTasksCardButton = findViewById(R.id.tasker_tasks_card_button);
        myTasksList = findViewById(R.id.my_tasks_list);

        usersList = findViewById(R.id.users_list);

        changePasswordOldField = findViewById(R.id.change_password_old_field);
        changePasswordNew1Field = findViewById(R.id.change_password_new1_field);
        changePasswordNew2Field = findViewById(R.id.change_password_new2_field);
        changePasswordChangeButton = findViewById(R.id.change_password_change_button);
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

        addUserCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(USER_ADD);
            }
        });
        changePassCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(CHANGE_PASSWORD);
            }
        });

        addUserGenerateIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserIdField.setText(generateID());
            }
        });
        addUserAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userAdd();
            }
        });

        taskMasterTaskersCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKERS);
                loadUsers();
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

        changePasswordChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }
    
    private void changeScreen(int newScreen) {
        switch (newScreen) {
            case LOGIN:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                loginCard.setVisibility(View.VISIBLE);
                break;
            case CHOOSE_ROLE:
                loginCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.GONE);
                addUserCard.setVisibility(View.GONE);
                changePasswordCard.setVisibility(View.GONE);
                if (taskMaster) taskMasterCard.setVisibility(View.VISIBLE);
                if (admin) {
                    addUserCardButton.setVisibility(View.VISIBLE);
                } else {
                    addUserCardButton.setVisibility(View.GONE);
                }
                taskerCard.setVisibility(View.VISIBLE);
                adminCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKERS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                addTaskCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.VISIBLE);
                break;
            case TASKER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.VISIBLE);
                break;
            case USER_ADD:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                addUserCard.setVisibility(View.VISIBLE);
                addUserNameField.setText("");
                addUserIdField.setText(generateID());
                break;
            case TASK_MASTER_NEW_TASK:
                taskMasterTasksCard.setVisibility(View.GONE);
                addTaskCard.setVisibility(View.VISIBLE);
                buildNewTaskCard();
                break;
            case CHANGE_PASSWORD:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                changePasswordCard.setVisibility(View.VISIBLE);
                changePasswordOldField.setText("");
                changePasswordNew1Field.setText("");
                changePasswordNew2Field.setText("");
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
            case TASK_MASTER_TASKS:
            case TASKER_TASKS:
            case USER_ADD:
            case CHANGE_PASSWORD:
                changeScreen(CHOOSE_ROLE);
                break;
            case TASK_MASTER_NEW_TASK:
                changeScreen(TASK_MASTER_TASKS);
                break;
        }
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

    private void userAdd() {
        int taskMasterCB = 0;
        int adminCB = 0;
        if (addUserTaskMaster.isChecked()) taskMasterCB = 1;
        if (addUserAdmin.isChecked()) adminCB = 1;

        addUserAddButton.setEnabled(false);
        if (!addUserNameField.getText().toString().isEmpty()) {
            String rawData = addUserNameField.getText().toString() + fS + addUserIdField.getText().toString() + fS + taskMasterCB + fS + adminCB;
            String response = contactServer(addUserPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);
            if (response.contains("New record created successfully")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addUserAddButton.setEnabled(true);
                        info.setText("");
                        changeScreen(CHOOSE_ROLE);
                    }
                }, 1500);
            } else if (response.contains("User name already registered")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("");
                        addUserAddButton.setEnabled(true);
                    }
                }, 2000);
            } else if (response.contains("User ID already exists")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("Generate new ID and try again");
                        addUserAddButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            info.setText("ERROR\nName field cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                    addUserAddButton.setEnabled(true);
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

    private void loadUsers() {
        info.setText("Contacting server...  ");
        usersList.setText("");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadUsersPHP, Java_AES_Cipher.encryptSimple(emptyData));
                if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
                    usersNames = new ArrayList<>();
                    usersIds = new ArrayList<>();
                    usersTaskMaster = new ArrayList<>();
                    usersAdmin = new ArrayList<>();

                    String[] lines = response.split(newLine);
                    String[] line;
                    for (int i = 0; i < lines.length; i ++) {
                        line = lines[i].split(fS);
                        if (line.length == 4 && !line[1].equals(myID)) {
                            usersNames.add(line[0]);
                            usersIds.add(line[1]);
                            if (line[2].equals("0")) {
                                usersTaskMaster.add(false);
                            } else {
                                usersTaskMaster.add(true);
                            }
                            if (line[3].equals("0")) {
                                usersAdmin.add(false);
                            } else {
                                usersAdmin.add(true);
                            }
                        }
                    }


                    info.setText("");

                    String[] items = usersNames.toArray(new String[usersNames.size()]);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
                    addTaskTaskerSpinner.setAdapter(adapter);

                    String tempList = "";
                    for (int i = 0; i < usersNames.size(); i ++) {
                        tempList += usersNames.get(i) + "\n";
                    }

                    usersList.setText(tempList);
                }
            }
        }, 100);
    }

    private String generateID() {
        return UUID.randomUUID().toString().substring(9, 28);
    }

    private void changePassword() {
        if (changePasswordNew1Field.getText().toString().isEmpty() && changePasswordNew2Field.getText().toString().isEmpty()) {
            info.setText("New password cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                }
            }, 2000);
        } else if (!changePasswordNew1Field.getText().toString().equals(changePasswordNew2Field.getText().toString())) {
            info.setText("New password fields do not match");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                }
            }, 2000);
        } else if (changePasswordNew1Field.getText().toString().length() < 8) {
            info.setText("New password must have at least 8 characters");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                }
            }, 2500);
        } else {
            changePasswordChangeButton.setEnabled(false);
            String rawData = myID + fS + changePasswordOldField.getText().toString() + fS + changePasswordNew1Field.getText().toString();
            String response = contactServer(changePasswordPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            response = response.replaceAll(fS, " - ");
            info.setText(response);

            if (response.contains("Password changed")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePasswordOldField.setText("");
                        changePasswordNew1Field.setText("");
                        changePasswordNew2Field.setText("");
                        changePasswordChangeButton.setEnabled(true);
                        info.setText("");
                        changeScreen(CHOOSE_ROLE);
                    }
                }, 1500);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePasswordChangeButton.setEnabled(true);
                        info.setText("");
                    }
                }, 1500);
            }
        }
    }

    private void buildNewTaskCard() {
        loadUsers();
    }
}