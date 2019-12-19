package com.example.studypal;


import android.Manifest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class One2OneSessionFragment extends Fragment implements  Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_KEY = "46479552";
    private static String SESSION_ID = "1_MX40NjQ3OTU1Mn5-MTU3NjU5Mjc3NTM4NX5QbXUxdXh4QTZpOUFrQUlQcE9NMU1zWS9-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjQ3OTU1MiZzaWc9ZDNjMGMzYTIwMDJhNGJhNGI4NGMwMTFhN2M3Y2Y3YjEyYjIyM2RmOTpzZXNzaW9uX2lkPTFfTVg0ME5qUTNPVFUxTW41LU1UVTNOalU1TWpjM05UTTROWDVRYlhVeGRYaDRRVFpwT1VGclFVbFFjRTlOTVUxeldTOS1mZyZjcmVhdGVfdGltZT0xNTc2NTkyODIxJm5vbmNlPTAuNzg2MDE1NTg2NDU1MjE2NyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTc2NjE0NDE5JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = "One2OneFragment";
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private Session mSession;
    private Publisher mPublisher;
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private Subscriber mSubscriber;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one2_one_session, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermissions();
    }


    public One2OneSessionFragment() {
        // Required empty public constructor
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(Objects.requireNonNull(getActivity()), perms)) {
            // initialize view objects from your layout
            mPublisherViewContainer = (FrameLayout)getActivity().findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout)getActivity().findViewById(R.id.subscriber_container);

            // initialize and connect to the session
            mSession = new Session.Builder(getContext(), API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    //SessionListener methods
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(getContext()).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(getContext(), stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }
    // PublisherListener methods

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }
}
