package com.awesome.taskit;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import coil3.Image;
import coil3.ImageLoader;
import coil3.ImageLoaders;
import coil3.Image_androidKt;
import coil3.SingletonImageLoader;
import coil3.network.NetworkHeaders;
import coil3.request.CachePolicy;
import coil3.request.ImageRequest;
import coil3.target.ImageViewTarget;

public class MainActivity extends AppCompatActivity {
    // General
    String TAG = "LogCat TaskIt: ";
    private Context context;
    private Activity activityContext;
    private SharedPreferences sharedPref;
    private String myID;
    private boolean taskMaster;
    private boolean admin;

    private Calendar calendar;
    private int day, month, year, hour, minute;

    private final String fS = "10FXS01";
    private final String newLine = "10NXL01";

    private ArrayList<String> usersNames;
    private ArrayList<String> usersIds;
    private ArrayList<Boolean> usersTaskMaster;
    private ArrayList<Boolean> usersAdmin;

    private ArrayList<String> theirTasksTaskId;
    private ArrayList<String> theirTasksTaskerId;
    private ArrayList<String> theirTasksTitle;
    private ArrayList<String> theirTasksDescription;
    private ArrayList<String> theirTasksDeadline;
    private ArrayList<Boolean> theirTasksTaskerMarkedAsDone;
    private ArrayList<Boolean> theirTasksAttachment1;
    private ArrayList<Boolean> theirTasksAttachment2;
    private ArrayList<String> theirTasksTaskerComment;
    private ArrayList<String> theirTasksTaskMasterComment;
    private ArrayList<Boolean> theirTasksTaskMasterMarkedAsDone;

    private ArrayList<String> myTasksTaskId;
    private ArrayList<String> myTasksTaskMasterId;
    private ArrayList<String> myTasksTitle;
    private ArrayList<String> myTasksDescription;
    private ArrayList<String> myTasksDeadline;
    private ArrayList<Boolean> myTasksTaskerMarkedAsDone;
    private ArrayList<Boolean> myTasksAttachment1;
    private ArrayList<Boolean> myTasksAttachment2;
    private ArrayList<String> myTasksTaskerComment;
    private ArrayList<String> myTasksTaskMasterComment;
    private ArrayList<Boolean> myTasksTaskMasterMarkedAsDone;

    private boolean justOne, showingImage;
    private int selectedTask, selectedAttachment, selectedUser;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private Uri photoUri;
    private File photoFile;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ExecutorService attachmentExecutor1, attachmentExecutor2 = Executors.newSingleThreadExecutor();

    // UI
    private final int LOGIN = 0;
    private final int MAIN_MENU = 1;
    private final int TASK_MASTER_TASKERS = 2;
    private final int TASK_MASTER_TASKS = 3;
    private final int TASKER_TASKS = 4;
    private final int USER_ADD = 5;
    private final int TASK_MASTER_NEW_TASK = 6;
    private final int CHANGE_PASSWORD = 7;
    private final int MY_TASK = 8;
    private final int THEIR_TASKS = 9;
    private final int MY_TASK_IMAGE_SHOW = 10;
    private final int THEIR_TASKS_IMAGE_SHOW = 11;
    private final int TASKER_MANAGEMENT = 12;
    private int currentScreen;

    private TextView info, addUserIdField, addTaskDate, addTaskTime, myTaskTitle, myTaskDescription, myTaskDeadline, myTaskTmComments, theirTasksNameField, theirTasksDate, theirTasksTime, theirTasksTComments, taskerManagementId;
    private LinearLayout loginCard, taskMasterCard, taskerCard, taskerManagementCard, adminCard, taskMasterTaskersCard, taskMasterTasksCard, taskerTasksCard, addUserCard, changePasswordCard, addTaskCard, myTaskCard, theirTasksCard, taskerTrigger;
    private ImageButton backButton, taskMasterTaskersCardButton, taskMasterTasksCardButton, taskerTasksCardButton, myTaskAttachment1, myTaskAttachment2, myTaskAttachment1TakePic, myTaskAttachment1DelPic, myTaskAttachment2TakePic, myTaskAttachment2DelPic, theirTasksAttachmentIB1, theirTasksAttachmentIB2;
    private Button signInButton, addUserCardButton, addUserGenerateIdButton, addUserAddButton, changePassCardButton, addTaskCardButton, addOneTaskButton, addMoreTaskButton, changePasswordChangeButton, myTaskSaveButton, theirTasksSaveButton, deleteUserButton, updateUserButton;
    private EditText loginUsernameField, loginPasswordField, addUserNameField, changePasswordOldField, changePasswordNew1Field, changePasswordNew2Field, addTaskTitle, addTaskDescription, myTaskMyComments, theirTasksTitleField, theirTasksDescriptionField, theirTasksMyComments, taskerManagementNameField;
    private CheckBox loginKeep, addUserTaskMaster, addUserAdmin, myTaskDone, theirTasksDone, taskerManagementTaskMaster, taskerManagementAdmin;
    private Spinner addTaskTaskerSpinner;
    private ListView usersListView, theirTasksListView, myTasksListView;
    private ImageView imageShow;

    private Image placeholder, fallback, error;

