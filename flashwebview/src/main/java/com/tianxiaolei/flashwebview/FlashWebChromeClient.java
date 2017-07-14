package com.tianxiaolei.flashwebview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * Created by tianxiaolei on 2017/7/14.
 */

public class FlashWebChromeClient extends WebChromeClient {
    private Activity context;

    public FlashWebChromeClient(Activity context) {
        this.context = context;
    }

    // Andorid 4.1+
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        openFileChooser(uploadFile, acceptType);
    }

    // Andorid 3.0 +
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
        try {
            context.startActivityForResult(createFileChooserIntent(uploadFile), REQUEST_UPLOAD_FILE_CODE);
        } catch (Throwable t) {
        }

    }

    // Android 3.0
    public void openFileChooser(ValueCallback<Uri> uploadFile) {
        openFileChooser(uploadFile, "");
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        context.startActivityForResult(createFileChooserIntentForAndroid5(filePathCallback), FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
        return true;
    }

    @Override
    public void onReceivedTitle(WebView webView, String title) {
        super.onReceivedTitle(webView, title);
    }

    @Override
    public void onProgressChanged(WebView webView, int newProgress) {
        super.onProgressChanged(webView, newProgress);
    }


    private ValueCallback<Uri> mUploadFile;
    private String mCameraFilePath;
    private static final int REQUEST_UPLOAD_FILE_CODE = 1003; // 选择文件请求码

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    private static final int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 1004; // 选择文件请求码

    private ValueCallback<Uri[]> mUploadFileForAndroid5;

    private Intent createFileChooserIntentForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadFileForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");

        Intent chooser = createChooserIntent(createCameraIntent(), photoIntent);
        chooser.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "请选择要上传的文件");
        return chooser;
    }

    private Intent createFileChooserIntent(ValueCallback<Uri> uploadFile) {
        mUploadFile = uploadFile;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
//        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);


        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");


        Intent chooser = createChooserIntent(createCameraIntent(), photoIntent);
        chooser.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "请选择要上传的文件");
        return chooser;
    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "请选择要上传的文件");
        return chooser;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            switch (requestCode) {
                case REQUEST_UPLOAD_FILE_CODE:
                    // 获取需上传文件成功
                    if (null == mUploadFile) {
                        return;
                    }
                    if (resultCode != RESULT_OK) {
                        mUploadFile.onReceiveValue(null);
                        mUploadFile = null;
                        return;
                    }
                    Uri result = (null == data) ? null : data.getData();
                    if (result == null) {
                        // 从相机获取
                        File cameraFile = new File(mCameraFilePath);
                        if (cameraFile.exists()) {
                            result = Uri.fromFile(cameraFile);
                            // 扫描相册
                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                        }
                    }
                    try {
                        if (mUploadFile != null) {
                            if (PathUtils.getPath(context, result) != null) {
                                mUploadFile.onReceiveValue(Uri.parse(PathUtils.getPath(context, result)));
                            } else {
                                mUploadFile.onReceiveValue(result);
                            }
                            mUploadFile = null;
                        }
                    } catch (Throwable e) {
                        // 获取需上传文件失败或者取消操作
                        if (mUploadFile != null) {
                            mUploadFile.onReceiveValue(null);
                            mUploadFile = null;
                        }
                    }

                    break;
                case FILECHOOSER_RESULTCODE_FOR_ANDROID_5:
                    try {
                        if (null == mUploadFileForAndroid5) {
                            return;
                        }
                        if (resultCode != RESULT_OK) {
                            mUploadFileForAndroid5.onReceiveValue(new Uri[]{});
                            mUploadFileForAndroid5 = null;
                            return;
                        }
                        Uri result5 = (data == null) ? null : data.getData();
                        if (result5 == null) {
                            // 从相机获取
                            File cameraFile = new File(mCameraFilePath);
                            if (cameraFile.exists()) {
                                result5 = Uri.fromFile(cameraFile);
                                // 扫描相册
                                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result5));
                            }
                        }
                        if (result5 != null) {

                            mUploadFileForAndroid5.onReceiveValue(new Uri[]{result5});
                        } else {
                            mUploadFileForAndroid5.onReceiveValue(new Uri[]{});
                        }
                        mUploadFileForAndroid5 = null;
                    } catch (Throwable t) {
                        if (mUploadFileForAndroid5 != null) {
                            mUploadFileForAndroid5.onReceiveValue(new Uri[]{});
                            mUploadFileForAndroid5 = null;
                        }
                    }
                    break;
            }

        } catch (Throwable t) {

        }
    }
}
