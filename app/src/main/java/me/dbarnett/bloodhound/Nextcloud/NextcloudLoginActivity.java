package me.dbarnett.bloodhound.Nextcloud;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;

import me.dbarnett.bloodhound.R;
import me.dbarnett.bloodhound.Services.BloodhoundService;

/**
 * A login screen that offers login via username/password.
 */
public class NextcloudLoginActivity extends AppCompatActivity {

    private EditText mAddressView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    /**
     * The Prefs.
     */
    SharedPreferences prefs;
    /**
     * The Address.
     */
    String address;
    /**
     * The Username.
     */
    String username;
    /**
     * The Password.
     */
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nextcloud_login);
        setupActionBar();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mAddressView = (EditText) findViewById(R.id.server_address);

        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void setupActionBar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void attemptLogin() {

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        address = mAddressView.getText().toString();
        username = mUsernameView.getText().toString();
        password = mPasswordView.getText().toString();

        showProgress(true);
        new Login(username, password, address);
    }

    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    private class Login implements OnRemoteOperationListener, OnDatatransferProgressListener {
        /**
         * The Intent.
         */
        Intent intent;

        private OwnCloudClient mClient;
        private Handler mHandler = new Handler();


        /**
         * Instantiates a new Login.
         *
         * @param username      the username
         * @param password      the password
         * @param serverAddress the server address
         */
        public Login(String username, String password, String serverAddress){
            intent = new Intent(getApplicationContext(), BloodhoundService.class);


            Uri serverUri = Uri.parse(serverAddress);

            mClient = OwnCloudClientFactory.createOwnCloudClient(
                    serverUri,
                    getApplicationContext(),
                    true);

            login(username, password);

        }


        /**
         * Login.
         *
         * @param username the username
         * @param password the password
         */
        public void login(String username, String password){
            mClient.setCredentials(
                    OwnCloudCredentialsFactory.newBasicCredentials(username, password)
            );

            startDownload("Bloodhound/Config/check", getApplicationContext().getFilesDir() + "/");

        }

        /**
         * Start download.
         *
         * @param filePath        the file path
         * @param targetDirectory the target directory
         */
        public void startDownload(String filePath, String targetDirectory) {
            DownloadRemoteFileOperation downloadOperation = new DownloadRemoteFileOperation(filePath, targetDirectory);
            downloadOperation.addDatatransferProgressListener(this);
            downloadOperation.execute( mClient, this, mHandler);
        }

        @Override
        public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileName) {
            mHandler.post( new Runnable() {
                @Override
                public void run() {
                    // do your UI updates about progress here
                }
            });
        }

        @Override
        public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
            if (operation instanceof DownloadRemoteFileOperation) {
                if (result.isSuccess()) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove("username");

                    edit.putString("username", username);

                    edit.remove("password");

                    edit.putString("password", password);

                    edit.remove("address");

                    edit.putString("address", address);
                    edit.commit();
                    finish();
                }else{
                    showProgress(false);
                }
            }
        }
    }
}