    // network
    private boolean isOnline;
    private BroadcastReceiver receiver;
    private final String emptyData = "emptyData";
    private final String loginPHP = "https://www.solvaelys.com/taskit/login.php";
    private final String addUserPHP = "https://www.solvaelys.com/taskit/add_user.php";
    private final String loadUsersPHP = "https://www.solvaelys.com/taskit/load_users.php";
    private final String loadTheirTasksPHP = "https://www.solvaelys.com/taskit/load_their_tasks.php";
    private final String loadMyTasksPHP = "https://www.solvaelys.com/taskit/load_my_tasks.php";
    private final String addTaskPHP = "https://www.solvaelys.com/taskit/add_task.php";
    private final String updateMyTaskPHP = "https://www.solvaelys.com/taskit/update_my_task.php";
    private final String updateTheirTasksPHP = "https://www.solvaelys.com/taskit/update_their_tasks.php";
    private final String changePasswordPHP = "https://www.solvaelys.com/taskit/change_password.php";
    private final String uploadImagePHP = "https://www.solvaelys.com/taskit/upload_image.php";
    private final String updateAttachmentPHP = "https://www.solvaelys.com/taskit/update_attachment.php";
    private final String updateUserPHP = "https://www.solvaelys.com/taskit/update_user.php";
    private final String deleteUserPHP = "https://www.solvaelys.com/taskit/delete_user.php";
    private final String taskImagesRemote = "https://www.solvaelys.com/taskit/images/";

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

        sharedPref = getSharedPreferences("com.awesome.taskit", Context.MODE_PRIVATE);
        myID = sharedPref.getString("myid", "");
        taskMaster = sharedPref.getBoolean("taskmaster", false);
        admin = sharedPref.getBoolean("admin", false);

        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        executor = Executors.newSingleThreadExecutor();
        attachmentExecutor1 = Executors.newSingleThreadExecutor();
        attachmentExecutor2 = Executors.newSingleThreadExecutor();

        placeholder = Image_androidKt.asImage(getDrawable(R.drawable.dowloading));
        fallback = Image_androidKt.asImage(getDrawable(R.drawable.fallback));
        error = Image_androidKt.asImage(getDrawable(R.drawable.error));

        assignViews();
        assignViewListeners();

        prepareNetwork();
        prepareCamera();

        if (myID.isEmpty()) {
            changeScreen(LOGIN);
        } else {
            changeScreen(MAIN_MENU);
        }

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
        taskerManagementCard = findViewById(R.id.tasker_management_card);
        adminCard = findViewById(R.id.admin_card);

        taskMasterTaskersCard = findViewById(R.id.task_master_taskers_card);
        taskMasterTasksCard = findViewById(R.id.task_master_tasks_card);
        taskerTasksCard = findViewById(R.id.tasker_tasks_card);
        myTaskCard = findViewById(R.id.my_task_card);
        theirTasksCard = findViewById(R.id.their_tasks_card);
        addUserCard = findViewById(R.id.add_user_card);
        changePasswordCard = findViewById(R.id.change_password_card);

        loginUsernameField = findViewById(R.id.login_username_field);
        loginPasswordField = findViewById(R.id.login_password_field);
        loginKeep = findViewById(R.id.login_keep);
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

        taskerManagementNameField = findViewById(R.id.tasker_management_name_field);
        taskerManagementId = findViewById(R.id.tasker_management_id);
        taskerManagementTaskMaster = findViewById(R.id.tasker_management_task_master);
        taskerManagementAdmin = findViewById(R.id.tasker_management_admin);
        deleteUserButton = findViewById(R.id.delete_user_button);
        updateUserButton = findViewById(R.id.update_user_button);

        addTaskCard = findViewById(R.id.add_task_card);
        theirTasksListView = findViewById(R.id.their_tasks_listview);

        theirTasksNameField = findViewById(R.id.their_tasks_name);
        theirTasksTitleField = findViewById(R.id.their_tasks_title);
        theirTasksDescriptionField = findViewById(R.id.their_tasks_description);
        theirTasksDate = findViewById(R.id.their_tasks_date);
        theirTasksTime = findViewById(R.id.their_tasks_time);
        theirTasksAttachmentIB1 = findViewById(R.id.their_tasks_attachment_ib_1);
        theirTasksAttachmentIB2 = findViewById(R.id.their_tasks_attachment_ib_2);
        theirTasksTComments = findViewById(R.id.their_tasks_t_comments);
        theirTasksMyComments = findViewById(R.id.their_tasks_my_comments);
        theirTasksDone = findViewById(R.id.their_tasks_done);
        theirTasksSaveButton = findViewById(R.id.their_tasks_save_button);

        addTaskTaskerSpinner = findViewById(R.id.add_task_tasker_spinner);
        addTaskTitle = findViewById(R.id.add_task_title);
        addTaskDescription = findViewById(R.id.add_task_description);
        addTaskDate = findViewById(R.id.add_task_date);
        addTaskTime = findViewById(R.id.add_task_time);
        addOneTaskButton = findViewById(R.id.add_one_task_button);
        addMoreTaskButton = findViewById(R.id.add_more_task_button);

        myTaskTitle = findViewById(R.id.my_task_title);
        myTaskDescription = findViewById(R.id.my_task_description);
        myTaskDeadline = findViewById(R.id.my_task_deadline);
        myTaskAttachment1 = findViewById(R.id.my_task_attachment_1);
        myTaskAttachment1TakePic = findViewById(R.id.my_task_attachment_1_take_pic);
        myTaskAttachment1DelPic = findViewById(R.id.my_task_attachment_1_del_pic);
        myTaskAttachment2 = findViewById(R.id.my_task_attachment_2);
        myTaskAttachment2TakePic = findViewById(R.id.my_task_attachment_2_take_pic);
        myTaskAttachment2DelPic = findViewById(R.id.my_task_attachment_2_del_pic);
        myTaskMyComments = findViewById(R.id.my_task_my_comments);
        myTaskTmComments = findViewById(R.id.my_task_tm_comments);
        myTaskDone = findViewById(R.id.my_task_done);
        myTaskSaveButton = findViewById(R.id.my_task_save_button);

        taskerTasksCardButton = findViewById(R.id.tasker_tasks_card_button);
        myTasksListView = findViewById(R.id.my_tasks_listview);

        usersListView = findViewById(R.id.users_listview);

        changePasswordOldField = findViewById(R.id.change_password_old_field);
        changePasswordNew1Field = findViewById(R.id.change_password_new1_field);
        changePasswordNew2Field = findViewById(R.id.change_password_new2_field);
        changePasswordChangeButton = findViewById(R.id.change_password_change_button);

