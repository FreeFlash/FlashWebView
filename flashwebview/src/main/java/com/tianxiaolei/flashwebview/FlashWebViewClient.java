package com.tianxiaolei.flashwebview;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianxiaolei on 2017/7/14.
 */

public class FlashWebViewClient extends WebViewClient {

    private Activity context;

    //证书异常处理中 block
    private boolean sslBlock = false;
    //证书异常messageTag
    private static final int SSL_TAG = 0xff5;
    //证书信息异常处理队列
    private List<SsLEvent> sslRequestQueue = new ArrayList<>();
    //证书异常处理Handler
    private Handler sslHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SSL_TAG:
                    if(sslBlock){
                        sslHandler.sendEmptyMessageDelayed(SSL_TAG,500);
                    }else{
                        if (sslRequestQueue.size() > 0) {
                            SsLEvent ssLEvent = sslRequestQueue.get(0);
                            handleSslEvent(ssLEvent);
                            sslRequestQueue.remove(ssLEvent);
                        }
                    }

                    break;
            }
        }
    };
    //用户信任证书队列
    private List<BigInteger> enableSslSerialNumberList = new ArrayList<>();

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
        try {
           BigInteger serialNumber = getSsLSerialNumber(error);
            for (int i = 0; i < enableSslSerialNumberList.size(); i++) {
                if (serialNumber.compareTo(enableSslSerialNumberList.get(i))==0) {
                    handler.proceed();
                    return;
                }
            }
        }catch (Throwable t){}
        sslRequestQueue.add(new SsLEvent(handler, error));
        sslHandler.sendEmptyMessage(SSL_TAG);
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
                        //  2017/7/14
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
        /*
        太长，只保留16个byte
         */
        for (int i = 0; i < pkenc.length && i < 16; i++) {
            String temp = Integer.toHexString(pkenc[i] & 0xFF);
            temp.toUpperCase();
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            str = str + temp;
            if (!(i == pkenc.length - 1 || i == 16 - 1)) {
                str = str + ":";
            }
        }
        return str;
    }

    private void handleSslEvent(final SsLEvent ssLEvent) {
        sslBlock = true;
        if (ssLEvent == null || ssLEvent.errorHandler == null) {
            sslBlock = false;
            return;
        }
        if (ssLEvent.sslError != null) {
            BigInteger serialNumber = getSsLSerialNumber(ssLEvent.sslError);
            if (serialNumber != null) {
                for (int i = 0; i < enableSslSerialNumberList.size(); i++) {
                    if (serialNumber.compareTo(enableSslSerialNumberList.get(i))==0) {
                        ssLEvent.errorHandler.proceed();
                        sslBlock = false;
                        return;
                    }
                }
            }
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            final android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(context);
            TextView textView = new TextView(context);
            textView.setText(Html.fromHtml("<u>" + "查看详细信息" + "</u>"));
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder1.setTitle("证书详情").setMessage(makeSslMessage(ssLEvent.sslError.getCertificate())).setPositiveButton("确定", new DialogInterface.OnClickListener() {

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
                    sslBlock = false;
                    ssLEvent.errorHandler.cancel();
                }
            }).setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BigInteger serialNumber = getSsLSerialNumber(ssLEvent.sslError);
                    if(serialNumber != null){
                        enableSslSerialNumberList.add(serialNumber);
                    }
                    sslBlock = false;
                    ssLEvent.errorHandler.proceed();
                }
            }).setCancelable(false).create().show();

        } else {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            builder.setTitle("证书信息异常").setMessage("是否信任并继续访问？").setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sslBlock = false;
                    ssLEvent.errorHandler.cancel();
                }
            }).setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sslBlock = false;
                    ssLEvent.errorHandler.proceed();
                }
            }).setCancelable(false).create().show();
        }


    }

    private BigInteger getSsLSerialNumber(SslError sslError) {
        BigInteger serialNumber = null;
        try {
            SslCertificate sslCertificate = sslError.getCertificate();
            Field field = sslCertificate.getClass().getDeclaredField("mX509Certificate");
            if (field != null) {
                field.setAccessible(true);
                X509Certificate x509Certificate = (X509Certificate) field.get(sslCertificate);
                if (x509Certificate != null) {
                    serialNumber = x509Certificate.getSerialNumber();
                }
            }
        } catch (Throwable t) {

        }
        return serialNumber;
    }

    private class SsLEvent {
        SslErrorHandler errorHandler;
        SslError sslError;

        public SsLEvent(SslErrorHandler errorHandler, SslError sslError) {
            this.errorHandler = errorHandler;
            this.sslError = sslError;
        }
    }
}
