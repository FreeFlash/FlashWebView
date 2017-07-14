package com.tianxiaolei.flashwebview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by tianxiaolei on 2017/7/12.
 */
public class FlashWebView extends WebView {
    /*
    //清除网页访问留下的缓存
//由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
Webview.clearCache(true);

//清除当前webview访问的历史记录
//只会webview访问历史记录里的所有记录除了当前访问记录
Webview.clearHistory()；

//这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
Webview.clearFormData()；
     */

    public FlashWebView(Context context) {
        super(context);
        init();
    }

    public FlashWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlashWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        defaultConfig();
    }

    private void defaultConfig() {
        defaultSetting();
        this.setDownloadable(true);
    }

    private void defaultSetting() {
        WebSettings webSettings = this.getSettings();

        webSettings.setJavaScriptEnabled(true);
        //支持插件
//        webSettings.setPluginsEnabled(true);
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        //去掉缩放按钮，消除异常java.lang.IllegalArgumentException: Receiver not registered: android.widget.ZoomButtonsController
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSettings.setDomStorageEnabled(true);
        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

    }


    public void setDownloadable(boolean downloadable) {
        if (downloadable) {
            if (defaultDownloadListener == null) {
                defaultDownloadListener = new DownloadListener() {
                    @Override
                    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                        try {
                            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                        } catch (Throwable ignored) {
                        }
                    }

                };
            }
            setDownloadListener(defaultDownloadListener);
        } else {
            setDownloadListener(null);
        }
    }

    private DownloadListener defaultDownloadListener;

    @Override
    public void destroy() {
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
        // destory()
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        this.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        this.getSettings().setJavaScriptEnabled(false);
        //webView.clearView();
        this.removeAllViews();
        super.destroy();
    }


}
