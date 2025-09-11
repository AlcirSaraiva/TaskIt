package com.awesome.taskit;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Activity activityContext;

    private Calendar calendar;
    private int day, month, year, hour, minute;

    private final String fS = "10FXS01";
    private final String newLine = "10NXL01";

    private ArrayList<String> usersNames;
    private ArrayList<String> usersIds;
    private ArrayList<Boolean> usersTaskMaster;
    private ArrayList<Boolean> usersAdmin;

    private ArrayList<String> theirTasksTaskerId;
    private ArrayList<String> theirTasksTitle;
    private ArrayList<String> theirTasksDescription;
    private ArrayList<String> theirTasksDeadline;
    private ArrayList<Boolean> theirTasksTaskerMarkedAsDone;
    private ArrayList<String> theirTasksAttachment1;
    private ArrayList<String> theirTasksAttachment2;
    private ArrayList<String> theirTasksTaskerComment;
    private ArrayList<String> theirTasksTaskMasterComment;
    private ArrayList<Boolean> theirTasksTaskMasterMarkedAsDone;

    private ArrayList<String> myTasksTaskMasterId;
    private ArrayList<String> myTasksTitle;
    private ArrayList<String> myTasksDescription;
    private ArrayList<String> myTasksDeadline;
    private ArrayList<Boolean> myTasksTaskerMarkedAsDone;
    private ArrayList<String> myTasksAttachment1;
    private ArrayList<String> myTasksAttachment2;
    private ArrayList<String> myTasksTaskerComment;
    private ArrayList<String> myTasksTaskMasterComment;
    private ArrayList<Boolean> myTasksTaskMasterMarkedAsDone;

    private boolean justOne;

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

    private TextView info, addUserIdField, addTaskDate, addTaskTime;
    private LinearLayout loginCard, taskMasterCard, taskerCard, adminCard, taskMasterTaskersCard, taskMasterTasksCard, taskerTasksCard, addUserCard, changePasswordCard, addTaskCard;
    private ImageButton backButton, taskMasterTaskersCardButton, taskMasterTasksCardButton, taskerTasksCardButton;
    private Button signInButton, addUserCardButton, addUserGenerateIdButton, addUserAddButton, changePassCardButton, addTaskCardButton, addTaskPickDateButton, addTaskPickTimeButton, addOneTaskButton, addMoreTaskButton, changePasswordChangeButton;
    private EditText addUserNameField, changePasswordOldField, changePasswordNew1Field, changePasswordNew2Field, addTaskTitle, addTaskDescription;
    private CheckBox addUserTaskMaster, addUserAdmin;
    private Spinner addTaskTaskerSpinner;
    private ListView usersListView, theirTasksListView, myTasksListView;

    // network
    private boolean isOnline;
    private BroadcastReceiver receiver;
    private final String emptyData = "emptyData";
    private final String addUserPHP = "https://www.solvaelys.com/taskit/add_user.php";
    private final String loadUsersPHP = "https://www.solvaelys.com/taskit/load_users.php";
    private final String loadTheirTasksPHP = "https://www.solvaelys.com/taskit/load_their_tasks.php";
    private final String loadMyTasksPHP = "https://www.solvaelys.com/taskit/load_my_tasks.php";
    private final String addTaskPHP = "https://www.solvaelys.com/taskit/add_task.php";
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
        activityContext = MainActivity.this;

        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

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
        theirTasksListView = findViewById(R.id.their_tasks_listview);

        addTaskTaskerSpinner = findViewById(R.id.add_task_tasker_spinner);
        addTaskTitle = findViewById(R.id.add_task_title);
        addTaskDescription = findViewById(R.id.add_task_description);
        addTaskPickDateButton = findViewById(R.id.add_task_pick_date_button);
        addTaskDate = findViewById(R.id.add_task_date);
        addTaskPickTimeButton = findViewById(R.id.add_task_pick_time_button);
        addTaskTime = findViewById(R.id.add_task_time);
        addOneTaskButton = findViewById(R.id.add_one_task_button);
        addMoreTaskButton = findViewById(R.id.add_more_task_button);

        taskerTasksCardButton = findViewById(R.id.tasker_tasks_card_button);
        myTasksListView = findViewById(R.id.my_tasks_listview);

        usersListView = findViewById(R.id.users_listview);

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
                addUser();
            }
        });

        taskMasterTaskersCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKERS);
            }
        });

        taskMasterTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKS);
            }
        });

        taskerTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_TASKS);
            }
        });
        addTaskCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_NEW_TASK);
            }
        });

        addTaskPickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dpdYear, int dpdMonthOfYear, int dpdDayOfMonth) {
                        addTaskDate.setText(dpdDayOfMonth + "/" + (dpdMonthOfYear + 1) + "/" + dpdYear);
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, dpdMonthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dpdYear);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
        addTaskPickTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int tpdHourOfDay, int tpdMinute) {
                        addTaskTime.setText(tpdHourOfDay + ":" + tpdMinute);
                        calendar.set(Calendar.HOUR_OF_DAY, tpdHourOfDay);
                        calendar.set(Calendar.MINUTE, tpdMinute);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });
        addOneTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justOne = true;
                addTask();
            }
        });
        addMoreTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justOne = false;
                addTask();
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
                loadUsers();
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
                loadTheirTasks();
                break;
            case TASKER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.VISIBLE);
                loadMyTasks();
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
                if (addTaskDate.getText().toString().isEmpty()) {
                    addTaskDate.setText(day + "/" + (month + 1) + "/" + year);
                }
                if (addTaskTime.getText().toString().isEmpty()) {
                    addTaskTime.setText(hour + ":" + minute);
                }
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

    private void addUser() {
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadTheirTasksPHP, Java_AES_Cipher.encryptSimple(myID));
                if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
                    theirTasksTaskerId = new ArrayList<>();
                    theirTasksTitle = new ArrayList<>();
                    theirTasksDescription = new ArrayList<>();
                    theirTasksDeadline = new ArrayList<>();
                    theirTasksTaskerMarkedAsDone = new ArrayList<>();
                    theirTasksAttachment1 = new ArrayList<>();
                    theirTasksAttachment2 = new ArrayList<>();
                    theirTasksTaskerComment = new ArrayList<>();
                    theirTasksTaskMasterComment = new ArrayList<>();
                    theirTasksTaskMasterMarkedAsDone = new ArrayList<>();

                    String[] lines = response.split(newLine);
                    String[] line;
                    for (int i = 0; i < lines.length; i ++) {
                        line = lines[i].split(fS);
                        if (line.length == 10) {
                            theirTasksTaskerId.add(line[0]);
                            theirTasksTitle.add(line[1]);
                            theirTasksDescription.add(line[2]);
                            theirTasksDeadline.add(line[3]);
                            if (line[4].equals("0")) {
                                theirTasksTaskerMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskerMarkedAsDone.add(true);
                            }
                            theirTasksAttachment1.add(line[5]);
                            theirTasksAttachment2.add(line[6]);
                            theirTasksTaskerComment.add(line[7]);
                            theirTasksTaskMasterComment.add(line[8]);
                            if (line[9].equals("0")) {
                                theirTasksTaskMasterMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskMasterMarkedAsDone.add(true);
                            }
                        }
                    }

                    info.setText("");

                    ArrayList<String> tasksTaskerName = new ArrayList<>();
                    for (int i = 0; i < theirTasksTaskerId.size(); i ++) {
                        tasksTaskerName.add(usersNames.get(usersIds.indexOf(theirTasksTaskerId.get(i))));
                    }

                    TheirTasksListAdapter theirTasksListAdapter = new TheirTasksListAdapter(activityContext, tasksTaskerName, theirTasksTitle, theirTasksDeadline, theirTasksTaskerMarkedAsDone);
                    theirTasksListView.setAdapter(theirTasksListAdapter);
                }
            }
        }, 100);
    }

    private void loadMyTasks() {
        info.setText("Contacting server...  ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String response = contactServer(loadMyTasksPHP, Java_AES_Cipher.encryptSimple(myID));
                if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
                    myTasksTaskMasterId = new ArrayList<>();
                    myTasksTitle = new ArrayList<>();
                    myTasksDescription = new ArrayList<>();
                    myTasksDeadline = new ArrayList<>();
                    myTasksTaskerMarkedAsDone = new ArrayList<>();
                    myTasksAttachment1 = new ArrayList<>();
                    myTasksAttachment2 = new ArrayList<>();
                    myTasksTaskerComment = new ArrayList<>();
                    myTasksTaskMasterComment = new ArrayList<>();
                    myTasksTaskMasterMarkedAsDone = new ArrayList<>();

                    String[] lines = response.split(newLine);
                    String[] line;
                    for (int i = 0; i < lines.length; i ++) {
                        line = lines[i].split(fS);
                        if (line.length == 10) {
                            myTasksTaskMasterId.add(line[0]);
                            myTasksTitle.add(line[1]);
                            myTasksDescription.add(line[2]);
                            myTasksDeadline.add(line[3]);
                            if (line[4].equals("0")) {
                                myTasksTaskerMarkedAsDone.add(false);
                            } else {
                                myTasksTaskerMarkedAsDone.add(true);
                            }
                            myTasksAttachment1.add(line[5]);
                            myTasksAttachment2.add(line[6]);
                            myTasksTaskerComment.add(line[7]);
                            myTasksTaskMasterComment.add(line[8]);
                            if (line[9].equals("0")) {
                                myTasksTaskMasterMarkedAsDone.add(false);
                            } else {
                                myTasksTaskMasterMarkedAsDone.add(true);
                            }
                        }
                    }

                    info.setText("");

                    MyTasksListAdapter MyTasksListAdapter = new MyTasksListAdapter(activityContext, myTasksTitle, myTasksDeadline, myTasksTaskerMarkedAsDone);
                    myTasksListView.setAdapter(MyTasksListAdapter);
                }
            }
        }, 100);
    }

    private void loadUsers() {
        info.setText("Contacting server...  ");
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
                        if (line.length == 4) {// && !line[1].equals(myID)) {
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

                    ArrayList<String> tempUsersNames = new ArrayList<>();
                    ArrayList<String> tempUsersIds = new ArrayList<>();

                    for (int i = 0; i < usersIds.size(); i ++) {
                        if (!usersIds.get(i).equals(myID)) {
                            tempUsersNames.add(usersNames.get(i));
                            tempUsersIds.add(usersIds.get(i));
                        }
                    }

                    String[] items = tempUsersNames.toArray(new String[tempUsersNames.size()]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
                    addTaskTaskerSpinner.setAdapter(adapter);

                    UsersListAdapter usersListAdapter = new UsersListAdapter(activityContext, tempUsersNames, tempUsersIds);
                    usersListView.setAdapter(usersListAdapter);
                }
            }
        }, 100);
    }

    private void addTask() {
        addOneTaskButton.setEnabled(false);
        addMoreTaskButton.setEnabled(false);
        if (!addTaskTitle.getText().toString().isEmpty()) {
            String rawData = myID + fS +                                                                                    // task_master_id
                             usersIds.get(usersNames.indexOf(addTaskTaskerSpinner.getSelectedItem().toString())) + fS +     // tasker_id
                             addTaskTitle.getText().toString() + fS +                                                       // title
                             addTaskDescription.getText().toString() + fS +                                                 // description
                             year + "-" + (month + 1) + "-" + day + " " + hour + ":" + minute + ":00" + fS +                // deadline
                             "0" + fS +                                                                                     // tasker_marked_as_done
                             " " + fS +                                                                                     // attachment_1
                             " " + fS +                                                                                     // attachment_2
                             " " + fS +                                                                                     // tasker_comment
                             " " + fS +                                                                                     // task_master_comment
                             "0";                                                                                           // task_master_marked_as_done

            String response = contactServer(addTaskPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);

            if (response.contains("New task created successfully")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addOneTaskButton.setEnabled(true);
                        addMoreTaskButton.setEnabled(true);
                        info.setText("");
                        addTaskTitle.setText("");
                        addTaskDescription.setText("");
                        if (justOne) changeScreen(TASK_MASTER_TASKS);
                    }
                }, 1500);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addOneTaskButton.setEnabled(true);
                        addMoreTaskButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            info.setText("ERROR\nTitle field cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                    addOneTaskButton.setEnabled(true);
                    addMoreTaskButton.setEnabled(true);
                }
            }, 2000);
        }
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

    public class UsersListAdapter extends ArrayAdapter {
        private Activity activityContext;
        private ArrayList<String> name, id;

        public UsersListAdapter(@NonNull Activity activityContext, ArrayList<String> name, ArrayList<String> id) {
            super(context, R.layout.users_list, name);
            this.activityContext = activityContext;
            this.name = name;
            this.id = id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater inflater = activityContext.getLayoutInflater();
            if (convertView == null) view = inflater.inflate(R.layout.users_list, null, true);

            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);

            text1.setText(name.get(position));
            text2.setText(id.get(position));

            return view;
        }
    }

    public class TheirTasksListAdapter extends ArrayAdapter {
        private Activity activityContext;
        private ArrayList<String> name, title, deadline;
        private ArrayList<Boolean> done;

        public TheirTasksListAdapter(@NonNull Activity activityContext, ArrayList<String> name, ArrayList<String> title, ArrayList<String> deadline, ArrayList<Boolean> done) {
            super(context, R.layout.their_tasks_list, name);
            this.activityContext = activityContext;
            this.name = name;
            this.title = title;
            this.deadline = deadline;
            this.done = done;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater inflater = activityContext.getLayoutInflater();
            if (convertView == null) view = inflater.inflate(R.layout.their_tasks_list, null, true);

            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);
            TextView text3 = (TextView) view.findViewById(R.id.text3);
            CheckBox check = (CheckBox) view.findViewById(R.id.check);

            text1.setText(name.get(position));
            text2.setText(title.get(position));
            text3.setText(deadline.get(position));
            check.setChecked(done.get(position));

            return view;
        }
    }

    public class MyTasksListAdapter extends ArrayAdapter {
        private Activity activityContext;
        private ArrayList<String> title, deadline;
        private ArrayList<Boolean> done;

        public MyTasksListAdapter(@NonNull Activity activityContext, ArrayList<String> title, ArrayList<String> deadline, ArrayList<Boolean> done) {
            super(context, R.layout.my_tasks_list, title);
            this.activityContext = activityContext;
            this.title = title;
            this.deadline = deadline;
            this.done = done;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater inflater = activityContext.getLayoutInflater();
            if (convertView == null) view = inflater.inflate(R.layout.my_tasks_list, null, true);

            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);
            CheckBox check = (CheckBox) view.findViewById(R.id.check);

            text1.setText(title.get(position));
            text2.setText(deadline.get(position));
            check.setChecked(done.get(position));

            return view;
        }
    }
}