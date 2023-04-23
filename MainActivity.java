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
//"\t\u2606\tUser Manual:\n\u2605 ChatGPT даст API Key sk-xxx... https://platform.openai.com/account/api-keys\n\u2605 CLEAR бе,рет прошлый API Key и очищает вопрос\n\u2605 Z00M удаляет служебную инфо ответа и увеличивает шрифт\n\u2605 Другая AI модель text-davinci-002 в https://api.openai.com/v1/engines/text-davinci-003/completions\n\u2605 Tokens 2023 вопроса/ответа max 40хх токен/слог\n\u2605 Temperature ответа 1>>0.5>>0-один ответ";

float sz=15f;
long p2;
LocalDateTime t0,t1;

String ver=System.getProperty("os.version");
int vni=(int)(System.getProperty("os.version").charAt(0));

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

String requestBody = "{\n"+
"\"model\": \""+"text-davinci-003"+"\",\n"+
"\"prompt\": \""+txt+"\",\n"+"    \"max_tokens\": "+tk+",\n"+"    \"temperature\": "+temp+"\n" + "}";


//"model": "text-davinci-003",
//"prompt": "Say this is a test",
//"max_tokens": 7,
//"temperature": 0


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
{err=e.toString();
//outputText.setText(e.toString());
return"Err: "+e.getMessage();
 }
}

//Json output
@Override
protected void onPostExecute(String result) {

try{

JSONObject json = new JSONObject(res);
JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
restxt=choice.getString("text");

}catch (Exception e) {e.printStackTrace();}  

if(vni>51){t1=LocalDateTime.now();p2=ChronoUnit.MILLIS.between(t0,t1);}

sz=15f;
outputText.setTextSize(sz);
outputText.setTextIsSelectable(true);
outputText.setText(restxt+"\n"+"\n << Time: "+p2+"ms "+t1+" >>\n\tOS > "+ver+"\t"+reskey+"\n"+err+"\n"+res);
}
}
}