package com.pianosdkreactnative;

import static com.pianosdkreactnative.utilities.Constant.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.FacebookSdk;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.pianosdkreactnative.utilities.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.piano.android.composer.Composer;
import io.piano.android.composer.listeners.EventTypeListener;
import io.piano.android.composer.listeners.ExperienceExecuteListener;
import io.piano.android.composer.listeners.MeterListener;
import io.piano.android.composer.listeners.NonSiteListener;
import io.piano.android.composer.listeners.ShowLoginListener;
import io.piano.android.composer.listeners.ShowTemplateListener;
import io.piano.android.composer.listeners.UserSegmentListener;
import io.piano.android.composer.model.Access;
import io.piano.android.composer.model.ActiveMeter;
import io.piano.android.composer.model.DelayBy;
import io.piano.android.composer.model.Event;
import io.piano.android.composer.model.EventExecutionContext;
import io.piano.android.composer.model.EventModuleParams;
import io.piano.android.composer.model.ExperienceRequest;
import io.piano.android.composer.model.SplitTest;
import io.piano.android.composer.model.User;
import io.piano.android.composer.model.events.EventType;
import io.piano.android.composer.model.events.ShowLogin;
import io.piano.android.composer.model.events.ShowTemplate;
import io.piano.android.composer.showtemplate.ComposerJs;
import io.piano.android.composer.showtemplate.ShowTemplateController;
import io.piano.android.id.PianoId;
import io.piano.android.id.PianoIdCallback;
import io.piano.android.id.PianoIdClient;
import io.piano.android.id.PianoIdException;
import io.piano.android.id.facebook.FacebookOAuthProvider;
import io.piano.android.id.google.GoogleOAuthProvider;
import io.piano.android.id.models.PianoIdAuthFailureResult;
import io.piano.android.id.models.PianoIdAuthSuccessResult;
import io.piano.android.id.models.PianoIdToken;

