package com.anqgpt;

import android.app.Activity;
import android.os.Bundle;

import com.anqgpt.R;

import android.os.AsyncTask;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.OutputStream;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.FileInputStream;

public class MainActivity extends Activity {
EditText inputUrl,inputTxt,inputKey,inputTk,inputTemp;

Button btnFetch,btnClr,btnZm;

TextView outputText;

String res="",reskey="",key="",err="",restxt="";

float sz=15f;
long p2,tt0,tt1;
LocalDateTime t0,t1;

String ver=System.getProperty("os.version");
int vni=(int)(System.getProperty("os.version").charAt(0));
String mod="text-davinci-003",rB="";


@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);

inputUrl = findViewById(R.id.input_url);
inputTxt = findViewById(R.id.input_txt);
inputKey = findViewById(R.id.input_key);
inputTk = findViewById(R.id.input_tk);
inputTemp = findViewById(R.id.input_temp);

btnFetch = findViewById(R.id.btn_fetch);

btnClr = findViewById(R.id.btn_clr);
btnZm = findViewById(R.id.btn_zm);

outputText = findViewById(R.id.output_text);

restxt=outputText.getText().toString();

//inputTxt.requestFocus();
//inputKey.requestFocus();

//Chatgpt button
btnFetch.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {

try
 {
if(!inputKey.getText().toString().equals(""))
{
key=inputKey.getText().toString();
BufferedWriter wrt=new BufferedWriter(new FileWriter(new File(getFilesDir(),"apikey.txt")));
wrt.write(key);
wrt.close();

reskey="\tNew API Key >\n"+key;
}
 }
 catch (IOException e)
 {e.printStackTrace();
 err=e.toString();
 outputText.setText(e.toString());}

String url = inputUrl.getText().toString();
FetchWebsiteTask task = new FetchWebsiteTask();
task.execute(url);

outputText.setTextSize(12f);
outputText.setText("\tЖду GPT ответа...\n\n"+reskey);

tt0=System.currentTimeMillis();
if(vni>51)t0=LocalDateTime.now();

}
});

//Clear
btnClr.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {

try
 {
BufferedReader bR=new BufferedReader(new InputStreamReader(new FileInputStream(new File(getFilesDir(),"apikey.txt"))));
String sk0=bR.readLine();bR.close();
if(sk0!=null)key=sk0;
 }
catch (IOException e)
{
e.printStackTrace();
err=e.toString();
outputText.setText(e.toString());
}

reskey="\tOld API Key >\n"+key;
outputText.setTextIsSelectable(true);
outputText.setText("\tOS > "+ver+"\t"+reskey);

inputTxt.setText("");
inputTxt.requestFocus();

}});

//Zoom
btnZm.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {

sz=sz+1;
outputText.setTextSize(sz);
outputText.setTextIsSelectable(true);
outputText.setText(restxt);

}});
}

//Web request
private class FetchWebsiteTask extends AsyncTask<String, Void, String> {
@Override
protected String doInBackground(String... params) {
try {
String urlStr = params[0];

String txt = inputTxt.getText().toString();

if(!inputKey.getText().toString().equals("")) key=inputKey.getText().toString();

String tk=inputTk.getText().toString();
String temp=inputTemp.getText().toString();

String url0=urlStr;

String f4L= url0.substring(0, 4);

String requestBody = "{\n"+"    \"prompt\": \""+txt+"\",\n"+"    \"temperature\": "+temp+",\n"+"    \"max_tokens\": "+tk+"\n" + "}";

if (!f4L.equals("http")) {mod=url0;
url0="https://api.openai.com/v1/completions";
requestBody = "{\n"+
"\"model\": \""+mod+"\",\n"+
"\"prompt\": \""+txt+"\",\n"+"    \"max_tokens\": "+tk+",\n"+"    \"temperature\": "+temp+"\n" + "}";
}

rB=requestBody;

String apiKey=key;

URL urlObj = new URL(url0);
HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
con.setRequestMethod("POST");
con.setRequestProperty("Content-Type", "application/json");
con.setRequestProperty("Authorization", "Bearer " + apiKey);

con.setDoOutput(true);
OutputStream os = con.getOutputStream();
os.write(requestBody.getBytes());
os.flush();
os.close();

BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuilder response = new StringBuilder();
while ((inputLine = in.readLine()) != null) {response.append(inputLine);}
res=response.toString();
in.close();

return res.toString();}
catch(IOException e)
{err=e.toString();return"Err: "+e.getMessage();}
}

//Json output
@Override
protected void onPostExecute(String result) {

try{

JSONObject json = new JSONObject(res);
JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
restxt=choice.getString("text");

}catch (Exception e) {e.printStackTrace();}  

tt1=System.currentTimeMillis();
p2=tt1-tt0;
if(vni>51){t1=LocalDateTime.now();p2=ChronoUnit.MILLIS.between(t0,t1);}

sz=15f;
outputText.setTextSize(sz);
outputText.setTextIsSelectable(true);
outputText.setText(restxt+"\n"+"\n << Time: "+p2+"ms "+t1+" >>\n\tOS > "+ver+"\n"+err+"\n\tRequest > Response >>>\n"+rB+"\n"+res+"\n\t"+reskey);
err="";
}
}
}
