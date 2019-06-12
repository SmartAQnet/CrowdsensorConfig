package edu.kit.teco.smartwlanconf.ui.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpPostRequest {

    public static boolean sendData(String urlData, String postData){
        HttpURLConnection connection = null;
        boolean ret = false;
        try {
            URL url = new URL(urlData);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Key","Value");
            connection.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) ret = true;
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null){
                connection.disconnect();
            }
        }
        return ret;
    }
}
