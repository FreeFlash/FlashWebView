package com.tianxiaolei.simple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.tianxiaolei.flashwebview.FlashWebChromeClient;
import com.tianxiaolei.flashwebview.FlashWebView;
import com.tianxiaolei.flashwebview.FlashWebViewClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;
    private FlashWebView flashWebView;
    private FlashWebChromeClient flashWebChormeClient;
    private FlashWebViewClient flashWebViewClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        flashWebView = (FlashWebView) findViewById(R.id.webView);
        flashWebChormeClient = new FlashWebChromeClient(this);
        flashWebView.setWebChromeClient(flashWebChormeClient);
        flashWebViewClient = new FlashWebViewClient(this);
        flashWebView.setWebViewClient(flashWebViewClient);
        flashWebView.loadUrl("https://m.jd.com/");
//        flashWebView.loadUrl("http://kouzi.pluosi.com/url_redirect?url=http%3A%2F%2F91qianmi.com%2Fbmember%2Fimgregister.xhtm%3FinviteCode%3DA978361&event=api&app_channel=%E9%92%B1%E7%B1%B3%E5%BA%94%E6%80%A5%E9%92%B1%E5%8C%85&id=23&device_id=863817035558348");
//        flashWebView.loadUrl("http://kouzi.pluosi.com/url_redirect?url=http%3A%2F%2F91qianbei.com%2Fbmember%2Fimgregister.xhtm%3FinviteCode%3DA319478&event=api&app_channel=%E9%92%B1%E5%91%97%E9%92%B1%E5%8C%85&id=25&device_id=863817035558348");
    }

    @Override
    protected void onDestroy() {
        flashWebView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        flashWebChormeClient.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button) {
            if (flashWebView != null && flashWebView.canGoBack()) {
                flashWebView.goBack();
            }

        }
    }
}
