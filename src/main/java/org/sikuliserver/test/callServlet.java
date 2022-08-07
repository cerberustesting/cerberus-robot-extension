/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuliserver.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class callServlet {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) {

        String url = "http://localhost:5556/extra/ExecuteSikuliAction";
        URL obj;
        try {
            obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
            URL urlPicture = new URL("file:///C:/Users/bcivel/Documents/Selenium.sikuli/1441615900282.png");
            InputStream istream = urlPicture.openStream();
            byte[] bytes = IOUtils.toByteArray(istream);
            String imageDataString = Base64.encodeBase64URLSafeString(bytes);

            JSONObject object = new JSONObject();
            object.put("action", "click");
            object.put("picture", imageDataString);

            // Send post request
            con.setDoOutput(true);
            //DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            PrintStream os = new PrintStream(con.getOutputStream());
//            wr.writeBytes(object.toString());
//            wr.writeBytes("|ENDS|");
//            wr.flush();
//            wr.close();
            os.println(object.toString());
            os.println("|ENDS|");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + object.toString());
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());

        } catch (MalformedURLException ex) {
            Logger.getLogger(callServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(callServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(callServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
