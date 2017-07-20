package com.tianxiaolei.flashwebview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
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

    }

    public void defaultSetting() {
        FlashWebViewUtil.setDefaultConfig(this.getSettings());
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
        try {
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
        } catch (Throwable t) {

        }
    }


}
