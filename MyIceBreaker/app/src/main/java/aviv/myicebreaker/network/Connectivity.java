package aviv.myicebreaker.network;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import aviv.myicebreaker.app.RealmManager;
import aviv.myicebreaker.Singleton;
import aviv.myicebreaker.module.CustomObjects.NewUser;
import io.realm.Realm;

/**
 * Created by Aviv on 03/07/2016.
 */
public class Connectivity {

    static final String TAG = "connectivity";

    private Realm realm;

    private Context context;
    private BaseListener baseListener;
    private String photoUrl;
    private String[] photosUrlArr = new String[3];

    public Connectivity(Activity activity) {
        this.context = activity;
        this.baseListener = (BaseListener) activity;
    }



   /* public void testLogin() {
        JsonObject json = new JsonObject();
        json.addProperty("3214636442", "HELLO");


        Ion.with(context)
                .load("http://79.178.127.180:8082/testpost/")
                .setJsonObjectBody(json)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e == null) {
                            User user = new User("Aviad", "1", 2);
                            //  ((SplashDoneDownload) baseListener).onLoginDone(user, null);
                        } else {
                            if (result != null) {
                                if (result.getHeaders().code() == 404) {
                                    Log.e(TAG, " Page Not Found! 404 error");
                                }
                            }
                            //  ((SplashDoneDownload) baseListener).onLoginDone(null, e);
                        }
                    }

                });

    }*/

    public void testUserDetails() {
        NewUser localUser = Singleton.getInstance().getNewUser();
        Log.d("localUser ", "localUser(facebook ID)" + localUser.getId() +
                "\nname " + localUser.getName() +
                "\ndateOfBirth " + localUser.getBirthday() +
                "\ngender " + localUser.getGender() +
                "\nimageUrl " + localUser.getProfileImageUrl() +
                "\nemail " + localUser.getEmail());

        JsonObject json = new JsonObject();
        json.addProperty("localUser", localUser.getId());
        json.addProperty("name", localUser.getName());
        json.addProperty("dateOfBirth", localUser.getBirthday());
        json.addProperty("gender", localUser.getGender());

        JSONArray imageArr = new JSONArray();

        imageArr.put(localUser.getImageUrl()[0]);
        imageArr.put(localUser.getImageUrl()[1]);
        imageArr.put(localUser.getImageUrl()[2]);

        json.addProperty("imageUrl", imageArr.toString());
        Log.d("imageUrlServ", imageArr.toString());
        json.addProperty("email", localUser.getEmail());
        json.addProperty("lastLocation", "33.2386268,35.6089003");

        Ion.with(context)
                .load("http://79.181.246.70:8082/api/user/GetUsersDetails")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.d("newResult", result + "");
                        Log.d("newException", e + "");
                    }
                });


    }

    public void testSearchForMatch() {
        JsonObject json = new JsonObject();
        json.addProperty("genderPreferences", 1);
        json.addProperty("lastLocation", "33.2386268,35.6089003");
        json.addProperty("localUser", "10154339288344725");
        JSONArray agePrefArr = new JSONArray();

        agePrefArr.put(22);
        agePrefArr.put(26);

        json.addProperty("agePreferences", agePrefArr.toString());


        Ion.with(context)
                .load("http://79.181.246.70:8082/api/user/SearchForMatch")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.d("newSearch", result + "");
                        Log.d("newSearchException", e + "");
                    }
                });

    }

    public String[] albumTry() {
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
        return photosUrlArr;
    }

    public void recieveChat() {
        Ion.with(context)
                .load("http://79.181.246.70:8082/api/user/GetChat/57c914fd3288e95b0ab2abb2")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
Log.d("result Chat ",result+" hhh");
                    }
                });
    }
}
