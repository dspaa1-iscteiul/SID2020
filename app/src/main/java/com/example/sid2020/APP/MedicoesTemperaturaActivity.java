package com.example.sid2020.APP;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.sid2020.APP.Connection.ConnectionHandler;
import com.example.sid2020.APP.Database.DatabaseHandler;
import com.example.sid2020.APP.Database.DatabaseReader;
import com.example.sid2020.APP.Helper.UserLogin;
import com.example.sid2020.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

public class MedicoesTemperaturaActivity extends AppCompatActivity {

    private static final String IP = UserLogin.getInstance().getIp();
    private static final String PORT = UserLogin.getInstance().getPort();
    private static final String username= UserLogin.getInstance().getUsername();
    private static final String password = UserLogin.getInstance().getPassword();

    String getMedicoesTemperatura = "http://" + IP + ":" + PORT + "/scripts/getMedicoesTemperatura.php";
    DatabaseHandler db = new DatabaseHandler(this);

    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicoes);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        updateMedicoes();
        drawGraphs();


    }

    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed( runnable = new Runnable() {
            public void run() {
                updateMedicoes();
                drawGraphs();

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    public void alertas(View v){
        Intent i = new Intent(this, AlertasGlobaisActivity.class);
        startActivity(i);
    }

    private void updateMedicoes(){
        db.clearMedicoes();
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray medicoesTemperatura = jParser.getJSONFromUrl(getMedicoesTemperatura, params);
        try {
            if (medicoesTemperatura != null){
                for (int i=0;i< medicoesTemperatura.length();i++){
                    JSONObject c = medicoesTemperatura.getJSONObject(i);
                    String valorMedicaoTemperatura = c.getString("ValorMedicao");
                    System.out.println(valorMedicaoTemperatura);
                    String dataHoraMedicao = c.getString("DataHoraMedicao");
                    System.out.println(dataHoraMedicao);
                    db.insert_MedicaoTemperatura(dataHoraMedicao,Double.parseDouble(valorMedicaoTemperatura));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawGraphs(){
        GraphView graphTemperatura = findViewById(R.id.temperatura_graph);
        graphTemperatura.removeAllSeries();
        int helper=0;
        DatabaseReader dbReader = new DatabaseReader(db);
        Cursor cursorTemperatura = dbReader.readMedicoesTemperatura();
        Date currentTimestamp = new Date();
        long currentLong = currentTimestamp.getTime();

        DataPoint[] datapointsTemperatura = new DataPoint[cursorTemperatura.getCount()];

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (cursorTemperatura.moveToNext()){
            Integer valorMedicaoTemperatura = cursorTemperatura.getInt(cursorTemperatura.getColumnIndex("ValorMedicao"));
            String dataHoraMedicao =  cursorTemperatura.getString(cursorTemperatura.getColumnIndex("DataHoraMedicao"));
            try {
                Date date = format.parse(dataHoraMedicao);
                long pointLong = date.getTime();
                long difference = currentLong - pointLong;
                double seconds = 300 - TimeUnit.MILLISECONDS.toSeconds(difference);
                datapointsTemperatura[helper]=new DataPoint(seconds,valorMedicaoTemperatura);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            helper++;
        }
        cursorTemperatura.close();

        graphTemperatura.getViewport().setXAxisBoundsManual(true);
        graphTemperatura.getViewport().setMinX(0);
        graphTemperatura.getViewport().setMaxX(300);
        LineGraphSeries<DataPoint> seriesTemperatura = new LineGraphSeries<>(datapointsTemperatura);
        seriesTemperatura.setColor(Color.RED);
        seriesTemperatura.setTitle("Temperatura");
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphTemperatura);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"300", "250","200","150","100","50","0"});
        graphTemperatura.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graphTemperatura.getLegendRenderer().setVisible(true);
        graphTemperatura.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphTemperatura.getLegendRenderer().setBackgroundColor(Color.alpha(0));
        graphTemperatura.addSeries(seriesTemperatura);
    }


}
