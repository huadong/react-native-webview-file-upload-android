package com.oblongmana.webviewfileuploadandroid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.webview.ReactWebViewManager;

public class AndroidWebViewManager extends ReactWebViewManager {

    private AndroidWebViewPackage aPackage;
    public String getName() {
        return "AndroidWebView";
    }

    @Override
    protected WebView createViewInstance(ThemedReactContext reactContext) {
        WebView view = super.createViewInstance(reactContext);
        //Now do our own setWebChromeClient, patching in file chooser support
        final AndroidWebViewModule module = this.aPackage.getModule();
        view.setWebChromeClient(new WebChromeClient(){

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                if(!this.checkSelfPermission()) {
                    return;
                }

                module.setUploadMessage(uploadMsg);
                openFileChooserView();

            }

            public boolean onJsConfirm (WebView view, String url, String message, JsResult result){
                return true;
            }

            public boolean onJsPrompt (WebView view, String url, String message, String defaultValue, JsPromptResult result){
                return true;
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                if(!this.checkSelfPermission()) {
                    return;
                }

                module.setUploadMessage(uploadMsg);
                openFileChooserView();
            }

            // For Android  > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                if(!this.checkSelfPermission()) {
                    return;
                }

                module.setUploadMessage(uploadMsg);
                openFileChooserView();
            }

            // For Android > 5.0
            public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d("customwebview", "onShowFileChooser");
                if(!this.checkSelfPermission()) {
                    return false;
                }

                module.setmUploadCallbackAboveL(filePathCallback);
                openFileChooserView();
                return true;
            }

            private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
                Activity activity = module.getActivity();
                new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setPositiveButton("OK", okListener)
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }

            private boolean checkSelfPermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final Activity activity = module.getActivity();
                    // Here, activity is the current activity
                    if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            String msg = "You need allow access to photos.";
                            this.showMessageOKCancel(msg, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                            1);
                                }
                            });
                        } else {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    1);
                        }
                        return false;
                    }
                }
                return true;
            }

            private void openFileChooserView(){
                try {
                    final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setType("image/*");
                    final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose File");
                    module.getActivity().startActivityForResult(chooserIntent, 1);
                } catch (Exception e) {
                    Log.d("customwebview", e.toString());
                }
            }
        });
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT) {
            if (0 != (reactContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)){
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        return view;
    }

    public void setPackage(AndroidWebViewPackage aPackage){
        this.aPackage = aPackage;
    }

    public AndroidWebViewPackage getPackage(){
        return this.aPackage;
    }
}
