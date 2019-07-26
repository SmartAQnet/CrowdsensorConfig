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

public class HttpNodePost extends AsyncTask<String, Void, Boolean> {

    //Needs API Level 26 because of Duration find workaround if needed
    /*public static final String REQUEST_METHOD = "POST";
    public static final Duration READ_TIMEOUT = Duration.ofMillis(15000);
    public static final int CONNECTION_TIMEOUT = 15000;*/
    private Context mContext;

    public HttpNodePost(){ }

    public HttpNodePost(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

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

    private Boolean sendData(String... params) throws Exception{
        Network node_network = null;
        try{
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (Network network : connManager.getAllNetworks()){
                NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    LinkProperties prop = connManager.getLinkProperties(network);
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
        builder.socketFactory(node_network.getSocketFactory());
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
        return response.isSuccessful();
    }
}
