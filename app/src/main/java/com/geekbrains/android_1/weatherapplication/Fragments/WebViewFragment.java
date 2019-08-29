package com.geekbrains.android_1.weatherapplication.Fragments;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.OnBackPressedListener;
import com.geekbrains.android_1.weatherapplication.R;

import java.util.Objects;

public class WebViewFragment extends Fragment implements OnBackPressedListener {

    private WebView webView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_web_view, container, false);

        initUI(layout);

        return layout;
    }

    private void initUI(View layout) {
        String dest = BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, "");
        webView = layout.findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        if (dest.contains(",")) webView.loadUrl("https://en.wikipedia.org/wiki/" + dest.substring(0, dest.length() - 3));
        else webView.loadUrl("https://en.wikipedia.org/wiki/" + dest);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
}
