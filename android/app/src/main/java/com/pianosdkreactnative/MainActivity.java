package com.pianosdkreactnative;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

import io.piano.android.id.PianoIdAuthResultContract;
import io.piano.android.id.PianoIdClient;
import io.piano.android.id.models.PianoIdAuthResult;
import io.piano.android.id.models.PianoIdAuthSuccessResult;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "PianoSDKReactNative";
    }

    /**
     * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
     * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
     * (aka React 18) with two boolean flags.
     */
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new DefaultReactActivityDelegate(
                this,
                getMainComponentName(),
                // If you opted-in for the New Architecture, we enable the Fabric Renderer.
                DefaultNewArchitectureEntryPoint.getFabricEnabled(), // fabricEnabled
                // If you opted-in for the New Architecture, we enable Concurrent React (i.e. React 18).
                DefaultNewArchitectureEntryPoint.getConcurrentReactEnabled() // concurrentRootEnabled
        );
    }

    protected OnActivityResultImplementation onActivityResultImplementation = null;
    final ActivityResultLauncher<PianoIdClient.SignInContext> authResult = registerForActivityResult(
            new PianoIdAuthResultContract(),
            result -> {
                onActivityResultImplementation.execute(result);
            }
    );

    public interface OnActivityResultImplementation<S, T> {
        S execute(T a);
    }
}
