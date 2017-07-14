package com.tianxiaolei.flashwebview;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

/**
 * Created by tianxiaolei on 2017/7/14.
 */

public class FlashWebViewClient extends WebViewClient {

    private Activity context;

    public FlashWebViewClient(Activity context) {
        super();
        this.context = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        if (url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("content://"))) {
            return false;
        } else {
            return true;
        }
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);
    }


    @Override
    public void onReceivedSslError(WebView webView, final SslErrorHandler handler, final SslError error) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        final android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(context);
        TextView textView = new TextView(context);
        textView.setText(Html.fromHtml("<u>" + "查看详细信息" + "</u>"));
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        textView.getLayoutParams().
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder1.setTitle("证书详情").setMessage(makeSslMessage(error.getCertificate())).setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
            }
        });
        builder.setTitle("证书信息异常").setMessage("是否信任并继续访问？").setView(textView).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        }).setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        }).setCancelable(false).create().show();
    }

    private String makeSslMessage(SslCertificate sslCertificate) {
        String str = "";
        SimpleDateFormat dateformat = new SimpleDateFormat("yy/MM/dd HH:mm");
//        DateFormat dateformat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        try {
            str = str + "颁发给： " + sslCertificate.getIssuedTo().getCName() + "\n";
            str = str + "颁发者： " + sslCertificate.getIssuedBy().getCName() + "\n";
            str = str + "有效期： " + dateformat.format(sslCertificate.getValidNotBeforeDate()) + " \n至 " + dateformat.format(sslCertificate.getValidNotAfterDate()) + "\n";

            Field field = sslCertificate.getClass().getDeclaredField("mX509Certificate");
            if (field != null) {
                field.setAccessible(true);
                try {
                    X509Certificate x509Certificate = (X509Certificate) field.get(sslCertificate);
                    if (x509Certificate != null) {
                        // TODO: 2017/7/14
                        str = str + "证书版本：" + String.valueOf(x509Certificate.getVersion() + "\n");
                        str = str + "证书序列号：" + x509Certificate.getSerialNumber().toString(16) + "\n";
                        str = str + "签名算法：" + x509Certificate.getSigAlgName() + "\n";
                        str = str + "证书指纹：" + makePublicKeyStr(x509Certificate.getPublicKey().getEncoded());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            return str;
        }
    }

    private String makePublicKeyStr(byte[] pkenc) {
        String str = "";
        for (int i = 0; i < pkenc.length; i++) {
            String temp = Integer.toHexString(pkenc[i] & 0xFF);
            temp.toUpperCase();
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            str = str + temp;
            if (i < pkenc.length - 1) {
                str = str + ":";
            }
        }
        return str;
    }
}
