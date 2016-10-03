package aviv.myicebreaker.network;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import aviv.myicebreaker.app.RealmManager;
import aviv.myicebreaker.Singleton;
import aviv.myicebreaker.module.CustomObjects.NewUser;
import io.realm.Realm;

/**
 * Created by Aviv on 20/07/2016.
 */
public class FacebookConnectivity {

    static final String TAG = "FacebookConnectivity";


    private Context context;
    private BaseListener baseListener;
    private Realm realm;
    private LoginButton loginButton;
    private String nName, nId, nEmail, nGender, nPhoto, nBirthday;
    private String[] photosUrlArr = new String[3];


    public FacebookConnectivity(Activity activity) {
        this.context = activity;
        this.baseListener = (BaseListener) activity;
    }

    public void initFacebook(CallbackManager callbackManager) {

        RealmManager.getInstance(context);
        realm = RealmManager.getRealm();
        loginButton = new LoginButton(context);

        loginButton.setReadPermissions(Arrays.asList("public_profile", "user_photos", "email", "user_birthday"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                albumTry();
                Log.d("accesdToken", loginResult.getAccessToken() + "");
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("GraphResponse", response.getRawResponse());
                        Log.d("GraphResponse!", response.toString());
                        Log.d("LoginResults", loginResult.getAccessToken().getToken() + "");


                        try {
                            realm.beginTransaction();


                            realm.commitTransaction();
//////////
                            Bundle params = new Bundle();
                            params.putString("fields", "id,name,birthday,email,gender,cover,picture.type(large)");
                            new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response != null) {
                                                try {
                                                    JSONObject data = response.getJSONObject();
                                                    Log.d("newData", data + "");

                                                    nName = data.getString("name");
                                                    nId = data.getString("id");
                                                    nEmail = data.getString("email");
                                                    nGender = data.getString("gender");
                                                    nBirthday = data.getString("birthday");
                                                    nPhoto = data.getJSONObject("picture").getJSONObject("data").getString("url");


                                                    Log.d("localUser YEAH", "localUser(facebook ID)" + nId +
                                                            "\nfirstName " + nName.substring(0, nName.lastIndexOf(" ")) +
                                                            "\nlastName " + nName.substring(nName.lastIndexOf(" ") + 1, nName.length()) +
                                                            "\ndateOfBirth " + nBirthday +
                                                            "\ngender " + nGender +
                                                            "\nimageUrl " + nPhoto +
                                                            "\nemail " + nEmail);
                                                    onDoneDownloadUserDetails();

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }).executeAsync();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("GraphExeception", e.toString());
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,gender,birthday,email,bio,photos{link}");
                parameters.putString("redirect", "false");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {


            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();

            }
        });
    }

    public void performLogin() {
        loginButton.performClick();
    }

    public void albumTry() {
        final Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        RealmManager.getInstance(context);
        realm = RealmManager.getRealm();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/albums",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d("FacebookAlbumRes", response.toString());

                        try {
                            JSONObject jsObj = new JSONObject(response.getRawResponse());

                            JSONArray jarray = jsObj.getJSONArray("data");
                            for (int i = 0; i < jarray.length(); i++) {

                                JSONObject albums = jarray.getJSONObject(i);
                                final String albumId = albums.getString("id");
                                if (albums.getString("name").equalsIgnoreCase("Profile Pictures")) {
                                    i = jarray.length();
                                    Log.d("albumIdF", albumId);

                                    new GraphRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            "/" + albumId + "/photos",
                                            params,
                                            HttpMethod.GET,
                                            new GraphRequest.Callback() {
                                                public void onCompleted(GraphResponse response) {
                                                    Log.d("photoRes", response + "");
                                                    try {
                                                        JSONObject jsObjPhoto = new JSONObject(response.getRawResponse());
                                                        JSONArray jarrayPhoto = jsObjPhoto.getJSONArray("data");
                                                        String[] photoArr = new String[3];
                                                        Log.d("jarrayPhoto", jarrayPhoto + "");
                                                        for (int i = 0; i < jarrayPhoto.length() && i < 3; i++) {

                                                            JSONObject jPhoto = jarrayPhoto.getJSONObject(i);
                                                            final String photoId = jPhoto.getString("id");
                                                            photoArr[i] = photoId;
                                                            Log.d("photoArr", photoArr[i]);


                                                            final int finalI = i;
                                                            new GraphRequest(
                                                                    AccessToken.getCurrentAccessToken(),
                                                                    "/" + photoArr[i] + "/picture",
                                                                    params,
                                                                    HttpMethod.GET,
                                                                    new GraphRequest.Callback() {
                                                                        public void onCompleted(GraphResponse response) {
                                                                            try {
                                                                                JSONObject jPics = new JSONObject(response.getRawResponse());
                                                                                JSONObject jPicsData = jPics.getJSONObject("data");
                                                                                Log.d("photoCheck", jPics + "");
                                                                                if (jPicsData.has("url")) {

                                                                                    photosUrlArr[finalI] = jPicsData.getString("url");

                                                                                    Log.d("photourl", photosUrlArr[finalI]);
                                                                                }
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                                Log.e("Problem photosUrlArr", " Connectivity e:" + e);

                                                                            }

                                                                        }
                                                                    }
                                                            ).executeAsync();

                                                        }

                                                    } catch (Exception e) {
                                                        Log.e("Problem photosUrlArr", " Connectivity e:" + e);
                                                    }
                                                    // Log.d("photosUrlArr THIS",photosUrlArr[1] );
                                                    //  ((SplashDoneDownload)baseListener).onDownloadProfilePicsDone(photosUrlArr);
                                                }
                                            }
                                    ).executeAsync();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Problem photosUrlArr", " Connectivity e:" + e);

                        }
                    }
                }
        ).executeAsync();

    }

    private void onDoneDownloadUserDetails() {
        if (nName != null) {
            Log.d("nName", nName); //TODO check why not recieve nName
            NewUser localUser = new NewUser(nName, nId, nBirthday, nGender, photosUrlArr, nEmail);
            Singleton.getInstance().setNewUser(localUser);
            Log.d(getClass().getSimpleName(), "User Ok " + localUser.getProfileImageUrl());
        } else {
            onDoneDownloadUserDetails();
            Log.e(getClass().getSimpleName(), " Problem With User: ");
        }
    }

}



