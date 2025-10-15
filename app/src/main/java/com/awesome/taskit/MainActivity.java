package com.awesome.taskit;

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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import coil3.Image;
import coil3.ImageLoader;
import coil3.ImageLoaders;
import coil3.Image_androidKt;
import coil3.SingletonImageLoader;
import coil3.request.CachePolicy;
import coil3.request.ImageRequest;
import coil3.target.ImageViewTarget;

public class MainActivity extends AppCompatActivity {
    // General
    String TAG = "LogCat TaskIt: ";
    private Context context;
    private Activity activityContext;
    private SharedPreferences sharedPref;
    private String myID, myDepartments, myName;
    private boolean taskMaster;
    private boolean admin;

    private Calendar calendar;
    private int day, month, year, hour, minute;

    private final String fS = "10FXS01";
    private final String newLine = "10NXL01";
    private String dS, hS;

    private ArrayList<String> departmentNames;
    private ArrayList<String> departmentObs;

    private ArrayList<String> usersNames;
    private ArrayList<String> usersIds;
    private ArrayList<String> usersDepartment;
    private ArrayList<Boolean> usersTaskMaster;
    private ArrayList<Boolean> usersAdmin;
    private ArrayList<String> userSubordinates;

    private ArrayList<String> theirTasksTaskId;
    private ArrayList<String> theirTasksTaskMasterId;
    private ArrayList<String> theirTasksTaskerId;
    private ArrayList<String> theirTasksTitle;
    private ArrayList<String> theirTasksDescription;
    private ArrayList<String> theirTasksDeadline;
    private ArrayList<String> theirTasksLastModifiedDateTime;
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

    private boolean showingImage;
    private int selectedTask, selectedAttachment, selectedUser, selectedDepartment;

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
    private final int DEPARTMENTS = 13;
    private final int DEPARTMENT_ADD = 14;
    private final int DEPARTMENT_MANAGEMENT = 15;
    private int currentScreen, lastscreen;

    private TextView appTitle, addUserIdField, addTaskDate, addTaskTime, myTaskTitle, myTaskDescription, myTaskDeadline, myTaskTmComments, theirTasksNameField, theirTasksDate, theirTasksTime, theirTasksTComments, usersManagementId, theirTasksLastModified, changePassCardButtonText, deleteDoneButtonText;
    private RelativeLayout topBar;
    private LinearLayout llTasks, llUsers, llDepartments, llMyTasks, llDeleteDone, llChangePass, mainContainer;
    private LinearLayout loginCard, menuCard, usersCard, changePasswordCard, taskerTrigger, departmentsCard, addDepartmentCard, departmentManagementCard, addUserDepartments, usersManagementDepartments, taskMasterTasksCard, taskerTasksCard;
    private ScrollView addUserCard, theirTasksCard, usersManagementCard, addTaskCard, myTaskCard;
    private ImageButton backButton, taskMasterTaskersCardButton, taskMasterTasksCardButton, taskerTasksCardButton, myTaskAttachment1, myTaskAttachment2, myTaskAttachment1TakePic, myTaskAttachment1DelPic, myTaskAttachment2TakePic, myTaskAttachment2DelPic, theirTasksAttachmentIB1, theirTasksAttachmentIB2, changePassCardButton, deleteDoneButton, menuButton, myTasksReload, theirTasksReload, departmentsCardButton;
    private Button signInButton, addUserCardButton, addUserGenerateIdButton, addUserAddButton, addTaskCardButton, addTaskButton, changePasswordChangeButton, myTaskSaveButton, theirTasksSaveButton, deleteUserButton, updateUserButton, theirTasksTemplateButton, addDepartmentCardButton, addDepartmentAddButton, deleteDepartmentButton, updateDepartmentButton, theirTasksDeleteButton;
    private EditText loginUsernameField, loginPasswordField, addUserNameField, changePasswordOldField, changePasswordNew1Field, changePasswordNew2Field, addTaskTitle, addTaskDescription, myTaskMyComments, theirTasksTitleField, theirTasksDescriptionField, theirTasksMyComments, usersManagementNameField, addDepartmentNameField, addDepartmentObsField, departmentManagementNameField, departmentManagementObsField;
    private CheckBox loginKeep, addUserTaskMaster, addUserAdmin, myTaskDone, theirTasksDone, usersManagementTaskMaster, usersManagementAdmin, showCompleted, addTaskDay1, addTaskDay2, addTaskDay3, addTaskDay4, addTaskDay5, addTaskDay6, addTaskDay7, taskerTasksCardToday, personalTask;
    private Spinner addTaskTaskerSpinner, addTaskNTimes, addUserDepartmentSpinner, usersManagementDepartmentSpinner;
    private ListView usersListView, theirTasksListView, myTasksListView, departmentsListView;
    private ImageView imageShow;
    private TextView loginTitle, taskMasterTaskersCardButtonText, taskMasterTasksCardButtonText, taskerTasksCardButtonText, menuCardName, menuCardId, usersCardTitle, addUserCardTitle, usersManagementCardTitle, myTaskDeadlineTitle, myTaskTmCommentsTitle,
            addTaskCardTitle, repeatTask, times, weekdays, addTaskDay1Text, addTaskDay2Text, addTaskDay3Text, addTaskDay4Text, addTaskDay5Text, addTaskDay6Text, addTaskDay7Text, theirTasksDeadlineText, theirTasksLastModifiedTitle, theirTasksTCommentsTitle, changePasswordCardTitle, departmentsCardButtonText,
            departmentsCardTitle, addDepartmentCardTitle, departmentManagementCardTitle, addUserDepartmentText, usersManagementDepartmentText, addUserDepartmentsText, usersManagementDepartmentsText, theirTasksPicturesText, theirTasksMyCommentsTitle, theirTasksDescriptionText, myTaskPicturesTitle, myTaskMyCommentsTitle, addTaskMyName,
            theirTasksTab1, theirTasksTab2, userTasksTab1, userTasksTab2, addTaskDeadline;
    private String templateName = "";
    private String templateTitle = "";
    private String templateDescription = "";
    private String templateTime = "";

    private Image placeholder, placeholderBig, fallback, fallbackBig, error, errorBig;

    private Typeface font1, font2;

    private ArrayAdapter<String> taskerSpinnerAdapter, departmentsSpinnerAdapter;

    private String[] departmentsArray;

    // network
    private boolean isOnline; //TODO check if is online before calls to server
    private BroadcastReceiver receiver;
    private final String emptyData = "emptyData";
    private final String loginPHP = "https://www.solvaelys.com/taskit/login.php";
    private final String addUserPHP = "https://www.solvaelys.com/taskit/add_user.php";
    private final String addDepartmentPHP = "https://www.solvaelys.com/taskit/add_department.php";
    private final String loadUsersPHP = "https://www.solvaelys.com/taskit/load_users.php";
    private final String loadDepartmentsPHP = "https://www.solvaelys.com/taskit/load_departments.php";
    private final String loadTheirTasksPHP = "https://www.solvaelys.com/taskit/load_their_tasks.php";
    private final String loadMyTasksPHP = "https://www.solvaelys.com/taskit/load_my_tasks.php";
    private final String addTaskPHP = "https://www.solvaelys.com/taskit/add_task.php";
    private final String updateMyTaskPHP = "https://www.solvaelys.com/taskit/update_my_task.php";
    private final String updateTheirTasksPHP = "https://www.solvaelys.com/taskit/update_their_tasks.php";
    private final String changePasswordPHP = "https://www.solvaelys.com/taskit/change_password.php";
    private final String uploadImagePHP = "https://www.solvaelys.com/taskit/upload_image.php";
    private final String updateAttachmentPHP = "https://www.solvaelys.com/taskit/update_attachment.php";
    private final String updateUserPHP = "https://www.solvaelys.com/taskit/update_user.php";
    private final String updateDepartmentPHP = "https://www.solvaelys.com/taskit/update_department.php";
    private final String deleteUserPHP = "https://www.solvaelys.com/taskit/delete_user.php";
    private final String deleteTaskPHP = "https://www.solvaelys.com/taskit/delete_task.php";
    private final String deleteDepartmentPHP = "https://www.solvaelys.com/taskit/delete_department.php";
    private final String deleteDoneTasksPHP = "https://www.solvaelys.com/taskit/delete_done_tasks.php";
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

        activityContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPref = getSharedPreferences("com.awesome.taskit", Context.MODE_PRIVATE);
        myID = sharedPref.getString("myid", "");
        taskMaster = sharedPref.getBoolean("taskmaster", false);
        admin = sharedPref.getBoolean("admin", false);

        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        dS = getString(R.string.date_separator);
        hS = getString(R.string.hour_separator);

        executor = Executors.newSingleThreadExecutor();
        attachmentExecutor1 = Executors.newSingleThreadExecutor();
        attachmentExecutor2 = Executors.newSingleThreadExecutor();