public class PianoSDKModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String TAG = "PianoSDKModule";
    private final ReactApplicationContext reactContext;
    private static String PIANO_ID_ENDPOINT;
    private static Composer.Endpoint COMPOSER_ENDPOINT;
    private WritableMap response = Arguments.createMap();
    private ShowTemplateController showTemplateController;
    private ComposerJs composerJs;

    PianoSDKModule(ReactApplicationContext context) {
        super(context);

        // set react context
        this.reactContext = context;
    }

    /**
     * @return {String} - representing the native module that is accessed in JS using this name.
     */
    @NonNull
    @Override
    public String getName() {
        return PIANO_SDK_MODULE_NAME;
    }

    @Override
    public void onActivityResult(Activity activity, int i, int i1, @Nullable Intent intent) {
        Log.d(TAG, "onActivityResult()");
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
    }

    /**
     * @param aid {String} - application id.
     * @param endpoint      {String} - used endpoint (prod).
     * @param facebookAppId {String} - facebook application id
     * @param callback      {Callback}
     *                      <p>
     *                      Create piano sdk module (method invoked by JS).
     */
    @ReactMethod
    public void init(String aid, String endpoint, String facebookAppId, Callback callback) {
        // check param endpoint not empty
        if (!TextUtils.isEmpty(endpoint)) {
            // init endpoint
            this.initEndpoint(endpoint);
        }

        // init piano id client
        PianoIdClient pianoIdClient = PianoId.init(PIANO_ID_ENDPOINT, aid)
                .with(new PianoIdCallback<PianoIdAuthSuccessResult>() {
                    @Override
                    public void onFailure(@NonNull PianoIdException exception) {
                        PianoIdCallback.super.onFailure(exception);

                        // invoke error from exception and callback
                        invokeError(exception.getMessage(), callback);
                    }

                    @Override
                    public void onSuccess(PianoIdAuthSuccessResult data) {
                        PianoIdCallback.super.onSuccess(data);

                        // reading token
                        onAccessToken(data.getToken(), callback);
                    }
                }).with(new GoogleOAuthProvider());

        // null check on facebook application id
        if (facebookAppId != null) {
            // set application id
            FacebookSdk.setApplicationId(facebookAppId);

            // add new facebook oauth provider to piano id client
            pianoIdClient.with(new FacebookOAuthProvider());
        }

        // using piano composer to init context
        Composer.init(this.reactContext, aid, COMPOSER_ENDPOINT);
        // add listener to react context
        this.reactContext.addActivityEventListener(this);
    }

    /**
     * @param endpoint {String} - endpoint
     *                 <p>
     *                 Init endpoint and its composer.
     */
    private void initEndpoint(String endpoint) {
        PIANO_ID_ENDPOINT = endpoint;
        COMPOSER_ENDPOINT = new Composer.Endpoint(endpoint, endpoint);
    }

    /**
     * @param error    {String} - error to pass to map.
     * @param callback {Callback} - callback that has to be invoked.
     *                 <p>
     *                 Invoke error using params.
     */
    private void invokeError(final String error, @Nullable final Callback callback) {
        // clean response
        this.cleanResponse();

        // update response data
        this.response.putString(ERROR, error);

        // invoke response
        this.invokeResponse(callback);
    }

    /**
     * Clean response by creating new writable map.
     */
    private void cleanResponse() {
        this.response = Arguments.createMap();
    }

    /**
     * @param callback {Callback} -
     */
    private void invokeResponse(@Nullable final Callback callback) {
        // null check
        if (callback != null) {
            callback.invoke(this.response);
        }
    }

    /**
     * @param pianoIdToken {PianoIdToken} - token object.
     * @param callback     {Callback}
     *                     <p>
     *                     Set response data and reload controller using token on UI thread.
     */
    private void onAccessToken(@Nullable final PianoIdToken pianoIdToken, @Nullable final Callback callback) {
        // clean response
        this.cleanResponse();

        // null check on token
        if (pianoIdToken != null) {
            // set user token
            this.setUserToken(pianoIdToken.accessToken);

            // set response data
            this.response.putString(ACCESS_TOKEN, pianoIdToken.accessToken);
            this.response.putString(REFRESH_TOKEN, pianoIdToken.refreshToken);
            this.response.putString(EXPIRES_IN, pianoIdToken.expiresIn.toString());
            this.response.putString(EXPIRES_IN_TIMESTAMP, String.valueOf(pianoIdToken.expiresInTimestamp));

            // null check on show template controller
            if (this.showTemplateController != null) {
                // reload with access token on UI thread
                Objects.requireNonNull(this.getCurrentActivity())
                        .runOnUiThread(() -> this.showTemplateController.reloadWithToken(pianoIdToken.accessToken));
            }
        }

        // invoke response
        this.invokeResponse(callback);
    }

    /**
     * @param accessToken {String} - access token that has to be assigned.
     *                    <p>
     *                    Assign param token to user using composer.
     */
    @ReactMethod
    public void setUserToken(@Nullable String accessToken) {
        Composer.getInstance().userToken(accessToken);
    }

    /**
     * @param callback {Callback}
     *                 <p>
     *                 Sign in function (using authentication).
     */
    @ReactMethod
    public void signIn(@Nullable Callback callback) {
        // create new login context
        PianoIdClient.SignInContext signInContext = PianoId.signIn().widget(PianoId.WIDGET_LOGIN);

        // perform authentication
        this.authentication(callback, signInContext);
    }

    /**
     * @param callback {Callback}
     *                 <p>
     *                 Registration function (using authentication).
     */
    @ReactMethod
    public void register(@Nullable Callback callback) {
        // create new registration context
        PianoIdClient.SignInContext signInContext = PianoId.signIn().widget(PianoId.WIDGET_REGISTER);

        // perform registration
        this.authentication(callback, signInContext);
    }

    /**
     * @param callback      {Callback}
     * @param signInContext {PianoIdClient.SignInContext} - context of authentication.
     *                      <p>
     *                      Authentication try and error management.
     */
    private void authentication(@Nullable Callback callback, PianoIdClient.SignInContext signInContext) {
        try {
            // get current activity
            MainActivity currentActivity = (MainActivity) this.getCurrentActivity();
            // null check on current activity --> if null, throw exception
            if (currentActivity == null) {
                throw new ActivityNotFoundException();
            }
            // set callback

            currentActivity.onActivityResultImplementation = result -> {
                // null check on result
                if (result == null) {
                    // invoke error
                    this.invokeError("Error: auth result null", callback);
                } else if (result instanceof PianoIdAuthSuccessResult) {
                    // invoke response
                    this.invokeResponse(callback);
                } else {
                    // retrieve exception
                    PianoIdAuthFailureResult pianoIdAuthFailureResult = (PianoIdAuthFailureResult) result;
                    PianoIdException exception = pianoIdAuthFailureResult.getException();
                    // invoke error
                    this.invokeError(exception.getMessage(), callback);
                }

                return null;
            };

            // launch action using param context
            currentActivity.authResult.launch(signInContext);
        } catch (ActivityNotFoundException exception) {
            this.invokeError(exception.getMessage(), callback);
        }
    }

    /**
     * @param accessToken {String}
     * @param callback    {Callback}
     */
    @ReactMethod
    public void signOut(@Nullable String accessToken, @Nullable Callback callback) {
        // perform sign out using access token if not null, else "tmp"
        PianoId.signOut(accessToken != null ? accessToken : TMP,
                PianoIdCallback.asResultCallback(new PianoIdCallback<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        PianoIdCallback.super.onSuccess(data);

                        // clean response
                        cleanResponse();

                        // set response data
                        response.putBoolean(SUCCESS, true);

                        // invoke response
                        invokeResponse(callback);
                    }

                    @Override
                    public void onFailure(@NonNull PianoIdException exception) {
                        PianoIdCallback.super.onFailure(exception);

                        // invoke error
                        invokeError(exception.getMessage(), callback);
                    }
                }));

        // delete cookies
        this.deleteCookies();
        // clear user token
        this.setUserToken(null);
        // clear user data
        this.clearStoredData();
    }

    /**
     * Delete cookies using cookie manager.
     */
    private void deleteCookies() {
        CookieManager.getInstance().removeAllCookies(null);
    }

    /**
     * Clear stored data.
     */
    @ReactMethod
    public void clearStoredData() {
        Composer.getInstance().clearStoredData();
    }

    /**
     * @param refreshToken {String}
     * @param callback     {Callback}
     */
    @ReactMethod
    public void refreshToken(@Nullable String refreshToken, @Nullable Callback callback) {
        // perform token refresh using refresh token if not null, else "tmp"
        PianoId.refreshToken(refreshToken != null ? refreshToken : TMP,
                PianoIdCallback.asResultCallback(new PianoIdCallback<PianoIdToken>() {
                    @Override
                    public void onFailure(@NonNull PianoIdException exception) {
                        PianoIdCallback.super.onFailure(exception);

                        // invoke error
                        invokeError(exception.getMessage(), callback);
                    }

                    @Override
                    public void onSuccess(PianoIdToken data) {
                        PianoIdCallback.super.onSuccess(data);

                        // refresh token
                        onAccessToken(data, callback);
                    }
                }));
    }

    /**
     * @param config               {ReadableMap}
     * @param showLoginCallback    {Callback}
     * @param showTemplateCallback {Callback}
     */
    @ReactMethod
    public void getExperience(@NonNull ReadableMap config,
                              @Nullable Callback showLoginCallback,
                              @Nullable Callback showTemplateCallback) {
        // create experience request builder
        ExperienceRequest.Builder builder = new ExperienceRequest.Builder();

        // create iterator on readable map
        ReadableMapKeySetIterator iterator = config.keySetIterator();
        // iterate over readable map
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();

            switch (key) {
                case ACCESS_TOKEN:
                    setUserToken(config.getString(key));
                    break;
                case DEBUG:
                    builder.debug(config.getBoolean(key));
                    break;
                case CONTENT_CREATED:
                    builder.contentCreated(config.getString(key));
                    break;
                case CONTENT_AUTHOR:
                    builder.contentAuthor(config.getString(key));
                    break;
                case CONTENT_IS_NATIVE:
                    builder.contentIsNative(config.getBoolean(key));
                    break;
                case CONTENT_SECTION:
                    builder.contentSection(config.getString(key));
                    break;
                case CUSTOM_VARIABLES:
                    builder.customVariables(this.readableMapToMap(Objects.requireNonNull(config.getMap(key))));
                    break;
                case REFERER:
                    builder.referer(config.getString(key));
                    break;
                case URL:
                    builder.url(Objects.requireNonNull(config.getString(key)));
                    break;
                case Constant.TAG:
                    builder.tag(Objects.requireNonNull(config.getString(key)));
                    break;
                case TAGS:
                    builder.tags(this.readableArrayToArrayList(Objects.requireNonNull(config.getArray(key))));
                    break;
                case ZONE:
                    builder.zone(config.getString(key));
                    break;
            }
        }
        // build request
        ExperienceRequest request = builder.build();

        Collection<EventTypeListener<? extends EventType>> listeners =
                Arrays.asList((ExperienceExecuteListener) event -> {
                            // create writable map
                            WritableMap map = Arguments.createMap();
                            // set data to map
                            map.putString(EVENT_NAME, EXPERIENCE_EXECUTE);

                            this.sendEvent(map, showLoginCallback);
                        },
                        (MeterListener) event -> Log.d(TAG, "METER LISTENER"),
                        (NonSiteListener) event -> Log.d(TAG, "NON SITE LISTENER"),
                        (ShowLoginListener) event -> {
                            // create writable map
                            WritableMap map = Arguments.createMap();
                            // set data to map
                            map.putString(EVENT_NAME, SHOW_LOGIN);
                            map.putMap(EVENT_MODULE_PARAMS, this.eventModuleParamsToMap(event.eventModuleParams));
                            map.putMap(EVENT_EXECUTION_CONTEXT, this.eventExecutionContextToMap(event.eventExecutionContext));
                            map.putMap(EVENT_DATA, this.showLoginToMap(event.eventData));

                            this.sendEvent(map, showLoginCallback);
                        },
                        (ShowTemplateListener) event -> {
                            // init show template controller if cancelable
                            boolean showTemplateControllerIfCancelable = true;
                            // retrieve value of show template controller if cancelable
                            if (config.hasKey(SHOW_TEMPLATE_CONTROLLER_IF_CANCELABLE)) {
                                showTemplateControllerIfCancelable =
                                        config.getBoolean(SHOW_TEMPLATE_CONTROLLER_IF_CANCELABLE) ||
                                                event.eventData.getShowCloseButton();
                            }

                            // init show template
                            boolean showTemplate = true;
                            // retrieve value of show template
                            if (config.hasKey(SHOW_TEMPLATE_CONTROLLER)) {
                                showTemplate = config.getBoolean(SHOW_TEMPLATE_CONTROLLER);
                            }

                            if (showTemplateControllerIfCancelable && showTemplate) {
                                // init composer JS
                                this.composerJs = new ComposerJs() {
                                    @JavascriptInterface
                                    @Override
                                    public void customEvent(@NotNull String eventData) {
                                        // create writable map
                                        WritableMap map = Arguments.createMap();

                                        // set data to map
                                        map.putString(EVENT_NAME, TEMPLATE_CUSTOM_EVENT);
                                        map.putString(EVENT_DATA, eventData);

                                        sendEvent(map, null);
                                    }
                                };

                                this.showTemplateController = new ShowTemplateController((Event<ShowTemplate>) event, composerJs);
                                this.showTemplateController.show((FragmentActivity) Objects.requireNonNull(this.getCurrentActivity()));
                            }

                            // create writable map
                            WritableMap map = Arguments.createMap();

                            // set data to map
                            map.putString(EVENT_NAME, SHOW_TEMPLATE);
                            map.putMap(EVENT_MODULE_PARAMS, this.eventModuleParamsToMap(event.eventModuleParams));
                            map.putMap(EVENT_EXECUTION_CONTEXT, this.eventExecutionContextToMap(event.eventExecutionContext));
                            map.putMap(EVENT_DATA, this.showTemplateToMap(event.eventData));
                            sendEvent(map, showTemplateCallback);
                        },
                        (UserSegmentListener) event ->
                                Log.d(TAG, "USER SEGMENT LISTENER"));

        Composer.getInstance().getExperience(request, listeners,
                exception -> Log.e(TAG, Objects.requireNonNull(exception.getMessage())));
    }

    /**
     * @param readableMap {ReadableMap} - map to iterate and read.
     * @return {Map<String, List<String>>} - map of key value.
     */
    @NotNull
    private Map<String, List<String>> readableMapToMap(@NotNull ReadableMap readableMap) {
        // create hash map
        Map<String, List<String>> map = new HashMap<>();

        // init iterator on param map
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        // iterate over readable map
        while (iterator.hasNextKey()) {
            // read key
            String key = iterator.nextKey();
            // create empty list of items
            List<String> items = new ArrayList<>();
            // read type from key
            ReadableType type = readableMap.getType(key);
            // add item to list only if it is string
            if (type == ReadableType.String) {
                items.add(readableMap.getString(key));
            }

            // add key value to map
            map.put(key, items);
        }

        return map;
    }

    /**
     * @param readableArray {ReadableArray} - array to iterate and read.
     * @return {List<String>}
     */
    @NotNull
    private List<String> readableArrayToArrayList(@NotNull ReadableArray readableArray) {
        // create empty list
        List<String> arrayList = new ArrayList<>();
        // loop over readable array
        for (int i = 0; i < readableArray.size(); i++) {
            // read type
            ReadableType type = readableArray.getType(i);
            // add item only if it is string
            if (type == ReadableType.String) {
                arrayList.add(readableArray.getString(i));
            }
        }

        return arrayList;
    }

    /**
     * @param map      {WritableMap} - map that has to be invoked.
     * @param callback {Callback}
     *                 <p>
     *                 Send event to JS module.
     */
    private void sendEvent(WritableMap map, @Nullable Callback callback) {
        // null check on callback
        if (callback != null) {
            callback.invoke(map);
        }

        // emit map with piano
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(PIANO_LISTENER_NAME, map);
    }

    /**
     * @param eventData {ShowLogin} - object contains user provider.
     * @return {WritableMap} - map with user provider data.
     */
    @NotNull
    private WritableMap showLoginToMap(@NotNull ShowLogin eventData) {
        // create map
        WritableMap map = Arguments.createMap();

        // set user provider to map
        map.putString(USER_PROVIDER, eventData.getUserProvider());

        return map;
    }

    /**
     * @param gaClientId {String}
     *                   <p>
     *                   Set ga client id.
     */
    @ReactMethod
    public void setGaClientId(@NonNull String gaClientId) {
        Composer.getInstance().gaClientId(gaClientId);
    }

    /**
     * Close modal using composer JS.
     */
    @ReactMethod
    public void closeTemplateController() {
        this.composerJs.close(CLOSE_MODAL);
    }

    /**
     * @param eventData {ShowTemplate}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap showTemplateToMap(@NotNull ShowTemplate eventData) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(TEMPLATE_ID, eventData.getTemplateId());
        map.putString(TEMPLATE_VARIANT_ID, eventData.getTemplateVariantId());
        map.putString(DISPLAY_MODE, eventData.getDisplayMode().getMode());
        map.putString(CONTAINER_SELECTOR, eventData.getContainerSelector());
        map.putMap(DELAY_BY, delayByToMap(eventData.getDelayBy()));
        map.putBoolean(SHOW_CLOSE_BUTTON, eventData.getShowCloseButton());
        map.putString(TEMPLATE_URL, eventData.getUrl());

        return map;
    }

    /**
     * @param delayBy {DelayBy}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap delayByToMap(@NotNull DelayBy delayBy) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(TYPE, Objects.requireNonNull(delayBy.type).name());
        map.putInt(VALUE, delayBy.value);

        return map;
    }

    /**
     * @param eventModuleParams {EventModuleParams}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap eventModuleParamsToMap(@NotNull EventModuleParams eventModuleParams) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(MODULE_ID, eventModuleParams.moduleId);
        map.putString(MODULE_NAME, eventModuleParams.moduleName);

        return map;
    }

    /**
     * @param eventExecutionContext {EventExecutionContext}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap eventExecutionContextToMap(@NotNull EventExecutionContext eventExecutionContext) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(EXPERIENCE_ID, eventExecutionContext.experienceId);
        map.putString(EXECUTION_ID, eventExecutionContext.executionId);
        map.putString(TRACKING_ID, eventExecutionContext.trackingId);
        map.putArray(SPLIT_TESTS, splitTestListToArray(Objects.requireNonNull(eventExecutionContext.splitTests).iterator()));
        map.putString(CURRENT_METER_NAME, eventExecutionContext.currentMeterName);
        map.putMap(USER, this.userToMap(eventExecutionContext.user));
        map.putString(REGION, eventExecutionContext.region);
        map.putString(COUNTRY_CODE, eventExecutionContext.countryCode);
        map.putArray(ACCESS_LIST, accessListToArray(Objects.requireNonNull(eventExecutionContext.accessList).iterator()));
        map.putArray(ACTIVE_METERS, activeMeterListToArray(Objects.requireNonNull(eventExecutionContext.activeMeters).iterator()));

        return map;
    }

    /**
     * @param access {Access}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap accessToMap(@NotNull Access access) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(RESOURCE_ID, access.resourceId);
        map.putString(RESOURCE_NAME, access.resourceName);
        map.putInt(DAYS_UNTIL_EXPIRATION, access.daysUntilExpiration);
        map.putInt(EXPIRE_DATE, access.expireDate);

        return map;
    }

    /**
     * @param splitTest {SplitTest}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap splitTestToMap(@NotNull SplitTest splitTest) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(VARIANT_ID, splitTest.variantId);
        map.putString(VARIANT_NAME, splitTest.variantName);

        return map;
    }

    /**
     * @param user {User}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap userToMap(@Nullable User user) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // null check on user
        if (user != null) {
            // set data to map
            map.putString(USER_ID, user.userId);
            map.putString(FIRST_NAME, user.firstName);
            map.putString(LAST_NAME, user.lastName);
            map.putString(EMAIL, user.email);
        }

        return map;
    }

    /**
     * @param activeMeter {ActiveMeter}
     * @return {WritableMap} - map contains value retrieved from param.
     */
    @NotNull
    private WritableMap activeMeterToMap(@NotNull ActiveMeter activeMeter) {
        // create writable map
        WritableMap map = Arguments.createMap();

        // set data to map
        map.putString(METER_NAME, activeMeter.meterName);
        map.putInt(VIEWS, activeMeter.views);
        map.putInt(VIEWS_LEFT, activeMeter.viewsLeft);
        map.putInt(MAX_VIEWS, activeMeter.maxViews);
        map.putInt(TOTAL_VIEWS, activeMeter.totalViews);

        return map;
    }

    /**
     * @param iterator {Iterator<ActiveMeter>}
     * @return {WritableArray}
     */
    @NotNull
    private WritableArray activeMeterListToArray(@Nullable Iterator<ActiveMeter> iterator) {
        // create writable array
        WritableArray array = Arguments.createArray();

        // iterate over active meter
        while (Objects.requireNonNull(iterator).hasNext()) {
            array.pushMap(activeMeterToMap(iterator.next()));
        }

        return array;
    }

    /**
     * @param iterator {Iterator<Access>}
     * @return {WritableArray}
     */
    @NotNull
    private WritableArray accessListToArray(@Nullable Iterator<Access> iterator) {
        // create writable array
        WritableArray array = Arguments.createArray();

        // iterate over access
        while (Objects.requireNonNull(iterator).hasNext()) {
            array.pushMap(this.accessToMap(iterator.next()));
        }

        return array;
    }

    /**
     * @param iterator {Iterator<SplitTest>}
     * @return {WritableArray}
     */
    @NotNull
    private WritableArray splitTestListToArray(@Nullable Iterator<SplitTest> iterator) {
        // create writable array
        WritableArray array = Arguments.createArray();

        // iterate over split test
        while (Objects.requireNonNull(iterator).hasNext()) {
            array.pushMap(this.splitTestToMap(iterator.next()));
        }

        return array;
    }
}