        imageShow = findViewById(R.id.image_show);
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
                checkCredentials();
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

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
        updateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
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

        addTaskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dpdYear, int dpdMonthOfYear, int dpdDayOfMonth) {
                        day = dpdDayOfMonth;
                        month = dpdMonthOfYear + 1;
                        year = dpdYear;
                        addTaskDate.setText(day + "/" + month + "/" + year);
                        calendar.set(Calendar.YEAR, dpdYear);
                        calendar.set(Calendar.MONTH, dpdMonthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dpdDayOfMonth);
                    }
                }, year, month - 1, day);
                datePickerDialog.show();
            }
        });
        addTaskTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int tpdHourOfDay, int tpdMinute) {
                        hour = tpdHourOfDay;
                        minute = tpdMinute;
                        addTaskTime.setText(hour + ":" + String.format("%02d", minute));
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

        theirTasksAttachmentIB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (theirTasksAttachment1.get(selectedTask)) {
                    selectedAttachment = 1;
                    loadShowImage(true);
                    changeScreen(THEIR_TASKS_IMAGE_SHOW);
                }
            }
        });
        theirTasksAttachmentIB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (theirTasksAttachment2.get(selectedTask)) {
                    selectedAttachment = 2;
                    changeScreen(THEIR_TASKS_IMAGE_SHOW);
                    loadShowImage(true);
                }
            }
        });
        theirTasksDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dpdYear, int dpdMonthOfYear, int dpdDayOfMonth) {
                        day = dpdDayOfMonth;
                        month = dpdMonthOfYear + 1;
                        year = dpdYear;
                        theirTasksDate.setText(day + "/" + month + "/" + year);
                        calendar.set(Calendar.YEAR, dpdYear);
                        calendar.set(Calendar.MONTH, dpdMonthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dpdDayOfMonth);
                    }
                }, year, month - 1, day);
                datePickerDialog.show();
            }
        });
        theirTasksTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int tpdHourOfDay, int tpdMinute) {
                        hour = tpdHourOfDay;
                        minute = tpdMinute;
                        theirTasksTime.setText(hour + ":" + String.format("%02d", minute));
                        calendar.set(Calendar.HOUR_OF_DAY, tpdHourOfDay);
                        calendar.set(Calendar.MINUTE, tpdMinute);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });
        theirTasksSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTheirTasks();
            }
        });

        myTaskAttachment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTasksAttachment1.get(selectedTask)) {
                    selectedAttachment = 1;
                    changeScreen(MY_TASK_IMAGE_SHOW);
                    loadShowImage(false);
                }
            }
        });
        myTaskAttachment1TakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAttachment = 1;
                myTaskAttachment1.setEnabled(false);
                myTaskAttachment1TakePic.setEnabled(false);
                myTaskAttachment1DelPic.setEnabled(false);
                myTaskAttachment2.setEnabled(false);
                myTaskAttachment2TakePic.setEnabled(false);
                myTaskAttachment2DelPic.setEnabled(false);
                myTaskSaveButton.setEnabled(false);
                if (hasCameraPermission()) {
                    openCamera();
                } else {
                    checkCameraPermission();
                    myTaskAttachment1.setEnabled(true);
                    myTaskAttachment1TakePic.setEnabled(true);
                    myTaskAttachment1DelPic.setEnabled(true);
                    myTaskAttachment2.setEnabled(true);
                    myTaskAttachment2TakePic.setEnabled(true);
                    myTaskAttachment2DelPic.setEnabled(true);
                    myTaskSaveButton.setEnabled(true);
                }
            }
        });
        myTaskAttachment1DelPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTasksAttachment1.get(selectedTask)) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    selectedAttachment = 1;
                                    myTasksAttachment1.set(selectedTask, false);
                                    info.setText(contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "0")));
                                    populateMyTaskCard(selectedTask);
                                    break;
                                default:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Delete picture?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                }



            }
        });
        myTaskAttachment2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTasksAttachment2.get(selectedTask)) {
                    selectedAttachment = 2;
                    changeScreen(MY_TASK_IMAGE_SHOW);
                    loadShowImage(false);
                }
            }
        });
        myTaskAttachment2TakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAttachment = 2;
                myTaskAttachment1.setEnabled(false);
                myTaskAttachment1TakePic.setEnabled(false);
                myTaskAttachment1DelPic.setEnabled(false);
                myTaskAttachment2.setEnabled(false);
                myTaskAttachment2TakePic.setEnabled(false);
                myTaskAttachment2DelPic.setEnabled(false);
                myTaskSaveButton.setEnabled(false);
                if (hasCameraPermission()) {
                    openCamera();
                } else {
                    checkCameraPermission();
                    myTaskAttachment1.setEnabled(true);
                    myTaskAttachment1TakePic.setEnabled(true);
                    myTaskAttachment1DelPic.setEnabled(true);
                    myTaskAttachment2.setEnabled(true);
                    myTaskAttachment2TakePic.setEnabled(true);
                    myTaskAttachment2DelPic.setEnabled(true);
                    myTaskSaveButton.setEnabled(true);
                }
            }
        });
        myTaskAttachment2DelPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myTasksAttachment2.get(selectedTask)) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    selectedAttachment = 2;
                                    myTasksAttachment2.set(selectedTask, false);
                                    info.setText(contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "0")));
                                    populateMyTaskCard(selectedTask);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Delete picture?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                }
            }
        });
        myTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMyTask();
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
                loginUsernameField.setText("");
                loginPasswordField.setText("");
                loginKeep.setChecked(false);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("myid", "");
                editor.putBoolean("taskmaster", false);
                editor.putBoolean("admin", false);
                editor.apply();
                break;
            case MAIN_MENU:
                loginCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.GONE);
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
                addUserCard.setVisibility(View.GONE);
                taskerManagementCard.setVisibility(View.GONE);
                taskMasterTaskersCard.setVisibility(View.VISIBLE);
                break;
            case TASKER_MANAGEMENT:
                taskMasterTaskersCard.setVisibility(View.GONE);
                taskerManagementCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                addTaskCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                theirTasksCard.setVisibility(View.GONE);
                taskMasterTasksCard.setVisibility(View.VISIBLE);
                loadTheirTasks();
                break;
            case TASKER_TASKS:
                taskMasterCard.setVisibility(View.GONE);
                taskerCard.setVisibility(View.GONE);
                adminCard.setVisibility(View.GONE);
                myTaskCard.setVisibility(View.GONE);
                taskerTasksCard.setVisibility(View.VISIBLE);
                loadMyTasks();
                break;
            case USER_ADD:
                taskMasterTaskersCard.setVisibility(View.GONE);
                addUserCard.setVisibility(View.VISIBLE);
                addUserNameField.setText("");
                addUserIdField.setText(generateID());
                addUserTaskMaster.setChecked(false);
                addUserAdmin.setChecked(false);
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
            case MY_TASK:
                taskerTasksCard.setVisibility(View.GONE);
                imageShow.setVisibility(View.GONE);
                myTaskCard.setVisibility(View.VISIBLE);
                if (!showingImage) {
                    populateMyTaskCard(selectedTask);
                } else {
                    showingImage = false;
                }
                break;
            case THEIR_TASKS:
                taskMasterTasksCard.setVisibility(View.GONE);
                imageShow.setVisibility(View.GONE);
                theirTasksCard.setVisibility(View.VISIBLE);
                if (!showingImage) {
                    populateTheirTasksCard(selectedTask);
                } else {
                    showingImage = false;
                }
                break;
            case MY_TASK_IMAGE_SHOW:
                myTaskCard.setVisibility(View.GONE);
                imageShow.setVisibility(View.VISIBLE);
                showingImage = true;
                break;
            case THEIR_TASKS_IMAGE_SHOW:
                theirTasksCard.setVisibility(View.GONE);
                imageShow.setVisibility(View.VISIBLE);
                showingImage = true;
                break;
        }
        currentScreen = newScreen;
    }

    private void backButtonPressed() {
        switch (currentScreen) {
            case LOGIN:
                finish();
                break;
            case MAIN_MENU:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
                                changeScreen(LOGIN);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Close TaskIt?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener)
                        .setNeutralButton("Logout", dialogClickListener)
                        .show();
                break;
            case TASK_MASTER_TASKERS:
            case TASK_MASTER_TASKS:
            case TASKER_TASKS:
            case CHANGE_PASSWORD:
                changeScreen(MAIN_MENU);
                break;
            case USER_ADD:
            case TASKER_MANAGEMENT:
                changeScreen(TASK_MASTER_TASKERS);
                break;
            case TASK_MASTER_NEW_TASK:
            case THEIR_TASKS:
                changeScreen(TASK_MASTER_TASKS);
                break;
            case MY_TASK:
                changeScreen(TASKER_TASKS);
                break;
            case MY_TASK_IMAGE_SHOW:
                changeScreen(MY_TASK);
                break;
            case THEIR_TASKS_IMAGE_SHOW:
                changeScreen(THEIR_TASKS);
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

    private String uploadImage(File imageFile, File thumbFile, String rawData) {
        String response = "";
        String boundary = "----AndroidBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(uploadImagePHP);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            rawData = rawData.replace("'", "''");

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

            // 1. Send encrypted rawdata
            dataOutputStream.writeBytes("--" + boundary + LINE_FEED);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"rawdata\"" + LINE_FEED);
            dataOutputStream.writeBytes(LINE_FEED);
            dataOutputStream.writeBytes(rawData + LINE_FEED);

            // 2. Send main image
            String fileName = imageFile.getName();
            dataOutputStream.writeBytes("--" + boundary + LINE_FEED);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + LINE_FEED);
            dataOutputStream.writeBytes("Content-Type: image/jpeg" + LINE_FEED);
            dataOutputStream.writeBytes(LINE_FEED);

            FileInputStream inputStream = new FileInputStream(imageFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            dataOutputStream.writeBytes(LINE_FEED);

            // 3. Send thumbnail
            if (thumbFile != null && thumbFile.exists()) {
                String thumbName = thumbFile.getName();
                dataOutputStream.writeBytes("--" + boundary + LINE_FEED);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"thumb\"; filename=\"" + thumbName + "\"" + LINE_FEED);
                dataOutputStream.writeBytes("Content-Type: image/jpeg" + LINE_FEED);
                dataOutputStream.writeBytes(LINE_FEED);

                FileInputStream thumbStream = new FileInputStream(thumbFile);
                while ((bytesRead = thumbStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }
                thumbStream.close();
                dataOutputStream.writeBytes(LINE_FEED);
            }

            // End request
            dataOutputStream.writeBytes("--" + boundary + "--" + LINE_FEED);
            dataOutputStream.flush();
            dataOutputStream.close();

            // 4. Get server response
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line + "\n";
            }
            reader.close();
        } catch (Exception e) {
            response = e.getMessage();
        }
        return response;
    }

    // general

    private void checkCredentials() {
        signInButton.setEnabled(false);
        if (!loginUsernameField.getText().toString().isEmpty()) {
            String nameTemp = loginUsernameField.getText().toString().replaceAll("\\s+$", "");
            String rawData = nameTemp + fS + loginPasswordField.getText().toString();
            String response = contactServer(loginPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);
            if (response.contains("Login successful")) {
                String[] resp = response.split(newLine);
                String[] temp = resp[0].split(fS);
                myID = temp[1];
                if (temp[2].equals("0")) {
                    taskMaster = false;
                } else {
                    taskMaster = true;
                }
                if (temp[3].equals("0")) {
                    admin = false;
                } else {
                    admin = true;
                }
                info.setText("");
                signInButton.setEnabled(true);
                if (loginKeep.isChecked()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("myid", myID);
                    editor.putBoolean("taskmaster", taskMaster);
                    editor.putBoolean("admin", admin);
                    editor.apply();
                }
                changeScreen(MAIN_MENU);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("");
                        signInButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            info.setText("ERROR\nUsername cannot be empty");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("");
                    signInButton.setEnabled(true);
                }
            }, 2000);
        }
    }

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
                        loadUsers();
                        changeScreen(TASK_MASTER_TASKERS);
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
                    theirTasksTaskId = new ArrayList<>();
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
                        if (line.length == 11) {
                            theirTasksTaskId.add(line[0]);
                            theirTasksTaskerId.add(line[1]);
                            theirTasksTitle.add(line[2]);
                            theirTasksDescription.add(line[3]);
                            theirTasksDeadline.add(line[4]);
                            if (line[5].equals("0")) {
                                theirTasksTaskerMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskerMarkedAsDone.add(true);
                            }
                            if (line[6].equals("0")) {
                                theirTasksAttachment1.add(false);
                            } else {
                                theirTasksAttachment1.add(true);
                            }
                            if (line[6].equals("0")) {
                                theirTasksAttachment2.add(false);
                            } else {
                                theirTasksAttachment2.add(true);
                            }
                            theirTasksTaskerComment.add(line[8]);
                            theirTasksTaskMasterComment.add(line[9]);
                            if (line[10].equals("0")) {
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
                    myTasksTaskId = new ArrayList<>();
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
                        if (line.length == 11) {
                            myTasksTaskId.add(line[0]);
                            myTasksTaskMasterId.add(line[1]);
                            myTasksTitle.add(line[2]);
                            myTasksDescription.add(line[3]);
                            myTasksDeadline.add(line[4]);
                            if (line[5].equals("0")) {
                                myTasksTaskerMarkedAsDone.add(false);
                            } else {
                                myTasksTaskerMarkedAsDone.add(true);
                            }
                            if (line[6].equals("0")) {
                                myTasksAttachment1.add(false);
                            } else {
                                myTasksAttachment1.add(true);
                            }
                            if (line[7].equals("0")) {
                                myTasksAttachment2.add(false);
                            } else {
                                myTasksAttachment2.add(true);
                            }
                            myTasksTaskerComment.add(line[8]);
                            myTasksTaskMasterComment.add(line[9]);
                            if (line[10].equals("0")) {
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
            String rawData = generateID() + fS +                                                                            // task_id
                             myID + fS +                                                                                    // task_master_id
                             usersIds.get(usersNames.indexOf(addTaskTaskerSpinner.getSelectedItem().toString())) + fS +     // tasker_id
                             addTaskTitle.getText().toString() + fS +                                                       // title
                             addTaskDescription.getText().toString() + fS +                                                 // description
                             year + "-" + (month + 1) + "-" + day + " " + hour + ":" + minute + ":00" + fS +                // deadline
                             "0" + fS +                                                                                     // tasker_marked_as_done
                             "0" + fS +                                                                                     // attachment_1
                             "0" + fS +                                                                                     // attachment_2
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

    private void updateMyTask() {
        myTaskAttachment1.setEnabled(false);
        myTaskAttachment1TakePic.setEnabled(false);
        myTaskAttachment1DelPic.setEnabled(false);
        myTaskAttachment2.setEnabled(false);
        myTaskAttachment2TakePic.setEnabled(false);
        myTaskAttachment2DelPic.setEnabled(false);
        myTaskSaveButton.setEnabled(false);

        myTasksTaskerMarkedAsDone.set(selectedTask, myTaskDone.isChecked());
        if (myTaskMyComments.getText().toString().isEmpty()) {
            myTasksTaskerComment.set(selectedTask, " ");
        } else {
            myTasksTaskerComment.set(selectedTask, myTaskMyComments.getText().toString());
        }

        String tempMarked = "0";
        if (myTasksTaskerMarkedAsDone.get(selectedTask)) tempMarked = "1";
        String tempAttachment1 = "0";
        if (myTasksAttachment1.get(selectedTask)) tempAttachment1 = "1";
        String tempAttachment2 = "0";
        if (myTasksAttachment2.get(selectedTask)) tempAttachment2 = "1";

        String rawData = myTasksTaskId.get(selectedTask) + fS +
                tempMarked + fS +
                tempAttachment1 + fS +
                tempAttachment2 + fS +
                myTasksTaskerComment.get(selectedTask);

        String response = contactServer(updateMyTaskPHP, Java_AES_Cipher.encryptSimple(rawData));
        response = response.replaceAll(newLine, "\n");
        info.setText(response);

        if (response.contains("Task updated successfully")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myTaskAttachment1.setEnabled(true);
                    myTaskAttachment1TakePic.setEnabled(true);
                    myTaskAttachment1DelPic.setEnabled(true);
                    myTaskAttachment2.setEnabled(true);
                    myTaskAttachment2TakePic.setEnabled(true);
                    myTaskAttachment2DelPic.setEnabled(true);
                    myTaskSaveButton.setEnabled(true);
                    info.setText("");
                    changeScreen(TASKER_TASKS);
                }
            }, 1500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myTaskAttachment1.setEnabled(true);
                    myTaskAttachment1TakePic.setEnabled(true);
                    myTaskAttachment1DelPic.setEnabled(true);
                    myTaskAttachment2.setEnabled(true);
                    myTaskAttachment2TakePic.setEnabled(true);
                    myTaskAttachment2DelPic.setEnabled(true);
                    myTaskSaveButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void updateTheirTasks() {
        if (!theirTasksTitleField.getText().toString().isEmpty()) theirTasksTitle.set(selectedTask, theirTasksTitleField.getText().toString());
        if (!theirTasksDescriptionField.getText().toString().isEmpty()) theirTasksDescription.set(selectedTask, theirTasksDescriptionField.getText().toString());
        theirTasksDeadline.set(selectedTask, year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
        if (!theirTasksMyComments.getText().toString().isEmpty()) theirTasksTaskMasterComment.set(selectedTask, theirTasksMyComments.getText().toString());
        theirTasksTaskMasterMarkedAsDone.set(selectedTask, theirTasksDone.isChecked());

        String tempMarked = "0";
        if (theirTasksTaskMasterMarkedAsDone.get(selectedTask)) tempMarked = "1";

        theirTasksSaveButton.setEnabled(false);
        String rawData = theirTasksTaskId.get(selectedTask) + fS +
                         theirTasksTitle.get(selectedTask) + fS +
                         theirTasksDescription.get(selectedTask) + fS +
                         theirTasksDeadline.get(selectedTask) + fS +
                         theirTasksTaskMasterComment.get(selectedTask) + fS +
                         tempMarked;

        String response = contactServer(updateTheirTasksPHP, Java_AES_Cipher.encryptSimple(rawData));
        response = response.replaceAll(newLine, "\n");
        info.setText(response);

        if (response.contains("Task updated successfully")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksSaveButton.setEnabled(true);
                    info.setText("");
                    changeScreen(TASK_MASTER_TASKS);
                }
            }, 1500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksSaveButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void updateUser() {
        deleteUserButton.setEnabled(false);
        updateUserButton.setEnabled(false);
        if (!taskerManagementNameField.getText().toString().isEmpty()) {
            String tempTaskMaster = "0";
            if (taskerManagementTaskMaster.isChecked()) tempTaskMaster = "1";
            String tempAdmin = "0";
            if (taskerManagementAdmin.isChecked()) tempAdmin = "1";

            String rawData = usersIds.get(selectedUser) + fS +
                    taskerManagementNameField.getText().toString() + fS +
                    tempTaskMaster + fS +
                    tempAdmin;

            String response = contactServer(updateUserPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            info.setText(response);

            if (response.contains("User updated")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteUserButton.setEnabled(true);
                        updateUserButton.setEnabled(true);
                        info.setText("");
                        changeScreen(TASK_MASTER_TASKERS);
                        loadUsers();
                    }
                }, 2000);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteUserButton.setEnabled(true);
                        updateUserButton.setEnabled(true);
                        info.setText("");
                    }
                }, 2000);
            }
        } else {
            info.setText("");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    deleteUserButton.setEnabled(true);
                    updateUserButton.setEnabled(true);
                    info.setText("");
                }
            }, 2000);
        }
    }

    private void deleteUser() {
        deleteUserButton.setEnabled(false);
        updateUserButton.setEnabled(false);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        String rawData = usersIds.get(selectedUser);
                        String response = contactServer(deleteUserPHP, Java_AES_Cipher.encryptSimple(rawData));
                        response = response.replaceAll(newLine, "\n");
                        info.setText(response);

                        if (response.contains("User deleted")) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteUserButton.setEnabled(true);
                                    updateUserButton.setEnabled(true);
                                    info.setText("");
                                    changeScreen(TASK_MASTER_TASKERS);
                                    loadUsers();
                                }
                            }, 2000);
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteUserButton.setEnabled(true);
                                    updateUserButton.setEnabled(true);
                                    info.setText("");
                                }
                            }, 2000);
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        deleteUserButton.setEnabled(true);
                        updateUserButton.setEnabled(true);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Delete user?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener)
                .show();
    }

    private void populateMyTaskCard(int which) {
        myTaskTitle.setText(myTasksTitle.get(which));
        myTaskDescription.setText(myTasksDescription.get(which));

        String[] tempDeadline = myTasksDeadline.get(which).split(" ");
        if (tempDeadline.length == 2) {
            String[] tempDate = tempDeadline[0].split("-");
            myTaskDeadline.setText(tempDate[2] + "-" + tempDate[1] + "-" + tempDate[0] + " " + tempDeadline[1].substring(0, 5));
        } else {
            myTaskDeadline.setText("00-00-0000 00:00");
        }

        if (myTasksAttachment1.get(which)) {
            ImageLoader attachment1ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment1Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + myTasksTaskId.get(selectedTask) + "-1-thumb.jpg")
                    .placeholder(placeholder)
                    .fallback(fallback)
                    .error(error)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .target(new ImageViewTarget(myTaskAttachment1))
                    .build();
            attachment1ImageLoader.enqueue(attachment1Request);
            attachmentExecutor1.execute(() -> {
                try {
                    ImageLoaders.executeBlocking(attachment1ImageLoader, attachment1Request).getImage();
                    runOnUiThread(() -> {});
                } catch (Exception e) {
                    System.out.println(TAG + e.getMessage());
                }
            });
        } else {
            myTaskAttachment1.setImageResource(R.drawable.fallback);
        }

        if (myTasksAttachment2.get(which)) {
            ImageLoader attachment2ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment2Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + myTasksTaskId.get(selectedTask) + "-2-thumb.jpg")
                    .placeholder(placeholder)
                    .fallback(fallback)
                    .error(error)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .target(new ImageViewTarget(myTaskAttachment2))
                    .build();
            attachment2ImageLoader.enqueue(attachment2Request);
            attachmentExecutor2.execute(() -> {
                try {
                    ImageLoaders.executeBlocking(attachment2ImageLoader, attachment2Request).getImage();
                    runOnUiThread(() -> {});
                } catch (Exception e) {
                    System.out.println(TAG + e.getMessage());
                }
            });
        } else {
            myTaskAttachment2.setImageResource(R.drawable.fallback);
        }

        if (myTasksTaskerComment.get(which).equals(" ")) {
            myTaskMyComments.setText("");
        } else {
            myTaskMyComments.setText(myTasksTaskerComment.get(which));
        }

        if (myTasksTaskMasterComment.get(which).equals(" ")) {
            myTaskTmComments.setText("");
        } else {
            myTaskTmComments.setText(myTasksTaskMasterComment.get(which));
        }
        myTaskDone.setChecked(myTasksTaskerMarkedAsDone.get(which));
    }

    private void populateTheirTasksCard(int which) {
        theirTasksNameField.setText(usersNames.get(usersIds.indexOf(theirTasksTaskerId.get(which))));
        theirTasksTitleField.setText(theirTasksTitle.get(which));
        theirTasksDescriptionField.setText(theirTasksDescription.get(which));

        String tempDD = theirTasksDeadline.get(which);
        if (tempDD.length() == 19 && tempDD.contains(" ")) {
            String[] tempEach = tempDD.split(" ");
            String[] dateEach = tempEach[0].split("-");
            theirTasksDate.setText(dateEach[2] + "-" + dateEach[1] + "-" + dateEach[0]);
            theirTasksTime.setText(tempEach[1].substring(0, 5));
            try {
                day = Integer.parseInt(dateEach[2]);
                month = Integer.parseInt(dateEach[1]);
                year = Integer.parseInt(dateEach[0]);
                hour = Integer.parseInt(tempEach[1].substring(0, 2));
                minute = Integer.parseInt(tempEach[1].substring(3, 5));

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception e) {
                System.out.println(TAG + e.getMessage());
            }
        } else {
            theirTasksDate.setText("00-00-0000");
            theirTasksTime.setText("00:00");
        }

        if (theirTasksAttachment1.get(which)) {
            ImageLoader attachment1ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment1Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + theirTasksTaskId.get(selectedTask) + "-1-thumb.jpg")
                    .placeholder(placeholder)
                    .fallback(fallback)
                    .error(error)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .target(new ImageViewTarget(theirTasksAttachmentIB1))
                    .build();
            attachment1ImageLoader.enqueue(attachment1Request);
            attachmentExecutor1.execute(() -> {
                try {
                    ImageLoaders.executeBlocking(attachment1ImageLoader, attachment1Request).getImage();
                    runOnUiThread(() -> {});
                } catch (Exception e) {
                    System.out.println(TAG + e.getMessage());
                }
            });
        } else {
            theirTasksAttachmentIB1.setImageResource(R.drawable.fallback);
        }

        if (theirTasksAttachment2.get(which)) {
            ImageLoader attachment2ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment2Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + theirTasksTaskId.get(selectedTask) + "-2-thumb.jpg")
                    .placeholder(placeholder)
                    .fallback(fallback)
                    .error(error)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .target(new ImageViewTarget(theirTasksAttachmentIB2))
                    .build();
            attachment2ImageLoader.enqueue(attachment2Request);
            attachmentExecutor2.execute(() -> {
                try {
                    ImageLoaders.executeBlocking(attachment2ImageLoader, attachment2Request).getImage();
                    runOnUiThread(() -> {});
                } catch (Exception e) {
                    System.out.println(TAG + e.getMessage());
                }
            });
        } else {
            theirTasksAttachmentIB2.setImageResource(R.drawable.fallback);
        }

        theirTasksTComments.setText(theirTasksTaskerComment.get(which));
        if (theirTasksTaskMasterComment.get(which).equals(" ")) {
            theirTasksMyComments.setText("");
        } else {
            theirTasksMyComments.setText(theirTasksTaskMasterComment.get(which));
        }
        theirTasksDone.setChecked(theirTasksTaskMasterMarkedAsDone.get(which));
    }

    private void populateTaskerManagement() {
        taskerManagementNameField.setText(usersNames.get(selectedUser));
        taskerManagementId.setText(usersIds.get(selectedUser));
        taskerManagementTaskMaster.setChecked(usersTaskMaster.get(selectedUser));
        taskerManagementAdmin.setChecked(usersAdmin.get(selectedUser));
    }

    private void loadShowImage(boolean showTheirs) {
        String taskId;
        if (showTheirs) {
            taskId = theirTasksTaskId.get(selectedTask);
        } else {
            taskId = myTasksTaskId.get(selectedTask);
        }

        ImageLoader showImageLoader = SingletonImageLoader.get(context);
        ImageRequest showImageRequest = new ImageRequest.Builder(context)
                .data(taskImagesRemote + taskId + "-" + selectedAttachment + ".jpg")
                .placeholder(placeholder)
                .fallback(fallback)
                .error(error)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .target(new ImageViewTarget(imageShow))
                .build();
        showImageLoader.enqueue(showImageRequest);

        executor.execute(() -> {
            try {
                ImageLoaders.executeBlocking(showImageLoader, showImageRequest).getImage();
                runOnUiThread(() -> {});
            } catch (Exception e) {
                System.out.println(TAG + e.getMessage());
            }
        });
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
                        changeScreen(MAIN_MENU);
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

            LinearLayout trigger = (LinearLayout) view.findViewById(R.id.tasker_trigger);
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);

            trigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedUser = usersIds.indexOf(id.get(position));
                    changeScreen(TASKER_MANAGEMENT);
                    populateTaskerManagement();
                }
            });
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
            String[] tempDeadline = deadline.get(position).split(" ");
            if (tempDeadline.length == 2) {
                String[] tempDate = tempDeadline[0].split("-");
                text3.setText(tempDate[2] + "-" + tempDate[1] + "-" + tempDate[0] + " " + tempDeadline[1].substring(0, 5));
            } else {
                text3.setText("00-00-0000 00:00");
            }
            check.setChecked(done.get(position));

            LinearLayout openTheirTasksTrigger  = (LinearLayout) view.findViewById(R.id.open_their_tasks_trigger);
            openTheirTasksTrigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTask = position;
                    changeScreen(THEIR_TASKS);
                }
            });

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
            String[] tempDeadline = deadline.get(position).split(" ");
            if (tempDeadline.length == 2) {
                String[] tempDate = tempDeadline[0].split("-");
                text2.setText(tempDate[2] + "-" + tempDate[1] + "-" + tempDate[0] + " " + tempDeadline[1].substring(0, 5));
            } else {
                text2.setText("00-00-0000 00:00");
            }
            check.setChecked(done.get(position));

            LinearLayout openMyTaskTrigger  = (LinearLayout) view.findViewById(R.id.open_my_task_trigger);
            openMyTaskTrigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTask = position;
                    changeScreen(MY_TASK);
                }
            });

            return view;
        }
    }

    private void checkCameraPermission() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void prepareCamera() {
        if (!hasCameraPermission()) {
            checkCameraPermission();
        }
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        info.setText("Resizing picture...  ");
                        myTaskAttachment1.setEnabled(false);
                        myTaskAttachment2.setEnabled(false);
                        myTaskSaveButton.setEnabled(false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                File image = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + ".jpg");
                                File imageThumb = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + "-thumb.jpg");
                                scaleAndSaveImage(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + ".jpg");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String uploadResult = uploadImage(image, imageThumb, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment));
                                        info.setText(uploadResult);
                                        populateMyTaskCard(selectedTask);
                                        myTaskAttachment1.setEnabled(true);
                                        myTaskAttachment2.setEnabled(true);
                                        myTaskSaveButton.setEnabled(true);
                                        if (uploadResult.contains("Picture uploaded successfully")) {
                                            if (selectedAttachment == 1) {
                                                myTasksAttachment1.set(selectedTask, true);
                                            } else {
                                                myTasksAttachment2.set(selectedTask, true);
                                            }
                                            populateMyTaskCard(selectedTask);
                                            info.setText(info.getText() + contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "1")));
                                            myTaskAttachment1.setEnabled(true);
                                            myTaskAttachment1TakePic.setEnabled(true);
                                            myTaskAttachment1DelPic.setEnabled(true);
                                            myTaskAttachment2.setEnabled(true);
                                            myTaskAttachment2TakePic.setEnabled(true);
                                            myTaskAttachment2DelPic.setEnabled(true);
                                            myTaskSaveButton.setEnabled(true);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    info.setText("");
                                                }
                                            }, 4000);
                                        }
                                    }
                                }, 100);
                            }
                        }, 300);
                    }
                }
        );
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(context,getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(intent);
            } catch (IOException e) {
                info.setText("ERROR\n" + e.getMessage());
            }
        }
    }

    private File createImageFile() throws IOException {
        File storageDir = new File(context.getExternalFilesDir(null).getAbsolutePath());
        String fileName = selectedAttachment + ".jpg";
        return new File(storageDir, fileName);
    }

    private void scaleAndSaveImage(String imagePath) {
        try {
            File inputFile = new File(imagePath);

            if (!inputFile.exists()) {
                info.setText("ERROR\nFile does not exist: " + inputFile.getAbsolutePath());
                return;
            }

            // Decode original image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap originalBitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath(), options);

            if (originalBitmap == null) {
                info.setText("ERROR\nFailed to decode image.");
                return;
            }

            // ✅ Fix orientation first
            originalBitmap = applyExifRotation(inputFile, originalBitmap);

            int origWidth = originalBitmap.getWidth();
            int origHeight = originalBitmap.getHeight();

            int minLongSide = 1920;
            int minShortSide = 1080;

            int newWidth = origWidth;
            int newHeight = origHeight;

            boolean isPortrait = origHeight >= origWidth;

            // Only scale if both sides are larger than the limits
            if (origWidth > minShortSide && origHeight > minLongSide) {
                if (isPortrait) {
                    // Portrait -> height is the long side
                    float scale = Math.max(
                            (float) minLongSide / origHeight,
                            (float) minShortSide / origWidth
                    );
                    newHeight = Math.round(origHeight * scale);
                    newWidth = Math.round(origWidth * scale);
                } else {
                    // Landscape -> width is the long side
                    float scale = Math.max(
                            (float) minLongSide / origWidth,
                            (float) minShortSide / origHeight
                    );
                    newWidth = Math.round(origWidth * scale);
                    newHeight = Math.round(origHeight * scale);
                }
            }

            // Only scale if dimensions changed
            Bitmap scaledBitmap = (newWidth != origWidth || newHeight != origHeight)
                    ? Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                    : originalBitmap;

            FileOutputStream out = new FileOutputStream(inputFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            // ✅ Also create a thumbnail
            int thumbSize = 256; // max long side for thumbnail
            float thumbScale = Math.min(
                    (float) thumbSize / scaledBitmap.getWidth(),
                    (float) thumbSize / scaledBitmap.getHeight()
            );

            int thumbWidth = Math.round(scaledBitmap.getWidth() * thumbScale);
            int thumbHeight = Math.round(scaledBitmap.getHeight() * thumbScale);

            Bitmap thumbBitmap = Bitmap.createScaledBitmap(scaledBitmap, thumbWidth, thumbHeight, true);

            // Save thumbnail as <name>-thumb.jpg
            String thumbPath = imagePath.replace(".jpg", "-thumb.jpg");
            File thumbFile = new File(thumbPath);

            FileOutputStream thumbOut = new FileOutputStream(thumbFile);
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 60, thumbOut); // more compact
            thumbOut.flush();
            thumbOut.close();

            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            scaledBitmap.recycle();
            thumbBitmap.recycle();
            info.setText("Image saved\nUploading picture...\n");
        } catch (Exception e) {
            info.setText("ERROR\n" + e.getMessage());
        }
    }

    private Bitmap applyExifRotation(File file, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.preScale(-1f, 1f);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.preScale(1f, -1f);
                    break;
                default:
                    return bitmap;
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true
            );

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
            return rotatedBitmap;

        } catch (Exception e) {
            info.setText("ERROR\n" + e.getMessage());
            return bitmap;
        }
    }

}