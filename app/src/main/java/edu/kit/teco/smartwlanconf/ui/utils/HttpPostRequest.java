package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.Context;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPostRequest extends AsyncTask<String, Void, Boolean> {

    public static final String REQUEST_METHOD = "POST";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    private Context mContext;
    private String mData;
    private String mURL;

    public HttpPostRequest(){ }

    public HttpPostRequest(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //TODO: Make Progressdialog
        //dialog = new ProgressDialog(mContext);
        //dialog.setMessage("Please wait....");
        //dialog.setCanceledOnTouchOutside(false);
        //dialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params){
        try {
            sendData(params[0],params[1],params[2]);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean s) {
        //TODO: Stop showing Processdialog
        //if(dialog.isShowing())
        //    dialog.dismiss();

    }

    private void sendData(String url, String ssid, String pwd) throws Exception{
        final OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add(ssid, pwd)
                .build();
        Request request = new Request.Builder()
                .url(url)
                //.get()
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        String responseString = response.body().string();
        response.body().close();
        if(!response.isSuccessful()){
            throw new IOException("Unexpected code" + responseString);
        }
    }
}