        placeholder = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.downloading)));
        placeholderBig = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.downloading_big)));
        fallback = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.fallback)));
        fallbackBig = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.fallback_big)));
        error = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.error)));
        errorBig = Image_androidKt.asImage(Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.error_big)));

        assignViews();
        assignViewListeners();
        setFonts();

        prepareNetwork();
        prepareCamera();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                topBar.setVisibility(View.VISIBLE);
                mainContainer.setVisibility(View.VISIBLE);

                if (!taskMaster) {
                    personalTask.setEnabled(false);
                }
                if (!admin) {
                    llUsers.setVisibility(View.GONE);
                    llDepartments.setVisibility(View.GONE);
                    llDeleteDone.setVisibility(View.GONE);
                }


                if (myID.isEmpty()) {
                    changeScreen(LOGIN);
                } else if (taskMaster) {
                    loadDepartments();
                    loadUsers();
                    changeScreen(TASK_MASTER_TASKS);
                } else {
                    loadDepartments();
                    loadUsers();
                    changeScreen(TASKER_TASKS);
                }
            }
        }, 100);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backButtonPressed();
            }
        });
    }
    
    // UI

    private void assignViews() {
        topBar = findViewById(R.id.top_bar);
        mainContainer = findViewById(R.id.main_containner);
        menuButton = findViewById(R.id.menu_button);
        appTitle = findViewById(R.id.app_title);
        backButton = findViewById(R.id.back_button);

        loginCard = findViewById(R.id.login_card);
        usersManagementCard = findViewById(R.id.users_management_card);
        menuCard = findViewById(R.id.menu_card);

        llTasks = findViewById(R.id.ll_tasks);
        llUsers = findViewById(R.id.ll_users);
        llDepartments = findViewById(R.id.ll_departments);
        llMyTasks = findViewById(R.id.ll_my_tasks);
        llDeleteDone = findViewById(R.id.ll_delete_done);
        llChangePass = findViewById(R.id.ll_change_pass);

        usersCard = findViewById(R.id.users_card);
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
        departmentsCardButton = findViewById(R.id.departments_card_button);
        departmentsCardButtonText = findViewById(R.id.departments_card_button_text);
        changePassCardButton = findViewById(R.id.change_pass_card_button);
        changePassCardButtonText = findViewById(R.id.change_pass_card_button_text);
        deleteDoneButton = findViewById(R.id.delete_done_button);
        deleteDoneButtonText = findViewById(R.id.delete_done_button_text);

        addUserNameField = findViewById(R.id.add_user_name_field);
        addUserTaskMaster = findViewById(R.id.add_user_task_master);
        addUserAdmin = findViewById(R.id.add_user_admin);
        addUserIdField = findViewById(R.id.add_user_id_field);
        addUserGenerateIdButton = findViewById(R.id.add_user_generate_id_button);
        addUserDepartmentText = findViewById(R.id.add_user_department_text);
        addUserDepartmentSpinner = findViewById(R.id.add_user_department_spinner);
        addUserDepartmentsText = findViewById(R.id.add_user_departments_text);
        addUserDepartments = findViewById(R.id.add_user_departments);
        addUserAddButton = findViewById(R.id.add_user_add_button);

        usersManagementNameField = findViewById(R.id.users_management_name_field);
        usersManagementId = findViewById(R.id.users_management_id);
        usersManagementDepartmentText = findViewById(R.id.users_management_department_text);
        usersManagementDepartmentSpinner = findViewById(R.id.users_management_department_spinner);
        usersManagementTaskMaster = findViewById(R.id.users_management_task_master);
        usersManagementAdmin = findViewById(R.id.users_management_admin);
        usersManagementDepartmentsText = findViewById(R.id.users_management_departments_text);
        usersManagementDepartments = findViewById(R.id.users_management_departments);
        deleteUserButton = findViewById(R.id.delete_user_button);
        updateUserButton = findViewById(R.id.update_user_button);

        departmentsCard = findViewById(R.id.departments_card);
        departmentsCardTitle = findViewById(R.id.departments_card_title);
        addDepartmentCardButton = findViewById(R.id.add_department_card_button);
        departmentsListView = findViewById(R.id.departments_listview);

        addDepartmentCard = findViewById(R.id.add_department_card);
        addDepartmentCardTitle = findViewById(R.id.add_department_card_title);
        addDepartmentNameField = findViewById(R.id.add_department_name_field);
        addDepartmentObsField = findViewById(R.id.add_department_obs_field);
        addDepartmentAddButton = findViewById(R.id.add_department_add_button);

        departmentManagementCard = findViewById(R.id.department_management_card);
        departmentManagementCardTitle = findViewById(R.id.department_management_card_title);
        departmentManagementNameField = findViewById(R.id.department_management_name_field);
        departmentManagementObsField = findViewById(R.id.department_management_obs_field);
        deleteDepartmentButton = findViewById(R.id.delete_department_button);
        updateDepartmentButton = findViewById(R.id.update_department_button);

        addTaskCard = findViewById(R.id.add_task_card);
        theirTasksReload = findViewById(R.id.their_tasks_reload);
        theirTasksListView = findViewById(R.id.their_tasks_listview);
        addTaskCardButton = findViewById(R.id.add_task_card_button);
        showCompleted = findViewById(R.id.show_completed);

        theirTasksNameField = findViewById(R.id.their_tasks_name);
        theirTasksTitleField = findViewById(R.id.their_tasks_title);
        theirTasksDescriptionText = findViewById(R.id.their_tasks_description_text);
        theirTasksDescriptionField = findViewById(R.id.their_tasks_description);
        theirTasksDate = findViewById(R.id.their_tasks_date);
        theirTasksTime = findViewById(R.id.their_tasks_time);
        theirTasksLastModified = findViewById(R.id.their_tasks_last_modified);
        theirTasksPicturesText = findViewById(R.id.their_tasks_pictures_text);
        theirTasksAttachmentIB1 = findViewById(R.id.their_tasks_attachment_ib_1);
        theirTasksAttachmentIB2 = findViewById(R.id.their_tasks_attachment_ib_2);
        theirTasksTComments = findViewById(R.id.their_tasks_t_comments);
        theirTasksMyCommentsTitle = findViewById(R.id.their_tasks_my_comments_title);
        theirTasksMyComments = findViewById(R.id.their_tasks_my_comments);
        theirTasksDone = findViewById(R.id.their_tasks_done);
        theirTasksTemplateButton = findViewById(R.id.their_tasks_template_button);
        theirTasksDeleteButton = findViewById(R.id.their_tasks_delete_button);
        theirTasksSaveButton = findViewById(R.id.their_tasks_save_button);

        personalTask = findViewById(R.id.personal_task);
        addTaskTaskerSpinner = findViewById(R.id.add_task_tasker_spinner);
        addTaskMyName = findViewById(R.id.add_task_my_name);
        addTaskTitle = findViewById(R.id.add_task_title);
        addTaskDescription = findViewById(R.id.add_task_description);
        addTaskDeadline = findViewById(R.id.add_task_deadline);
        addTaskDate = findViewById(R.id.add_task_date);
        addTaskTime = findViewById(R.id.add_task_time);
        addTaskNTimes = findViewById(R.id.add_task_n_times);
        addTaskDay1 = findViewById(R.id.add_task_day_1);
        addTaskDay2 = findViewById(R.id.add_task_day_2);
        addTaskDay3 = findViewById(R.id.add_task_day_3);
        addTaskDay4 = findViewById(R.id.add_task_day_4);
        addTaskDay5 = findViewById(R.id.add_task_day_5);
        addTaskDay6 = findViewById(R.id.add_task_day_6);
        addTaskDay7 = findViewById(R.id.add_task_day_7);
        addTaskButton = findViewById(R.id.add_one_task_button);

        myTaskTitle = findViewById(R.id.my_task_title);
        myTaskDescription = findViewById(R.id.my_task_description);
        myTaskDeadline = findViewById(R.id.my_task_deadline);
        myTaskPicturesTitle = findViewById(R.id.my_task_pictures_title);
        myTaskAttachment1 = findViewById(R.id.my_task_attachment_1);
        myTaskAttachment1TakePic = findViewById(R.id.my_task_attachment_1_take_pic);
        myTaskAttachment1DelPic = findViewById(R.id.my_task_attachment_1_del_pic);
        myTaskAttachment2 = findViewById(R.id.my_task_attachment_2);
        myTaskAttachment2TakePic = findViewById(R.id.my_task_attachment_2_take_pic);
        myTaskAttachment2DelPic = findViewById(R.id.my_task_attachment_2_del_pic);
        myTaskMyCommentsTitle = findViewById(R.id.my_task_my_comments_title);
        myTaskMyComments = findViewById(R.id.my_task_my_comments);
        myTaskTmComments = findViewById(R.id.my_task_tm_comments);
        myTaskDone = findViewById(R.id.my_task_done);
        myTaskSaveButton = findViewById(R.id.my_task_save_button);

        taskerTasksCardButton = findViewById(R.id.tasker_tasks_card_button);
        taskerTasksCardToday = findViewById(R.id.tasker_tasks_card_today);
        myTasksReload = findViewById(R.id.my_tasks_reload);
        myTasksListView = findViewById(R.id.my_tasks_listview);

        usersListView = findViewById(R.id.users_listview);

        changePasswordOldField = findViewById(R.id.change_password_old_field);
        changePasswordNew1Field = findViewById(R.id.change_password_new1_field);
        changePasswordNew2Field = findViewById(R.id.change_password_new2_field);
        changePasswordChangeButton = findViewById(R.id.change_password_change_button);

        imageShow = findViewById(R.id.image_show);

        loginTitle = findViewById(R.id.login_title);
        taskMasterTaskersCardButtonText = findViewById(R.id.task_master_taskers_card_button_text);
        taskMasterTasksCardButtonText = findViewById(R.id.task_master_tasks_card_button_text);
        taskerTasksCardButtonText = findViewById(R.id.tasker_tasks_card_button_text);
        menuCardName = findViewById(R.id.menu_card_name);
        menuCardId = findViewById(R.id.menu_card_id);
        usersCardTitle = findViewById(R.id.users_card_title);
        addUserCardTitle = findViewById(R.id.add_user_card_title);
        usersManagementCardTitle = findViewById(R.id.users_management_card_title);
        myTaskDeadlineTitle = findViewById(R.id.my_task_deadline_title);
        myTaskTmCommentsTitle = findViewById(R.id.my_task_tm_comments_title);
        addTaskCardTitle = findViewById(R.id.add_task_card_title);
        repeatTask = findViewById(R.id.repeat_task);
        times = findViewById(R.id.times);
        weekdays = findViewById(R.id.weekdays);
        addTaskDay1Text = findViewById(R.id.add_task_day_1_text);
        addTaskDay2Text = findViewById(R.id.add_task_day_2_text);
        addTaskDay3Text = findViewById(R.id.add_task_day_3_text);
        addTaskDay4Text = findViewById(R.id.add_task_day_4_text);
        addTaskDay5Text = findViewById(R.id.add_task_day_5_text);
        addTaskDay6Text = findViewById(R.id.add_task_day_6_text);
        addTaskDay7Text = findViewById(R.id.add_task_day_7_text);
        theirTasksDeadlineText = findViewById(R.id.their_tasks_deadline);
        theirTasksLastModifiedTitle = findViewById(R.id.their_tasks_last_modified_title);
        theirTasksTCommentsTitle = findViewById(R.id.their_tasks_t_comments_title);
        changePasswordCardTitle = findViewById(R.id.change_password_card_title);

        theirTasksTab1 = findViewById(R.id.their_tasks_tab1);
        theirTasksTab2 = findViewById(R.id.their_tasks_tab2);
        userTasksTab1 = findViewById(R.id.users_tasks_tab1);
        userTasksTab2 = findViewById(R.id.users_tasks_tab2);
    }

    private void assignViewListeners() {
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentScreen == MAIN_MENU) {
                    menuButton.setImageResource(R.drawable.menu);
                    changeScreen(lastscreen);
                } else {
                    changeScreen(MAIN_MENU);
                }
            }
        });
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

        // menu
        taskMasterTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKS);
            }
        });
        taskMasterTasksCardButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKS);
            }
        });

        taskMasterTaskersCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKERS);
            }
        });
        taskMasterTaskersCardButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKERS);
            }
        });

        taskerTasksCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_TASKS);
            }
        });
        taskerTasksCardButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_TASKS);
            }
        });

        departmentsCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(DEPARTMENTS);
            }
        });
        departmentsCardButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(DEPARTMENTS);
            }
        });

        deleteDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteDoneTasks();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setMessage(getString(R.string.delete_done_tasks))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show();

                TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
                Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
                Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

                message.setTypeface(font1);
                b1.setTypeface(font1);
                b2.setTypeface(font1);

                //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

                b1.setTextColor(getColor(R.color.yes));
                b2.setTextColor(getColor(R.color.no));
            }
        });
        deleteDoneButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteDoneTasks();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setMessage(getString(R.string.delete_done_tasks))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show();

                TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
                Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
                Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

                message.setTypeface(font1);
                b1.setTypeface(font1);
                b2.setTypeface(font1);

                //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

                b1.setTextColor(getColor(R.color.yes));
                b2.setTextColor(getColor(R.color.no));
            }
        });

        changePassCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(CHANGE_PASSWORD);
            }
        });
        changePassCardButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(CHANGE_PASSWORD);
            }
        });

        addUserCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(USER_ADD);
            }
        });

        addUserGenerateIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserIdField.setText(generateID());
            }
        });
        addUserTaskMaster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    addUserDepartmentsText.setVisibility(View.VISIBLE);
                    addUserDepartments.setVisibility(View.VISIBLE);
                } else {
                    addUserDepartmentsText.setVisibility(View.GONE);
                    addUserDepartments.setVisibility(View.GONE);
                }
            }
        });
        addUserAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });

        usersManagementTaskMaster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    usersManagementDepartmentsText.setVisibility(View.VISIBLE);
                    usersManagementDepartments.setVisibility(View.VISIBLE);
                } else {
                    usersManagementDepartmentsText.setVisibility(View.GONE);
                    usersManagementDepartments.setVisibility(View.GONE);
                }
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

        showCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                showCompleted.setEnabled(false);
                loadTheirTasks();
            }
        });

        personalTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    addTaskTaskerSpinner.setVisibility(View.GONE);
                    addTaskMyName.setVisibility(View.VISIBLE);
                    addTaskMyName.setText(myName);
                } else {
                    addTaskTaskerSpinner.setVisibility(View.VISIBLE);
                    addTaskMyName.setVisibility(View.GONE);
                }
            }
        });

        addTaskCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_NEW_TASK);
            }
        });

        theirTasksReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theirTasksReload.setEnabled(false);
                theirTasksReload.setImageResource(R.drawable.reload_disabled);
                theirTasksListView.setVisibility(View.VISIBLE);
                loadTheirTasks();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        theirTasksReload.setEnabled(true);
                        theirTasksReload.setImageResource(R.drawable.reload);
                    }
                }, 5000);

            }
        });

        myTasksReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTasksReload.setEnabled(false);
                myTasksReload.setImageResource(R.drawable.reload_disabled);
                myTasksListView.setVisibility(View.VISIBLE);
                loadMyTasks();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myTasksReload.setEnabled(true);
                        myTasksReload.setImageResource(R.drawable.reload);
                    }
                }, 5000);

            }
        });
        taskerTasksCardToday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                taskerTasksCardToday.setEnabled(false);
                loadMyTasks();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskerTasksCardToday.setEnabled(true);
                    }
                }, 2000);
            }
        });

        addTaskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dpdYear, int dpdMonthOfYear, int dpdDayOfMonth) {
                        day = dpdDayOfMonth;
                        month = dpdMonthOfYear + 1;
                        year = dpdYear;
                        addTaskDate.setText(day + dS + month + dS + year);
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int tpdHourOfDay, int tpdMinute) {
                        hour = tpdHourOfDay;
                        minute = tpdMinute;
                        addTaskTime.setText(hour + hS + String.format("%02d", minute));
                        calendar.set(Calendar.HOUR_OF_DAY, tpdHourOfDay);
                        calendar.set(Calendar.MINUTE, tpdMinute);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dpdYear, int dpdMonthOfYear, int dpdDayOfMonth) {
                        day = dpdDayOfMonth;
                        month = dpdMonthOfYear + 1;
                        year = dpdYear;
                        theirTasksDate.setText(day + dS + month + dS + year);
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int tpdHourOfDay, int tpdMinute) {
                        hour = tpdHourOfDay;
                        minute = tpdMinute;
                        theirTasksTime.setText(hour + hS + String.format("%02d", minute));
                        calendar.set(Calendar.HOUR_OF_DAY, tpdHourOfDay);
                        calendar.set(Calendar.MINUTE, tpdMinute);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });
        theirTasksTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useAsTemplate();
            }
        });
        theirTasksDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteTask();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setMessage(getString(R.string.delete_task_q))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show();

                TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
                Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
                Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

                message.setTypeface(font1);
                b1.setTypeface(font1);
                b2.setTypeface(font1);

                //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
                //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

                b1.setTextColor(getColor(R.color.yes));
                b2.setTextColor(getColor(R.color.no));
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
                if (hasCameraPermission()) {
                    openCamera();
                } else {
                    checkCameraPermission();
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
                                    contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "0"));
                                    populateMyTaskCard(selectedTask);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setMessage(getString(R.string.delete_picture))
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener)
                            .show();

                    TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
                    Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
                    Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

                    message.setTypeface(font1);
                    b1.setTypeface(font1);
                    b2.setTypeface(font1);

                    //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
                    //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
                    //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

                    b1.setTextColor(getColor(R.color.yes));
                    b2.setTextColor(getColor(R.color.no));
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
                if (hasCameraPermission()) {
                    openCamera();
                } else {
                    checkCameraPermission();
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
                                    contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "0"));
                                    populateMyTaskCard(selectedTask);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setMessage(getString(R.string.delete_picture))
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener)
                            .show();

                    TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
                    Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
                    Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

                    message.setTypeface(font1);
                    b1.setTypeface(font1);
                    b2.setTypeface(font1);

                    //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
                    //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
                    //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

                    b1.setTextColor(getColor(R.color.yes));
                    b2.setTextColor(getColor(R.color.no));
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

        addDepartmentCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(DEPARTMENT_ADD);
            }
        });
        addDepartmentAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDepartment();
            }
        });

        deleteDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDepartment();
            }
        });
        updateDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDepartment();
            }
        });

        theirTasksTab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASKER_TASKS);
            }
        });
        userTasksTab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreen(TASK_MASTER_TASKS);
            }
        });
    }

    private void setFonts() {
        font1 = ResourcesCompat.getFont(this, R.font.roboto_medium);
        font2 = ResourcesCompat.getFont(this, R.font.roboto_light);

        appTitle.setTypeface(font1);
        loginTitle.setTypeface(font1);
        loginUsernameField.setTypeface(font2);
        loginPasswordField.setTypeface(font2);
        loginKeep.setTypeface(font1);
        signInButton.setTypeface(font1);

        taskMasterTaskersCardButtonText.setTypeface(font1);
        departmentsCardButtonText.setTypeface(font1);
        taskMasterTasksCardButtonText.setTypeface(font1);

        taskerTasksCardButtonText.setTypeface(font1);

        menuCardName.setTypeface(font1);
        menuCardId.setTypeface(font2);
        changePassCardButtonText.setTypeface(font1);
        deleteDoneButtonText.setTypeface(font1);

        usersCardTitle.setTypeface(font1);
        addUserCardButton.setTypeface(font1);

        addUserCardTitle.setTypeface(font1);
        addUserNameField.setTypeface(font2);
        addUserIdField.setTypeface(font2);
        addUserGenerateIdButton.setTypeface(font1);
        addUserTaskMaster.setTypeface(font2);
        addUserAdmin.setTypeface(font2);
        addUserDepartmentText.setTypeface(font1);
        addUserDepartmentsText.setTypeface(font1);
        addUserAddButton.setTypeface(font1);

        usersManagementCardTitle.setTypeface(font1);
        usersManagementNameField.setTypeface(font2);
        usersManagementId.setTypeface(font2);
        usersManagementDepartmentText.setTypeface(font1);
        usersManagementTaskMaster.setTypeface(font2);
        usersManagementAdmin.setTypeface(font2);
        usersManagementDepartmentsText.setTypeface(font1);
        deleteUserButton.setTypeface(font1);
        updateUserButton.setTypeface(font1);

        departmentsCardTitle.setTypeface(font1);
        addDepartmentCardButton.setTypeface(font1);

        addDepartmentCardTitle.setTypeface(font1);
        addDepartmentNameField.setTypeface(font2);
        addDepartmentObsField.setTypeface(font2);
        addDepartmentAddButton.setTypeface(font1);

        departmentManagementCardTitle.setTypeface(font1);
        departmentManagementNameField.setTypeface(font2);
        departmentManagementObsField.setTypeface(font2);
        deleteDepartmentButton.setTypeface(font1);
        updateDepartmentButton.setTypeface(font1);

        addTaskCardButton.setTypeface(font1);
        showCompleted.setTypeface(font2);

        taskerTasksCardToday.setTypeface(font2);

        myTaskTitle.setTypeface(font1);
        myTaskDescription.setTypeface(font2);
        myTaskDeadlineTitle.setTypeface(font1);
        myTaskDeadline.setTypeface(font1);
        myTaskPicturesTitle.setTypeface(font1);
        myTaskMyCommentsTitle.setTypeface(font1);
        myTaskMyComments.setTypeface(font2);
        myTaskTmCommentsTitle.setTypeface(font1);
        myTaskTmComments.setTypeface(font2);
        myTaskDone.setTypeface(font2);
        myTaskSaveButton.setTypeface(font1);

        addTaskCardTitle.setTypeface(font1);
        personalTask.setTypeface(font2);
        // TODO set typeface of spinner addTaskTaskerSpinner
        addTaskMyName.setTypeface(font2);
        addTaskTitle.setTypeface(font2);
        addTaskDescription.setTypeface(font2);
        addTaskDate.setTypeface(font2);
        addTaskTime.setTypeface(font2);
        repeatTask.setTypeface(font1);
        // TODO set typeface of spinner addTaskNTimes
        times.setTypeface(font1);
        addTaskDeadline.setTypeface(font1);
        weekdays.setTypeface(font1);
        addTaskDay1Text.setTypeface(font1);
        addTaskDay2Text.setTypeface(font1);
        addTaskDay3Text.setTypeface(font1);
        addTaskDay4Text.setTypeface(font1);
        addTaskDay5Text.setTypeface(font1);
        addTaskDay6Text.setTypeface(font1);
        addTaskDay7Text.setTypeface(font1);
        addTaskButton.setTypeface(font1);

        theirTasksNameField.setTypeface(font1);
        theirTasksTitleField.setTypeface(font2);
        theirTasksDescriptionText.setTypeface(font1);
        theirTasksDescriptionField.setTypeface(font2);
        theirTasksDeadlineText.setTypeface(font1);
        theirTasksDate.setTypeface(font2);
        theirTasksTime.setTypeface(font2);
        theirTasksLastModifiedTitle.setTypeface(font1);
        theirTasksLastModified.setTypeface(font2);
        theirTasksPicturesText.setTypeface(font1);
        theirTasksMyCommentsTitle.setTypeface(font1);
        theirTasksTCommentsTitle.setTypeface(font1);
        theirTasksTComments.setTypeface(font2);
        theirTasksMyComments.setTypeface(font2);
        theirTasksDone.setTypeface(font2);
        theirTasksTemplateButton.setTypeface(font1);
        theirTasksSaveButton.setTypeface(font1);

        changePasswordCardTitle.setTypeface(font1);
        changePasswordOldField.setTypeface(font2);
        changePasswordNew1Field.setTypeface(font2);
        changePasswordNew2Field.setTypeface(font2);
        changePasswordChangeButton.setTypeface(font1);

        theirTasksTab1.setTypeface(font1);
        theirTasksTab2.setTypeface(font1);
        userTasksTab1.setTypeface(font1);
        userTasksTab2.setTypeface(font1);
    }

    private void changeScreen(int newScreen) {
        hideAllScreens();
        switch (newScreen) {
            case LOGIN:
                loginCard.setVisibility(View.VISIBLE);
                loginUsernameField.setText("");
                loginPasswordField.setText("");
                loginKeep.setChecked(false);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("myid", "");
                editor.putBoolean("taskmaster", false);
                editor.putBoolean("admin", false);
                editor.apply();
                taskMaster = false;
                admin = false;
                break;
            case MAIN_MENU:
                menuButton.setImageResource(R.drawable.menu_opened);
                menuCardName.setText(myName);
                menuCardId.setText(myID);
                if (admin) {
                    addUserCardButton.setVisibility(View.VISIBLE);
                    deleteDoneButton.setVisibility(View.VISIBLE);
                    deleteDoneButtonText.setVisibility(View.VISIBLE);
                } else {
                    addUserCardButton.setVisibility(View.GONE);
                    deleteDoneButton.setVisibility(View.GONE);
                    deleteDoneButtonText.setVisibility(View.GONE);
                }
                menuCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKERS:
                usersCard.setVisibility(View.VISIBLE);
                break;
            case TASKER_MANAGEMENT:
                usersManagementCard.setVisibility(View.VISIBLE);
                break;
            case DEPARTMENTS:
                departmentsCard.setVisibility(View.VISIBLE);
                break;
            case DEPARTMENT_ADD:
                addDepartmentCard.setVisibility(View.VISIBLE);
                addDepartmentNameField.setText("");
                addDepartmentObsField.setText("");
                break;
            case DEPARTMENT_MANAGEMENT:
                departmentManagementCard.setVisibility(View.VISIBLE);
                break;
            case TASK_MASTER_TASKS:
                loadTheirTasks();
                taskMasterTasksCard.setVisibility(View.VISIBLE);
                break;
            case TASKER_TASKS:
                loadMyTasks();
                taskerTasksCard.setVisibility(View.VISIBLE);
                break;
            case USER_ADD:
                addUserCard.setVisibility(View.VISIBLE);
                addUserNameField.setText("");
                addUserIdField.setText(generateID());
                addUserTaskMaster.setChecked(false);
                addUserAdmin.setChecked(false);
                addUserDepartmentsText.setVisibility(View.GONE);
                addUserDepartments.setVisibility(View.GONE);
                break;
            case TASK_MASTER_NEW_TASK:
                addTaskCard.setVisibility(View.VISIBLE);

                if (!taskMaster) {
                    personalTask.setChecked(true);
                }

                if (personalTask.isChecked()) {
                    addTaskTaskerSpinner.setVisibility(View.GONE);
                    addTaskMyName.setVisibility(View.VISIBLE);
                    addTaskMyName.setText(myName);
                } else {
                    addTaskTaskerSpinner.setVisibility(View.VISIBLE);
                    addTaskMyName.setVisibility(View.GONE);
                }

                if (!templateName.isEmpty()) addTaskTaskerSpinner.setSelection(taskerSpinnerAdapter.getPosition(templateName));
                if (!templateTitle.isEmpty()) {
                    addTaskTitle.setText(templateTitle);
                } else {
                    addTaskTitle.setText("");
                }
                if (!templateDescription.isEmpty()) {
                    addTaskDescription.setText(templateDescription);
                } else {
                    addTaskDescription.setText("");
                }

                calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH) + 1;
                year = calendar.get(Calendar.YEAR);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);

                addTaskDate.setText(day + dS + month + dS + year);
                if (!templateTime.isEmpty()) {
                    hour = Integer.parseInt(templateTime.substring(0, 2));
                    minute = Integer.parseInt(templateTime.substring(3));
                }
                addTaskTime.setText(hour + hS + String.format("%02d", minute));

                int n = 7;
                String[] items = new String[n];
                for (int i = 0; i < n; i ++) {
                    items[i] =  "  " + (i + 1) + "  ";
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
                addTaskNTimes.setAdapter(adapter);
                addTaskNTimes.setSelection(0);

                addTaskDay1.setChecked(true);
                addTaskDay2.setChecked(true);
                addTaskDay3.setChecked(true);
                addTaskDay4.setChecked(true);
                addTaskDay5.setChecked(true);
                addTaskDay6.setChecked(true);
                addTaskDay7.setChecked(true);
                break;
            case CHANGE_PASSWORD:
                changePasswordCard.setVisibility(View.VISIBLE);
                changePasswordOldField.setText("");
                changePasswordNew1Field.setText("");
                changePasswordNew2Field.setText("");
                break;
            case MY_TASK:
                myTaskCard.setVisibility(View.VISIBLE);
                if (!showingImage) {
                    populateMyTaskCard(selectedTask);
                } else {
                    showingImage = false;
                }
                break;
            case THEIR_TASKS:
                theirTasksCard.setVisibility(View.VISIBLE);
                if (!showingImage) {
                    populateTheirTasksCard(selectedTask);
                } else {
                    showingImage = false;
                }
                break;
            case MY_TASK_IMAGE_SHOW:
                imageShow.setVisibility(View.VISIBLE);
                showingImage = true;
                break;
            case THEIR_TASKS_IMAGE_SHOW:
                imageShow.setVisibility(View.VISIBLE);
                showingImage = true;
                break;
        }
        if (currentScreen == MAIN_MENU){
            menuButton.setImageResource(R.drawable.menu);
        } else {
            lastscreen = currentScreen;
        }
        currentScreen = newScreen;
    }

    private void backButtonPressed() {
        hideKeyboard();
        switch (currentScreen) {
            case LOGIN:
                askExit();
                break;
            case MAIN_MENU:
                menuButton.setImageResource(R.drawable.menu);
                changeScreen(lastscreen);
                break;
            case TASK_MASTER_TASKERS:
            case DEPARTMENTS:
            case TASK_MASTER_TASKS:
            case TASKER_TASKS:
                askExitOrLogout();
                break;
            case CHANGE_PASSWORD:
                changeScreen(lastscreen);
                break;
            case USER_ADD:
            case TASKER_MANAGEMENT:
                changeScreen(TASK_MASTER_TASKERS);
                break;
            case DEPARTMENT_ADD:
            case DEPARTMENT_MANAGEMENT:
                changeScreen(DEPARTMENTS);
                break;
            case TASK_MASTER_NEW_TASK:
            case THEIR_TASKS:
                templateName = "";
                templateTitle = "";
                templateDescription = "";
                templateTime = "";
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

    private void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            //System.out.println(TAG + "{hideKeyboard} " + e.getMessage());
        }
    }

    private void showKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            System.out.println(TAG + "{showNoteInput} " + e.getMessage());
        }
    }

    private void hideAllScreens() {
        loginCard.setVisibility(View.GONE);
        menuCard.setVisibility(View.GONE);
        usersCard.setVisibility(View.GONE);
        departmentsCard.setVisibility(View.GONE);
        addDepartmentCard.setVisibility(View.GONE);
        departmentManagementCard.setVisibility(View.GONE);
        taskMasterTasksCard.setVisibility(View.GONE);
        taskerTasksCard.setVisibility(View.GONE);
        changePasswordCard.setVisibility(View.GONE);
        addUserCard.setVisibility(View.GONE);
        usersManagementCard.setVisibility(View.GONE);
        addTaskCard.setVisibility(View.GONE);
        theirTasksCard.setVisibility(View.GONE);
        myTaskCard.setVisibility(View.GONE);
        imageShow.setVisibility(View.GONE);
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
        hideKeyboard();
        signInButton.setEnabled(false);
        if (!loginUsernameField.getText().toString().isEmpty()) {
            String nameTemp = loginUsernameField.getText().toString().replaceAll("\\s+$", "");
            String rawData = nameTemp + fS + loginPasswordField.getText().toString();
            String response = contactServer(loginPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            if (response.contains("Login successful")) {
                String[] resp = response.split(newLine);
                String[] temp = resp[0].split(fS);
                myID = temp[1];
                loadDepartments();
                loadUsers();
                if (temp[2].contains("0")) {
                    taskMaster = false;
                    personalTask.setEnabled(false);
                } else {
                    taskMaster = true;
                    personalTask.setEnabled(true);
                }
                if (temp[3].contains("0")) {
                    admin = false;
                    llUsers.setVisibility(View.GONE);
                    llDepartments.setVisibility(View.GONE);
                    llDeleteDone.setVisibility(View.GONE);
                } else {
                    admin = true;
                    llUsers.setVisibility(View.VISIBLE);
                    llDepartments.setVisibility(View.VISIBLE);
                    llDeleteDone.setVisibility(View.VISIBLE);
                }
                signInButton.setEnabled(true);
                if (loginKeep.isChecked()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("myid", myID);
                    editor.putBoolean("taskmaster", taskMaster);
                    editor.putBoolean("admin", admin);
                    editor.apply();
                }

                taskerTasksCardToday.setChecked(false);

                if (taskMaster) {
                    changeScreen(TASK_MASTER_TASKS);
                } else {
                    changeScreen(TASKER_TASKS);
                }
            } else {
                Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signInButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            Toast.makeText(context, getString(R.string.error_username_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    signInButton.setEnabled(true);
                }
            }, 3000);
        }
    }

    private void addUser() {
        int taskMasterCB = 0;
        int adminCB = 0;
        if (addUserTaskMaster.isChecked()) taskMasterCB = 1;
        if (addUserAdmin.isChecked()) adminCB = 1;

        addUserAddButton.setEnabled(false);
        if (!addUserNameField.getText().toString().isEmpty()) {

            String tempSubordinates = "";
            if (addUserTaskMaster.isChecked()) {
                for (int i = 0; i < addUserDepartments.getChildCount(); i++) {
                    View view = addUserDepartments.getChildAt(i);
                    if (view instanceof CheckBox) {
                        CheckBox cb = (CheckBox) view;
                        if (cb.isChecked()) {
                            tempSubordinates += cb.getText().toString() + ",";
                        }
                    }
                }
            }
            if (tempSubordinates.endsWith(",")) tempSubordinates = tempSubordinates.substring(0, tempSubordinates.length() - 1);
            if (tempSubordinates.isEmpty()) tempSubordinates = " ";

            if ((addUserTaskMaster.isChecked() && !tempSubordinates.equals(" ")) ||
                    !addUserTaskMaster.isChecked()) {
                String rawData = addUserNameField.getText().toString() + fS + addUserIdField.getText().toString() + fS + taskMasterCB + fS + adminCB + fS + addUserDepartmentSpinner.getSelectedItem().toString() + fS + tempSubordinates;
                String response = contactServer(addUserPHP, Java_AES_Cipher.encryptSimple(rawData));
                response = response.replaceAll(newLine, "\n");
                if (response.contains("New record created successfully")) {
                    Toast.makeText(context, getString(R.string.user_created), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addUserAddButton.setEnabled(true);
                            loadUsers();
                            changeScreen(TASK_MASTER_TASKERS);
                        }
                    }, 1500);
                } else if (response.contains("User name already registered")) {
                    Toast.makeText(context, getString(R.string.user_exists), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addUserAddButton.setEnabled(true);
                        }
                    }, 2000);
                } else if (response.contains("User ID already exists")) {
                    Toast.makeText(context, getString(R.string.user_id_exists), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addUserAddButton.setEnabled(true);
                        }
                    }, 2000);
                }
            } else {
                Toast.makeText(context, getString(R.string.error_no_department_selected), Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addUserAddButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            Toast.makeText(context, getString(R.string.error_username_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addUserAddButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void addDepartment() {
        addDepartmentAddButton.setEnabled(false);
        String tempObs = "";
        if (addDepartmentObsField.getText().toString().isEmpty()) {
            tempObs = " ";
        } else {
            tempObs = addDepartmentObsField.getText().toString();
        }
        if (!addDepartmentNameField.getText().toString().isEmpty()) {
            String rawData = addDepartmentNameField.getText().toString() + fS + tempObs;
            String response = contactServer(addDepartmentPHP, Java_AES_Cipher.encryptSimple(rawData));

            if (response.contains("New record created successfully")) {
                Toast.makeText(context, getString(R.string.department_created), Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadDepartments();
                        changeScreen(DEPARTMENTS);
                    }
                }, 1500);
            } else if (response.contains("Department name already registered")) {
                Toast.makeText(context, getString(R.string.department_exists), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, getString(R.string.error_department_empty), Toast.LENGTH_LONG).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addDepartmentAddButton.setEnabled(true);
            }
        }, 2000);
    }

    private void loadTheirTasks() {
        theirTasksTaskId = new ArrayList<>();
        theirTasksTaskMasterId = new ArrayList<>();
        theirTasksTaskerId = new ArrayList<>();
        theirTasksTitle = new ArrayList<>();
        theirTasksDescription = new ArrayList<>();
        theirTasksDeadline = new ArrayList<>();
        theirTasksLastModifiedDateTime = new ArrayList<>();
        theirTasksTaskerMarkedAsDone = new ArrayList<>();
        theirTasksAttachment1 = new ArrayList<>();
        theirTasksAttachment2 = new ArrayList<>();
        theirTasksTaskerComment = new ArrayList<>();
        theirTasksTaskMasterComment = new ArrayList<>();
        theirTasksTaskMasterMarkedAsDone = new ArrayList<>();
        String response = contactServer(loadTheirTasksPHP, Java_AES_Cipher.encryptSimple(emptyData));
        if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
            String[] lines = response.split(newLine);
            String[] line;
            String[] tempDepartments = myDepartments.split(",");
            int tempPosition;
            boolean tempoDeptFound;

            for (int i = 0; i < lines.length; i ++) {
                line = lines[i].split(fS);
                tempoDeptFound = false;
                if (showCompleted.isChecked()) {
                    if (line.length == 13) {
                        tempPosition = usersIds.indexOf(line[1]);
                        for (int j = 0; j < tempDepartments.length; j ++) {
                            if (usersDepartment.get(tempPosition).contains(tempDepartments[j]) && myDepartments.length() > 1) {
                                tempoDeptFound = true;
                            }
                        }
                        if ( (line[12].equals(line[1]) && myID.equals(line[12])) || (tempoDeptFound && !line[1].equals(line[12]) && !myID.equals(line[1])) ) {
                            theirTasksTaskId.add(line[0]);
                            theirTasksTaskerId.add(line[1]);
                            theirTasksTitle.add(line[2]);
                            theirTasksDescription.add(line[3]);
                            theirTasksDeadline.add(line[4]);
                            if (line[5].contains("0")) {
                                theirTasksTaskerMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskerMarkedAsDone.add(true);
                            }
                            if (line[6].contains("0")) {
                                theirTasksAttachment1.add(false);
                            } else {
                                theirTasksAttachment1.add(true);
                            }
                            if (line[7].contains("0")) {
                                theirTasksAttachment2.add(false);
                            } else {
                                theirTasksAttachment2.add(true);
                            }
                            theirTasksTaskerComment.add(line[8]);
                            theirTasksTaskMasterComment.add(line[9]);

                            if (line[10].contains("0")) {
                                theirTasksTaskMasterMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskMasterMarkedAsDone.add(true);
                            }

                            theirTasksLastModifiedDateTime.add(line[11]);
                            theirTasksTaskMasterId.add(line[12]);
                        }
                    }
                } else {
                    if (line.length == 13 && line[10].contains("0")) {
                        tempPosition = usersIds.indexOf(line[1]);
                        for (int j = 0; j < tempDepartments.length; j ++) {
                            if (usersDepartment.get(tempPosition).contains(tempDepartments[j]) && myDepartments.length() > 1) {
                                tempoDeptFound = true;
                            }
                        }
                        if ( (line[12].equals(line[1]) && myID.equals(line[12])) || (tempoDeptFound && !line[1].equals(line[12]) && !myID.equals(line[1])) ) {
                            theirTasksTaskId.add(line[0]);
                            theirTasksTaskerId.add(line[1]);
                            theirTasksTitle.add(line[2]);
                            theirTasksDescription.add(line[3]);
                            theirTasksDeadline.add(line[4]);
                            if (line[5].contains("0")) {
                                theirTasksTaskerMarkedAsDone.add(false);
                            } else {
                                theirTasksTaskerMarkedAsDone.add(true);
                            }
                            if (line[6].contains("0")) {
                                theirTasksAttachment1.add(false);
                            } else {
                                theirTasksAttachment1.add(true);
                            }
                            if (line[7].contains("0")) {
                                theirTasksAttachment2.add(false);
                            } else {
                                theirTasksAttachment2.add(true);
                            }
                            theirTasksTaskerComment.add(line[8]);
                            theirTasksTaskMasterComment.add(line[9]);

                            theirTasksTaskMasterMarkedAsDone.add(false);

                            theirTasksLastModifiedDateTime.add(line[11]);
                            theirTasksTaskMasterId.add(line[12]);
                        }
                    }
                }
            }

            ArrayList<String> tasksTaskerName = new ArrayList<>();
            for (int i = 0; i < theirTasksTaskerId.size(); i ++) {
                tasksTaskerName.add(usersNames.get(usersIds.indexOf(theirTasksTaskerId.get(i))));
            }

            TheirTasksListAdapter theirTasksListAdapter = new TheirTasksListAdapter(activityContext, tasksTaskerName, theirTasksTitle, theirTasksDeadline, theirTasksTaskerMarkedAsDone);
            theirTasksListAdapter.notifyDataSetChanged();
            theirTasksListView.setAdapter(theirTasksListAdapter);
            theirTasksListView.setDivider(null);

            if (theirTasksTitle.isEmpty()) {
                theirTasksListView.setVisibility(View.GONE);
            } else {
                theirTasksListView.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCompleted.setEnabled(true);
            }
        }, 2000);
    }

    private void loadMyTasks() {
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
                        if ( line.length == 11 && line[10].equals("0") &&
                                ( line[5].equals("0") || !myID.equals(line[1]) ) &&
                                ( !taskerTasksCardToday.isChecked() || isToday(line[4]) ) ) {
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

                            myTasksTaskMasterMarkedAsDone.add(false);
                        }
                    }

                    MyTasksListAdapter myTasksListAdapter = new MyTasksListAdapter(activityContext, myTasksTitle, myTasksDeadline, myTasksTaskerMarkedAsDone);
                    myTasksListAdapter.notifyDataSetChanged();
                    myTasksListView.setAdapter(myTasksListAdapter);
                    myTasksListView.setDivider(null);

                    if (myTasksTitle.isEmpty()) {
                        myTasksListView.setVisibility(View.GONE);
                    } else {
                        myTasksListView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                }
            }
        }, 100);
    }

    private void loadDepartments() {
        departmentNames = new ArrayList<>();
        departmentObs = new ArrayList<>();
        String response = contactServer(loadDepartmentsPHP, Java_AES_Cipher.encryptSimple(emptyData));
        if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
            String[] lines = response.split(newLine);
            String[] line;
            for (int i = 0; i < lines.length; i ++) {
                line = lines[i].split(fS);
                if (line.length == 2) {
                    departmentNames.add(line[0]);
                    departmentObs.add(line[1]);
                }
            }

            departmentsArray = departmentNames.toArray(new String[departmentNames.size()]);
            departmentsSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, departmentsArray);
            addUserDepartmentSpinner.setAdapter(departmentsSpinnerAdapter);
            usersManagementDepartmentSpinner.setAdapter(departmentsSpinnerAdapter);

            addUserDepartments.removeAllViews();
            for (String da : departmentsArray) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(da);
                checkBox.setTypeface(font2);
                //checkBox.setChecked(false);
                addUserDepartments.addView(checkBox);
            }

            usersManagementDepartments.removeAllViews();
            for (String da : departmentsArray) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(da);
                checkBox.setTypeface(font2);
                //checkBox.setChecked(false);
                usersManagementDepartments.addView(checkBox);
            }

            DepartmentsListAdapter departmentsListAdapter = new DepartmentsListAdapter(activityContext, departmentNames);
            departmentsListView.setAdapter(departmentsListAdapter);
            departmentsListView.setDivider(null);
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
        }
    }

    private void loadUsers() {
        usersNames = new ArrayList<>();
        usersIds = new ArrayList<>();
        usersDepartment = new ArrayList<>();
        usersTaskMaster = new ArrayList<>();
        usersAdmin = new ArrayList<>();
        userSubordinates = new ArrayList<>();
        String response = contactServer(loadUsersPHP, Java_AES_Cipher.encryptSimple(emptyData));
        if (!response.contains("ERROR") && !response.contains("<br />")) { // no error response
            String[] lines = response.split(newLine);
            String[] line;
            for (int i = 0; i < lines.length; i ++) {
                line = lines[i].split(fS);
                if (line.length == 6) {
                    usersNames.add(line[0]);
                    usersIds.add(line[1]);
                    if (line[2].contains("0")) {
                        usersTaskMaster.add(false);
                    } else {
                        usersTaskMaster.add(true);
                    }
                    if (line[3].contains("0")) {
                        usersAdmin.add(false);
                    } else {
                        usersAdmin.add(true);
                    }
                    usersDepartment.add(line[4]);
                    userSubordinates.add(line[5]);

                    if (myID.equals(line[1])) {
                        myName = line[0];
                        myDepartments = line[5];
                        if (myDepartments.equals(" ")) myDepartments = "";
                    }
                }
            }

            ArrayList<String> tempUsersNames = new ArrayList<>();
            ArrayList<String> tempUsersIds = new ArrayList<>();
            String[] tempDepartments = myDepartments.split(",");

            for (int i = 0; i < usersIds.size(); i ++) {
                if (!usersIds.get(i).equals(myID)) {
                    for (int j = 0; j < tempDepartments.length; j ++) {
                        if (tempDepartments[j].equals(usersDepartment.get(i))) {
                            tempUsersNames.add(usersNames.get(i));
                            tempUsersIds.add(usersIds.get(i));
                        }
                    }
                }
            }

            String[] items = tempUsersNames.toArray(new String[tempUsersNames.size()]);
            taskerSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);
            addTaskTaskerSpinner.setAdapter(taskerSpinnerAdapter);

            UsersListAdapter usersListAdapter = new UsersListAdapter(activityContext, tempUsersNames, tempUsersIds);
            usersListView.setAdapter(usersListAdapter);
            usersListView.setDivider(null);
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
        }
    }

    private void addTask() {
        addTaskButton.setEnabled(false);
        String rawData;
        if (!addTaskTitle.getText().toString().isEmpty()) {
            templateName = "";
            templateTitle = "";
            templateDescription = "";
            templateTime = "";

            String tempUserId;
            if (personalTask.isChecked()) {
                tempUserId = myID;
            } else {
                tempUserId = usersIds.get(usersNames.indexOf(addTaskTaskerSpinner.getSelectedItem().toString()));
            }

            int n = addTaskNTimes.getSelectedItemPosition() + 1;
            for (int i = 0; i < n; i ++) {

                if (isValidWeekDay(calendar.get(Calendar.DAY_OF_WEEK))) {
                    rawData = generateID() + fS +                                                                          // task_id
                            myID + fS +                                                                                    // task_master_id
                            tempUserId + fS +     // tasker_id
                            addTaskTitle.getText().toString() + fS +                                                       // title
                            addTaskDescription.getText().toString() + fS +                                                 // description
                            year + "-" + month + "-" + day + " " + hour + ":" + String.format("%02d", minute) + ":00" + fS +                // deadline
                            "0" + fS +                                                                                     // tasker_marked_as_done
                            "0" + fS +                                                                                     // attachment_1
                            "0" + fS +                                                                                     // attachment_2
                            " " + fS +                                                                                     // tasker_comment
                            " " + fS +                                                                                     // task_master_comment
                            "0";                                                                                           // task_master_marked_as_done

                    String response = contactServer(addTaskPHP, Java_AES_Cipher.encryptSimple(rawData));
                    response = response.replaceAll(newLine, "\n");

                    if (response.contains("New task created successfully")) {
                        if (i == n - 1) {
                            Toast.makeText(context, getString(R.string.task_created), Toast.LENGTH_LONG).show();
                            addTaskTitle.setText("");
                            addTaskDescription.setText("");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    addTaskButton.setEnabled(true);
                                }
                            }, 1500);
                            changeScreen(TASK_MASTER_TASKS);
                        }
                    } else if (i == n - 1) {
                        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                addTaskButton.setEnabled(true);
                            }
                        }, 2000);
                    }
                } else {
                    i --;
                }
                if (i < n) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    month = calendar.get(Calendar.MONTH) + 1;
                    year = calendar.get(Calendar.YEAR);
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.error_title_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addTaskButton.setEnabled(true);
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

        Calendar tempNow = Calendar.getInstance();
        int da, mo, ye, ho, mi;
        da = tempNow.get(Calendar.DAY_OF_MONTH);
        mo = tempNow.get(Calendar.MONTH) + 1;
        ye = tempNow.get(Calendar.YEAR);
        ho = tempNow.get(Calendar.HOUR_OF_DAY);
        mi = tempNow.get(Calendar.MINUTE);

        String rawData = myTasksTaskId.get(selectedTask) + fS +
                tempMarked + fS +
                tempAttachment1 + fS +
                tempAttachment2 + fS +
                myTasksTaskerComment.get(selectedTask) + fS +
                ye + "-" + String.format("%02d", mo) + "-" + String.format("%02d", da) + " " + String.format("%02d", ho) + ":" + String.format("%02d", mi) + ":00";

        String response = contactServer(updateMyTaskPHP, Java_AES_Cipher.encryptSimple(rawData));
        response = response.replaceAll(newLine, "\n");

        if (response.contains("Task updated successfully")) {
            Toast.makeText(context, getString(R.string.task_updated), Toast.LENGTH_LONG).show();
            changeScreen(TASKER_TASKS);
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
            }, 1500);
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
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
        theirTasksDeadline.set(selectedTask, year + "-" + month + "-" + day + " " + hour + ":" + String.format("%02d", minute));
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

        if (response.contains("Task updated successfully")) {
            Toast.makeText(context, getString(R.string.task_updated), Toast.LENGTH_LONG).show();
            changeScreen(TASK_MASTER_TASKS);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksSaveButton.setEnabled(true);
                }
            }, 1500);
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksSaveButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void useAsTemplate() {
        templateName = theirTasksNameField.getText().toString();
        templateTitle = theirTasksTitleField.getText().toString();
        templateDescription = theirTasksDescriptionField.getText().toString();
        templateTime = theirTasksTime.getText().toString();
        changeScreen(TASK_MASTER_NEW_TASK);
    }

    private void updateUser() {
        deleteUserButton.setEnabled(false);
        updateUserButton.setEnabled(false);
        if (!usersManagementNameField.getText().toString().isEmpty()) {

            String tempSubordinates = "";
            if (usersManagementTaskMaster.isChecked()) {
                for (int i = 0; i < usersManagementDepartments.getChildCount(); i++) {
                    View view = usersManagementDepartments.getChildAt(i);
                    if (view instanceof CheckBox) {
                        CheckBox cb = (CheckBox) view;
                        if (cb.isChecked()) {
                            tempSubordinates += cb.getText().toString() + ",";
                        }
                    }
                }
            }
            if (tempSubordinates.endsWith(",")) tempSubordinates = tempSubordinates.substring(0, tempSubordinates.length() - 1);
            if (tempSubordinates.isEmpty()) tempSubordinates = " ";

            String tempTaskMaster = "0";
            if (usersManagementTaskMaster.isChecked()) tempTaskMaster = "1";
            String tempAdmin = "0";
            if (usersManagementAdmin.isChecked()) tempAdmin = "1";

            if ((usersManagementTaskMaster.isChecked() && !tempSubordinates.equals(" ")) ||
                    !usersManagementTaskMaster.isChecked()) {
                String rawData = usersIds.get(selectedUser) + fS +
                        usersManagementNameField.getText().toString() + fS +
                        tempTaskMaster + fS +
                        tempAdmin + fS +
                        usersManagementDepartmentSpinner.getSelectedItem().toString() + fS +
                        tempSubordinates;

                String response = contactServer(updateUserPHP, Java_AES_Cipher.encryptSimple(rawData));
                response = response.replaceAll(newLine, "\n");

                if (response.contains("User updated")) {
                    Toast.makeText(context, getString(R.string.user_updated), Toast.LENGTH_LONG).show();
                    changeScreen(TASK_MASTER_TASKERS);
                    loadUsers();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteUserButton.setEnabled(true);
                            updateUserButton.setEnabled(true);
                        }
                    }, 2000);
                } else {
                    Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteUserButton.setEnabled(true);
                            updateUserButton.setEnabled(true);
                        }
                    }, 2000);
                }
            } else {
                Toast.makeText(context, getString(R.string.error_no_department_selected), Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteUserButton.setEnabled(true);
                        updateUserButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            Toast.makeText(context, getString(R.string.error_username_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    deleteUserButton.setEnabled(true);
                    updateUserButton.setEnabled(true);
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
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String rawData = usersIds.get(selectedUser);
                        String response = contactServer(deleteUserPHP, Java_AES_Cipher.encryptSimple(rawData));
                        response = response.replaceAll(newLine, "\n");

                        if (response.contains("User deleted")) {
                            Toast.makeText(context, getString(R.string.user_deleted), Toast.LENGTH_LONG).show();
                            changeScreen(TASK_MASTER_TASKERS);
                            loadUsers();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteUserButton.setEnabled(true);
                                    updateUserButton.setEnabled(true);
                                }
                            }, 2000);
                        } else {
                            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteUserButton.setEnabled(true);
                                    updateUserButton.setEnabled(true);
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
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(getString(R.string.delete_tasker_q))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                .show();

        TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
        Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
        Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

        message.setTypeface(font1);
        b1.setTypeface(font1);
        b2.setTypeface(font1);

        //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

        b1.setTextColor(getColor(R.color.yes));
        b2.setTextColor(getColor(R.color.no));
    }

    private void deleteTask() {
        theirTasksTemplateButton.setEnabled(false);
        theirTasksDeleteButton.setEnabled(false);
        theirTasksSaveButton.setEnabled(false);

        String rawData = theirTasksTaskId.get(selectedTask);
        String response = contactServer(deleteTaskPHP, Java_AES_Cipher.encryptSimple(rawData));
        response = response.replaceAll(newLine, "\n");

        if (response.contains("Task deleted")) {
            Toast.makeText(context, getString(R.string.task_deleted), Toast.LENGTH_LONG).show();
            changeScreen(TASK_MASTER_TASKS);
            loadTheirTasks();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksTemplateButton.setEnabled(true);
                    theirTasksDeleteButton.setEnabled(true);
                    theirTasksSaveButton.setEnabled(true);
                }
            }, 2000);
        } else {
            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    theirTasksTemplateButton.setEnabled(true);
                    theirTasksDeleteButton.setEnabled(true);
                    theirTasksSaveButton.setEnabled(true);
                }
            }, 2000);
        }
    }

    private void deleteDepartment() {
        deleteDepartmentButton.setEnabled(false);
        updateDepartmentButton.setEnabled(false);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String rawData = departmentNames.get(selectedDepartment);
                        String response = contactServer(deleteDepartmentPHP, Java_AES_Cipher.encryptSimple(rawData));
                        response = response.replaceAll(newLine, "\n");

                        if (response.contains("Department deleted")) {
                            Toast.makeText(context, getString(R.string.department_deleted), Toast.LENGTH_LONG).show();
                            changeScreen(DEPARTMENTS);
                            loadDepartments();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteDepartmentButton.setEnabled(true);
                                    updateDepartmentButton.setEnabled(true);
                                }
                            }, 2000);
                        } else {
                            Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deleteDepartmentButton.setEnabled(true);
                                    updateDepartmentButton.setEnabled(true);
                                }
                            }, 2000);
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        deleteDepartmentButton.setEnabled(true);
                        updateDepartmentButton.setEnabled(true);
                        break;
                }
            }
        };
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(getString(R.string.delete_department_q))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                .show();

        TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
        Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
        Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

        message.setTypeface(font1);
        b1.setTypeface(font1);
        b2.setTypeface(font1);

        //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

        b1.setTextColor(getColor(R.color.yes));
        b2.setTextColor(getColor(R.color.no));
    }

    private void updateDepartment() {
        deleteDepartmentButton.setEnabled(false);
        String tempObs = "";
        if (departmentManagementObsField.getText().toString().isEmpty()) {
            tempObs = " ";
        } else {
            tempObs = departmentManagementObsField.getText().toString();
        }

        if (!departmentManagementNameField.getText().toString().isEmpty()) {
            String rawData = departmentNames.get(selectedDepartment) + fS +
                    departmentManagementNameField.getText().toString() + fS +
                    tempObs;

            String response = contactServer(updateDepartmentPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");

            if (response.contains("Department updated")) {
                Toast.makeText(context, getString(R.string.department_updated), Toast.LENGTH_LONG).show();
                changeScreen(DEPARTMENTS);
                loadDepartments();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteDepartmentButton.setEnabled(true);
                        updateDepartmentButton.setEnabled(true);
                    }
                }, 2000);
            } else {
                Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteDepartmentButton.setEnabled(true);
                        updateDepartmentButton.setEnabled(true);
                    }
                }, 2000);
            }
        } else {
            Toast.makeText(context, getString(R.string.error_department_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    deleteDepartmentButton.setEnabled(false);
                    updateDepartmentButton.setEnabled(false);
                }
            }, 2000);
        }
    }

    private void deleteDoneTasks() {
        String rawData = myID + fS + emptyData; // TODO should send password instead of emptyData
        String response = contactServer(deleteDoneTasksPHP, Java_AES_Cipher.encryptSimple(rawData));
        response = response.replaceAll(newLine, "\n");
        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
    }

    private void populateMyTaskCard(int which) {
        myTaskTitle.setText(myTasksTitle.get(which));

        if (myTasksDescription.get(which).isEmpty()) {
            myTaskDescription.setText(getString(R.string.no_description));
        } else {
            myTaskDescription.setText(myTasksDescription.get(which));
        }

        String[] tempDeadline = myTasksDeadline.get(which).split(" ");
        Calendar now =  Calendar.getInstance();
        Calendar taskDate = Calendar.getInstance();
        int da, mo, ye, ho, mi;
        if (tempDeadline.length == 2) {
            String[] tempDate = tempDeadline[0].split("-");
            myTaskDeadline.setText(tempDate[2] + dS + tempDate[1] + dS + tempDate[0] + " " + tempDeadline[1].substring(0, 2) + hS + tempDeadline[1].substring(3, 5));
            da = Integer.parseInt(tempDate[2]);
            mo = Integer.parseInt(tempDate[1]) - 1;
            ye = Integer.parseInt(tempDate[0]);
            ho = Integer.parseInt(tempDeadline[1].substring(0, 2));
            mi = Integer.parseInt(tempDeadline[1].substring(3, 5));
            taskDate.set(Calendar.YEAR, ye);
            taskDate.set(Calendar.MONTH, mo);
            taskDate.set(Calendar.DAY_OF_MONTH, da);
            taskDate.set(Calendar.HOUR_OF_DAY, ho);
            taskDate.set(Calendar.MINUTE, mi);
            long nowMil = now.getTimeInMillis();
            long taskMil = taskDate.getTimeInMillis();
            if (nowMil > taskMil) myTaskDeadline.setTextColor(getColor(R.color.task_late));

        } else {
            myTaskDeadline.setText("00" + dS + "00" + dS + "0000 00" + hS + "00");
        }

        if (myTasksAttachment1.get(which)) {
            ImageLoader attachment1ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment1Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + myID + "/" + myTasksTaskId.get(selectedTask) + "-1-thumb.jpg")
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
                    .data(taskImagesRemote + myID + "/" + myTasksTaskId.get(selectedTask) + "-2-thumb.jpg")
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

        if (myTasksTaskMasterId.get(which).contains(myID)) {
            myTaskTmCommentsTitle.setVisibility(View.GONE);
            myTaskTmComments.setVisibility(View.GONE);
        } else {
            myTaskTmCommentsTitle.setVisibility(View.VISIBLE);
            myTaskTmComments.setVisibility(View.VISIBLE);
            if (myTasksTaskMasterComment.get(which).equals(" ")) {
                myTaskTmComments.setText(getString(R.string.no_comments));
            } else {
                myTaskTmComments.setText(myTasksTaskMasterComment.get(which));
            }
        }

        myTaskDone.setChecked(myTasksTaskerMarkedAsDone.get(which));
    }

    private void populateTheirTasksCard(int which) {
        theirTasksCard.smoothScrollTo(0,0);
        theirTasksNameField.setText(usersNames.get(usersIds.indexOf(theirTasksTaskerId.get(which))));
        theirTasksTitleField.setText(theirTasksTitle.get(which));
        theirTasksDescriptionField.setText(theirTasksDescription.get(which));

        String tempDD = theirTasksDeadline.get(which);
        if (tempDD.length() == 19 && tempDD.contains(" ")) {
            String[] tempEach = tempDD.split(" ");
            String[] dateEach = tempEach[0].split("-");
            theirTasksDate.setText(dateEach[2] + dS + dateEach[1] + dS + dateEach[0]);
            theirTasksTime.setText(tempEach[1].substring(0, 2) + hS + tempEach[1].substring(3, 5));
            try {
                day = Integer.parseInt(dateEach[2]);
                month = Integer.parseInt(dateEach[1]);
                year = Integer.parseInt(dateEach[0]);
                hour = Integer.parseInt(tempEach[1].substring(0, 2));
                minute = Integer.parseInt(tempEach[1].substring(3, 5));

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH , day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            } catch (Exception e) {
                System.out.println(TAG + e.getMessage());
            }
        } else {
            theirTasksDate.setText("00" + dS + "00" + dS + "0000");
            theirTasksTime.setText("00" + hS + "00");
        }

        String tempMD = theirTasksLastModifiedDateTime.get(which);
        Calendar taskDate = Calendar.getInstance();
        int da, mo, ye, ho, mi;
        if (tempMD.contains("0000-00-00 00:00:00")) {
            theirTasksLastModified.setText("");
        } else {
            String[] tempEach1 = tempMD.split(" ");
            String[] dateEach1 = tempEach1[0].split("-");
            theirTasksLastModified.setText(dateEach1[2] + dS + dateEach1[1] + dS + dateEach1[0] + " " + tempEach1[1].substring(0, 2) + hS + tempEach1[1].substring(3, 5));
            da = Integer.parseInt(dateEach1[2]);
            mo = Integer.parseInt(dateEach1[1]) - 1;
            ye = Integer.parseInt(dateEach1[0]);
            ho = Integer.parseInt(tempEach1[1].substring(0, 2));
            mi = Integer.parseInt(tempEach1[1].substring(3, 5));
            taskDate.set(Calendar.YEAR, ye);
            taskDate.set(Calendar.MONTH, mo);
            taskDate.set(Calendar.DAY_OF_MONTH, da);
            taskDate.set(Calendar.HOUR_OF_DAY, ho);
            taskDate.set(Calendar.MINUTE, mi);
            long calMil = calendar.getTimeInMillis();
            long taskMil = taskDate.getTimeInMillis();
            if (calMil <= taskMil) {
                theirTasksLastModified.setTextColor(getColor(R.color.task_late));
            } else {
                theirTasksLastModified.setTextColor(getColor(R.color.today));
            }
        }

        if (theirTasksAttachment1.get(which)) {
            ImageLoader attachment1ImageLoader = SingletonImageLoader.get(context);
            ImageRequest attachment1Request = new ImageRequest.Builder(context)
                    .data(taskImagesRemote + theirTasksTaskerId.get(selectedTask) + "/" + theirTasksTaskId.get(selectedTask) + "-1-thumb.jpg")
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
                    .data(taskImagesRemote + theirTasksTaskerId.get(selectedTask) + "/" + theirTasksTaskId.get(selectedTask) + "-2-thumb.jpg")
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

        if (myID.contains(theirTasksTaskerId.get(which))) {
            theirTasksLastModifiedTitle.setVisibility(View.GONE);
            theirTasksLastModified.setVisibility(View.GONE);
            theirTasksTCommentsTitle.setText(R.string.my_comments);
            theirTasksMyCommentsTitle.setVisibility(View.GONE);
            theirTasksMyComments.setVisibility(View.GONE);
            theirTasksDone.setVisibility(View.GONE);
        } else {
            theirTasksLastModifiedTitle.setVisibility(View.VISIBLE);
            theirTasksLastModified.setVisibility(View.VISIBLE);
            theirTasksTCommentsTitle.setText(R.string.tasker_comments);
            theirTasksMyCommentsTitle.setVisibility(View.VISIBLE);
            theirTasksMyComments.setVisibility(View.VISIBLE);
            theirTasksDone.setVisibility(View.VISIBLE);
        }

        if (theirTasksTaskerComment.get(which).equals(" ")) {
            theirTasksTComments.setText(getString(R.string.no_comments));
        } else {
            theirTasksTComments.setText(theirTasksTaskerComment.get(which));
        }

        if (theirTasksTaskMasterComment.get(which).equals(" ")) {
            theirTasksMyComments.setText("");
        } else {
            theirTasksMyComments.setText(theirTasksTaskMasterComment.get(which));
        }
        theirTasksDone.setChecked(theirTasksTaskMasterMarkedAsDone.get(which));
    }

    private void populateTaskerManagement() {
        usersManagementNameField.setText(usersNames.get(selectedUser));
        usersManagementId.setText(usersIds.get(selectedUser));
        usersManagementDepartmentSpinner.setSelection(departmentsSpinnerAdapter.getPosition(usersDepartment.get(selectedUser)));
        usersManagementTaskMaster.setChecked(usersTaskMaster.get(selectedUser));
        usersManagementAdmin.setChecked(usersAdmin.get(selectedUser));
        for (int i = 0; i < usersManagementDepartments.getChildCount(); i++) {
            View view = usersManagementDepartments.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                cb.setChecked(false);
            }
        }
        if (usersTaskMaster.get(selectedUser)) {
            usersManagementDepartmentsText.setVisibility(View.VISIBLE);
            usersManagementDepartments.setVisibility(View.VISIBLE);

            String[] tempSubordinates = userSubordinates.get(selectedUser).split(",");

            for (int i = 0; i < usersManagementDepartments.getChildCount(); i++) {
                View view = usersManagementDepartments.getChildAt(i);
                if (view instanceof CheckBox) {
                    CheckBox cb = (CheckBox) view;
                    for (int j = 0; j < tempSubordinates.length; j ++) {
                        if (cb.getText().toString().equals(tempSubordinates[j])) {
                            cb.setChecked(true);
                        }
                    }
                }
            }





        } else {
            usersManagementDepartmentsText.setVisibility(View.GONE);
            usersManagementDepartments.setVisibility(View.GONE);
        }
    }

    private void populateDepartmentManagement() {
        departmentManagementNameField.setText(departmentNames.get(selectedDepartment));
        if (departmentObs.get(selectedDepartment).equals(" ")) {
            departmentManagementObsField.setText("");
        } else {
            departmentManagementObsField.setText(departmentObs.get(selectedDepartment));
        }
    }

    private void loadShowImage(boolean showTheirs) {
        String taskId, taskerId;
        if (showTheirs) {
            taskId = theirTasksTaskId.get(selectedTask);
            taskerId = theirTasksTaskerId.get(selectedTask);
        } else {
            taskId = myTasksTaskId.get(selectedTask);
            taskerId = myID;
        }

        ImageLoader showImageLoader = SingletonImageLoader.get(context);
        ImageRequest showImageRequest = new ImageRequest.Builder(context)
                .data(taskImagesRemote + taskerId + "/" + taskId + "-" + selectedAttachment + ".jpg")
                .placeholder(placeholderBig)
                .fallback(fallbackBig)
                .error(errorBig)
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
        hideKeyboard();
        changePasswordChangeButton.setEnabled(false);
        if (changePasswordNew1Field.getText().toString().isEmpty() && changePasswordNew2Field.getText().toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.error_new_pass_empty), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    changePasswordChangeButton.setEnabled(true);
                }
            }, 2000);
        } else if (!changePasswordNew1Field.getText().toString().equals(changePasswordNew2Field.getText().toString())) {
            Toast.makeText(context, getString(R.string.error_new_pass_do_not_match), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    changePasswordChangeButton.setEnabled(true);
                }
            }, 2000);
        } else if (changePasswordNew1Field.getText().toString().length() < 8) {
            Toast.makeText(context, getString(R.string.error_new_pass_8_chars), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    changePasswordChangeButton.setEnabled(true);
                }
            }, 2500);
        } else {
            String rawData = myID + fS + changePasswordOldField.getText().toString() + fS + changePasswordNew1Field.getText().toString();
            String response = contactServer(changePasswordPHP, Java_AES_Cipher.encryptSimple(rawData));
            response = response.replaceAll(newLine, "\n");
            response = response.replaceAll(fS, " - ");

            if (response.contains("Password changed")) {
                Toast.makeText(context, getString(R.string.pass_changed), Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePasswordOldField.setText("");
                        changePasswordNew1Field.setText("");
                        changePasswordNew2Field.setText("");
                        changePasswordChangeButton.setEnabled(true);
                        changeScreen(MAIN_MENU);
                    }
                }, 1500);
            } else {
                Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePasswordChangeButton.setEnabled(true);
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

            text1.setTypeface(font1);
            text2.setTypeface(font2);

            return view;
        }
    }

    public class DepartmentsListAdapter extends ArrayAdapter {
        private Activity activityContext;
        private ArrayList<String> name;

        public DepartmentsListAdapter(@NonNull Activity activityContext, ArrayList<String> name) {
            super(context, R.layout.departments_list, name);
            this.activityContext = activityContext;
            this.name = name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater inflater = activityContext.getLayoutInflater();
            if (convertView == null) view = inflater.inflate(R.layout.departments_list, null, true);

            LinearLayout trigger = (LinearLayout) view.findViewById(R.id.departments_trigger);
            TextView text1 = (TextView) view.findViewById(R.id.text1);

            trigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDepartment = position;
                    changeScreen(DEPARTMENT_MANAGEMENT);
                    populateDepartmentManagement();
                }
            });
            text1.setText(name.get(position));
            text1.setTypeface(font1);

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

            text1.setTypeface(font1);
            text2.setTypeface(font2);
            text3.setTypeface(font2);

            LinearLayout openTheirTasksTrigger  = (LinearLayout) view.findViewById(R.id.open_their_tasks_trigger);
            openTheirTasksTrigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTask = position;
                    changeScreen(THEIR_TASKS);
                }
            });

            text1.setText(name.get(position));
            text2.setText(title.get(position));
            String[] tempDeadline = deadline.get(position).split(" ");
            Calendar now =  Calendar.getInstance();
            Calendar taskDate = Calendar.getInstance();
            int da, mo, ye, ho, mi;
            if (tempDeadline.length == 2) {
                String[] tempDate = tempDeadline[0].split("-");
                text3.setText(tempDate[2] + dS + tempDate[1] + dS + tempDate[0] + " " + tempDeadline[1].substring(0, 2) + hS + tempDeadline[1].substring(3, 5));
                da = Integer.parseInt(tempDate[2]);
                mo = Integer.parseInt(tempDate[1]) - 1;
                ye = Integer.parseInt(tempDate[0]);
                ho = Integer.parseInt(tempDeadline[1].substring(0, 2));
                mi = Integer.parseInt(tempDeadline[1].substring(3, 5));
                taskDate.set(Calendar.YEAR, ye);
                taskDate.set(Calendar.MONTH, mo);
                taskDate.set(Calendar.DAY_OF_MONTH, da);
                taskDate.set(Calendar.HOUR_OF_DAY, ho);
                taskDate.set(Calendar.MINUTE, mi);
                long nowMil = now.getTimeInMillis();
                long taskMil = taskDate.getTimeInMillis();

                if (nowMil > taskMil) {
                    text3.setTextColor(getColor(R.color.task_late));
                    text3.setTypeface(font1);
                } else if (now.get(Calendar.DAY_OF_MONTH) == da && now.get(Calendar.MONTH) == mo && now.get(Calendar.YEAR) == ye) {
                    text3.setTextColor(getColor(R.color.today));
                    text3.setTypeface(font1);
                } else {
                    text3.setTextColor(getColor(R.color.black));
                    text3.setTypeface(font2);
                }
                if (myID.equals(theirTasksTaskMasterId.get(position)) && myID.equals(theirTasksTaskerId.get(position))) {
                    text1.setText("\uD83D\uDC64 " + text1.getText().toString());
                }
            } else {
                text3.setText("00" + dS + "00" + dS + "0000 00" + hS + "00");
            }
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

            LinearLayout openMyTaskTrigger  = (LinearLayout) view.findViewById(R.id.open_my_task_trigger);
            openMyTaskTrigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTask = position;
                    changeScreen(MY_TASK);
                }
            });

            text1.setText(title.get(position));
            String[] tempDeadline = deadline.get(position).split(" ");
            Calendar now =  Calendar.getInstance();
            Calendar taskDate = Calendar.getInstance();
            int da, mo, ye, ho, mi;
            if (tempDeadline.length == 2) {
                String[] tempDate = tempDeadline[0].split("-");
                text2.setText(tempDate[2] + dS + tempDate[1] + dS + tempDate[0] + " " + tempDeadline[1].substring(0, 2) + hS + tempDeadline[1].substring(3, 5));
                da = Integer.parseInt(tempDate[2]);
                mo = Integer.parseInt(tempDate[1]) - 1;
                ye = Integer.parseInt(tempDate[0]);
                ho = Integer.parseInt(tempDeadline[1].substring(0, 2));
                mi = Integer.parseInt(tempDeadline[1].substring(3, 5));
                taskDate.set(Calendar.YEAR, ye);
                taskDate.set(Calendar.MONTH, mo);
                taskDate.set(Calendar.DAY_OF_MONTH, da);
                taskDate.set(Calendar.HOUR_OF_DAY, ho);
                taskDate.set(Calendar.MINUTE, mi);
                long nowMil = now.getTimeInMillis();
                long taskMil = taskDate.getTimeInMillis();
                if (now.get(Calendar.DAY_OF_MONTH) == da && now.get(Calendar.MONTH) == mo && now.get(Calendar.YEAR) == ye) {
                    text2.setTextColor(getColor(R.color.today));
                    text1.setTypeface(font1);
                    text2.setTypeface(font1);
                } else if (nowMil > taskMil) {
                    text2.setTextColor(getColor(R.color.task_late));
                    openMyTaskTrigger.setEnabled(true);
                    text1.setTypeface(font1);
                    text2.setTypeface(font1);
                } else {
                    text1.setTextColor(getColor(R.color.not_today));
                    text2.setTextColor(getColor(R.color.not_today));
                    check.setAlpha(0.3f);
                    openMyTaskTrigger.setEnabled(false);
                    text1.setTypeface(font2);
                    text2.setTypeface(font2);
                }

                if (myTasksTaskMasterId.get(position).contains(myID)) {
                    text1.setText("\uD83D\uDC64 " + text1.getText().toString());
                }
            } else {
                text2.setText("00" + dS + "00" + dS + "0000 00" + hS + "00");
            }
            check.setChecked(done.get(position));

            return view;
        }
    }

    private void checkCameraPermission() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(activityContext, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void prepareCamera() {
        if (!hasCameraPermission()) {
            checkCameraPermission();
        }
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (selectedAttachment == 1) {
                            myTaskAttachment1.setImageResource(R.drawable.uploading);
                        } else {
                            myTaskAttachment2.setImageResource(R.drawable.uploading);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                File image = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + ".jpg");
                                File imageThumb = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + "-thumb.jpg");
                                scaleAndSaveImage(context.getExternalFilesDir(null).getAbsolutePath() + "/" + selectedAttachment + ".jpg");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String uploadResult = uploadImage(image, imageThumb, Java_AES_Cipher.encryptSimple(myID + fS + myTasksTaskId.get(selectedTask) + fS + selectedAttachment));
                                        populateMyTaskCard(selectedTask);
                                        if (uploadResult.contains("Picture uploaded")) {
                                            Toast.makeText(context, getString(R.string.picture_uploaded), Toast.LENGTH_LONG).show();
                                            if (selectedAttachment == 1) {
                                                myTasksAttachment1.set(selectedTask, true);
                                            } else {
                                                myTasksAttachment2.set(selectedTask, true);
                                            }
                                            populateMyTaskCard(selectedTask);
                                            contactServer(updateAttachmentPHP, Java_AES_Cipher.encryptSimple(myTasksTaskId.get(selectedTask) + fS + selectedAttachment + fS + "1"));
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
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
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
                return;
            }

            // Decode original image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap originalBitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath(), options);

            if (originalBitmap == null) {
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
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return bitmap;
        }
    }

    private boolean isValidWeekDay(int weekDay) {
        boolean valid = false;

        switch (weekDay) {
            case Calendar.MONDAY:
                valid = addTaskDay1.isChecked();
                //System.out.println(TAG + "MONDAY: " + valid);
                break;
            case Calendar.TUESDAY:
                valid = addTaskDay2.isChecked();
                //System.out.println(TAG + "TUESDAY: " + valid);
                break;
            case Calendar.WEDNESDAY:
                valid = addTaskDay3.isChecked();
                //System.out.println(TAG + "WEDNESDAY: " + valid);
                break;
            case Calendar.THURSDAY:
                valid = addTaskDay4.isChecked();
                //System.out.println(TAG + "THURSDAY: " + valid);
                break;
            case Calendar.FRIDAY:
                valid = addTaskDay5.isChecked();
                //System.out.println(TAG + "FRIDAY: " + valid);
                break;
            case Calendar.SATURDAY:
                valid = addTaskDay6.isChecked();
                //System.out.println(TAG + "SATURDAY: " + valid);
                break;
            case Calendar.SUNDAY:
                valid = addTaskDay7.isChecked();
                //System.out.println(TAG + "SUNDAY: " + valid);
                break;
        }

        return valid;
    }

    private int dpToPx(float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private boolean isToday(String deadline) {
        boolean result = false;

        Calendar today = Calendar.getInstance();

        int da, mo, ye;
        String[] deadlineFields = deadline.split(" ");
        String[] tempDate;

        if (deadlineFields.length == 2) {
            tempDate = deadlineFields[0].split("-");
            da = Integer.parseInt(tempDate[2]);
            mo = Integer.parseInt(tempDate[1]) - 1;
            ye = Integer.parseInt(tempDate[0]);

            if (today.get(Calendar.DAY_OF_MONTH) == da && today.get(Calendar.MONTH) == mo && today.get(Calendar.YEAR) == ye) result = true;
        }

        return result;
    }

    private String calendarToStr(Calendar date) {
        return date.get(Calendar.DAY_OF_MONTH) + "/" + String.format("%02d", (date.get(Calendar.MONTH) + 1)) + "/" + date.get(Calendar.YEAR) + " " + date.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", date.get(Calendar.MINUTE));
    }

    private void askExit() {
        DialogInterface.OnClickListener dialogClickListenerLogin = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(getString(R.string.close_app))
                .setPositiveButton(getString(R.string.yes), dialogClickListenerLogin)
                .setNegativeButton(getString(R.string.cancel), dialogClickListenerLogin)
                .show();

        TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
        Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
        Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);

        message.setTypeface(font1);
        b1.setTypeface(font1);
        b2.setTypeface(font1);

        //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));

        b1.setTextColor(getColor(R.color.yes));
        b2.setTextColor(getColor(R.color.no));
    }

    private void askExitOrLogout() {
        DialogInterface.OnClickListener dialogClickListenerMainMenu = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(getString(R.string.close_app))
                .setPositiveButton(getString(R.string.yes), dialogClickListenerMainMenu)
                .setNegativeButton(getString(R.string.cancel), dialogClickListenerMainMenu)
                .setNeutralButton(getString(R.string.log_out), dialogClickListenerMainMenu)
                .show();

        TextView message = (TextView) alertDialog.findViewById(android.R.id.message);
        Button b1 = (Button) alertDialog.findViewById(android.R.id.button1);
        Button b2 = (Button) alertDialog.findViewById(android.R.id.button2);
        Button b3 = (Button) alertDialog.findViewById(android.R.id.button3);

        message.setTypeface(font1);
        b1.setTypeface(font1);
        b2.setTypeface(font1);
        b3.setTypeface(font1);

        //message.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b1.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b2.setTextSize(getResources().getDimension(R.dimen.alert_text));
        //b3.setTextSize(getResources().getDimension(R.dimen.alert_text));

        b1.setTextColor(getColor(R.color.yes));
        b2.setTextColor(getColor(R.color.no));
        b3.setTextColor(getColor(R.color.logout));
    }
}