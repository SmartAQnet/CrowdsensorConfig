package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;

import edu.kit.teco.smartwlanconf.ui.Config;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//Just used to send wifi credentials of user wifi asynchronously to node
public class HttpNodePost extends AsyncTask<String, Void, Boolean> {

    private ConnectivityManager connectivityManager;

    public HttpNodePost(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //Async send of wifi credentials to node
    //The params used here are the URL of the node, the SSID and the password of the user wifi
    @Override
    protected Boolean doInBackground(String... params){
        try {
            return sendData(params[0],params[1],params[2]);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean s) {
    }

    //This is the method that actually sends the credentials to node by using the OKHTTP Library
    private Boolean sendData(String... params) throws Exception{
        Network node_network = null;
        try{
            for (Network network : connectivityManager.getAllNetworks()){
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    LinkProperties prop = connectivityManager.getLinkProperties(network);
                    if(prop.getInterfaceName().equals(Config.NETWORK_INTERFACE_TYPE)){
                        node_network = network;
                        break;
                    }
                }


            }
        } catch (Exception e){
            e.printStackTrace();
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            builder.socketFactory(node_network.getSocketFactory());
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        final OkHttpClient client = builder.build();

        builder.followRedirects(false)
                .build();

        RequestBody requestBody = new FormBody.Builder()
                .add(Config.SSIDPARAM, params[1])
                .add(Config.PWDPARAM, params[2])
                .build();

        Request request = new Request.Builder()
                .url(params[0])
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        String responseString = response.body().string();
        response.body().close();
        if(!response.isSuccessful()){
            throw new IOException("Unexpected code" + responseString);
        }
        return true;
    }
}
