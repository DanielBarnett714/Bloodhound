package me.dbarnett.bloodhound.Nextcloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ReadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;
import com.owncloud.android.lib.resources.shares.CreateRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import java.io.File;

/**
 * Created by daniel on 4/23/17.
 */
public class NextcloudActions implements OnRemoteOperationListener, OnDatatransferProgressListener {

    private OwnCloudClient mClient;
    private Handler mHandler = new Handler();
    /**
     * The Context.
     */
    Context context;

    /**
     * The Share link.
     */
    public String shareLink;

    /**
     * Instantiates a new Nextcloud actions.
     *
     * @param context the context
     */
    public NextcloudActions(Context context){

        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String serverAddress = prefs.getString("address", "");

        Uri serverUri = Uri.parse(serverAddress);


        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, context, true);



        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");

        login(username, password);


    }

    /**
     * Start upload.
     *
     * @param fileToUpload the file to upload
     * @param remotePath   the remote path
     * @param mimeType     the mime type
     */
    public void startUpload(File fileToUpload, String remotePath, String mimeType) {
        UploadRemoteFileOperation uploadOperation = new UploadRemoteFileOperation( fileToUpload.getAbsolutePath(), remotePath, mimeType);
        uploadOperation.addDatatransferProgressListener(this);
        uploadOperation.execute(mClient, this, mHandler);
    }


    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {

    }


    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileName) {
        mHandler.post( new Runnable() {
            @Override
            public void run() {
            }
        });
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
    }








}
