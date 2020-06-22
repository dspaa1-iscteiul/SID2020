package com.example.sid2020.APP;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.sid2020.APP.Connection.ConnectionHandler;
import com.example.sid2020.APP.Database.DatabaseHandler;
import com.example.sid2020.APP.Database.DatabaseReader;
import com.example.sid2020.APP.Helper.UserLogin;
import com.example.sid2020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AlertasGlobaisActivity extends AppCompatActivity {

    private static final String IP = UserLogin.getInstance().getIp();
    private static final String PORT = UserLogin.getInstance().getPort();
    private static final String username= UserLogin.getInstance().getUsername();
    private static final String password = UserLogin.getInstance().getPassword();
    DatabaseHandler db = new DatabaseHandler(this);
    String getAlertasGlobais = "http://" + IP + ":" + PORT + "/scripts/getAlertasGlobais.php";
    int year;
    int month;
    int day;
    String date;

    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas_globais);

        if (getIntent().hasExtra("date")){
            int[] yearMonthDay = getIntent().getIntArrayExtra("date");
            year = yearMonthDay[0];
            month= yearMonthDay[1];
            day=yearMonthDay[2];
        }
        else{
            year = Calendar.getInstance().get(Calendar.YEAR);
            month = Calendar.getInstance().get(Calendar.MONTH)+1;
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        }
        dateToString();
        getAlertas();
        listAlertas();
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed( runnable = new Runnable() {
            public void run() {
                getAlertas();
                listAlertas();

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    private void dateToString() {
       String yearString = Integer.toString(year);
       String monthString ="";
       String dayString="";
        if (month<10){
            monthString="0"+Integer.toString(month);
        }else{
            monthString=Integer.toString(month);
        }
        if(day<10){
            dayString="0"+Integer.toString(day);
        }
        else{
            dayString=Integer.toString(day);
        }
        date = yearString+"-"+monthString+"-"+dayString;
        String formatted_date = dayString+"-"+monthString+"-"+yearString;
        TextView stringData = findViewById(R.id.diaSelecionado_tv);
        stringData.setText(formatted_date);

    }

    public void showDatePicker(View v) {
        Intent intent = new Intent(this,DatePickerActivity.class);
        intent.putExtra("global",1);
        startActivity(intent);
        finish();
    }

    private void getAlertas() {
        db.clearAlertasGlobais();
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("date",date);
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray medicoesTemperatura = jParser.getJSONFromUrl(getAlertasGlobais, params);
        try{
            if(medicoesTemperatura!=null){
                for (int i=0;i< medicoesTemperatura.length();i++){
                    JSONObject c = medicoesTemperatura.getJSONObject(i);
                    String dataHoraMedicao = c.getString("DataHoraMedicao");
                    String tipoSensor = c.getString("TipoSensor");
                    double valorMedicao;
                    int id = c.getInt("ID");
                    try {
                        valorMedicao = c.getDouble("ValorMedicao");
                    } catch (Exception e) {
                        valorMedicao = -1000.0;
                    }
                    double limite;
                    try {
                        limite = c.getDouble("Limite");
                    } catch (Exception e) {
                        limite = -1000.0;
                    }
                    String descricao = c.getString("Descricao");
                    int controlo;
                    try {
                        controlo = c.getInt("Controlo");
                    } catch (Exception e) {
                        controlo = 0;
                    }
                    String extra = c.getString("Extra");
                    db.insert_alertaGlobal(id,dataHoraMedicao,tipoSensor,valorMedicao,limite,descricao,controlo,extra);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void listAlertas() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("appPref", MODE_PRIVATE);
        int mostRecentEntry = 0;
        int mostRecentData = 0;
        int tamanho = 0;
        int contador = 0;

        TableLayout table = findViewById(R.id.tableAlertas);

        DatabaseReader dbReader = new DatabaseReader(db);
        Cursor cursorAlertasGlobais = dbReader.readAlertasGlobais();
        tamanho = cursorAlertasGlobais.getCount();
        int oldTamanho = sp.getInt("tamanho", 0);
        if(date.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date())) && tamanho > oldTamanho){
            SharedPreferences.Editor editorLT = sp.edit().putInt("lastSize", oldTamanho );
            editorLT.apply();
            SharedPreferences.Editor editorT = sp.edit().putInt("tamanho", tamanho);
            editorT.apply();
        } else if (date.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date())) && tamanho < oldTamanho) {
            SharedPreferences.Editor editorLT = sp.edit().putInt("lastSize", 0 );
            editorLT.apply();
            SharedPreferences.Editor editorT = sp.edit().putInt("tamanho", tamanho);
            editorT.apply();
        }

        table.removeAllViewsInLayout();
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView headerHora = new TextView(this);
        headerHora.setText("Hora");
        headerHora.setTextSize(16);
        headerHora.setPadding(dpAsPixels(16),dpAsPixels(50),0,10);

        TextView headerTipoSensor = new TextView(this);
        headerTipoSensor.setText("Tipo Sensor");
        headerTipoSensor.setTextSize(16);
        headerTipoSensor.setPadding(dpAsPixels(16),dpAsPixels(50),0,10);

        TextView headerValorMedicao = new TextView(this);
        headerValorMedicao.setText("Valor Medicao");
        headerValorMedicao.setTextSize(16);
        headerValorMedicao.setPadding(dpAsPixels(16),dpAsPixels(50),0,10);

        TextView headerLimite = new TextView(this);
        headerLimite.setText("Limite");
        headerLimite.setTextSize(16);
        headerLimite.setPadding(dpAsPixels(16),dpAsPixels(50),0,10);

        TextView headerDescricao = new TextView(this);
        headerDescricao.setText("Descricao");
        headerDescricao.setTextSize(16);
        headerDescricao.setPadding(dpAsPixels(16),dpAsPixels(50),dpAsPixels(5),10);

        TextView headerControlo = new TextView(this);
        headerControlo.setText("Gravidade (0-3)");
        headerControlo.setTextSize(16);
        headerControlo.setPadding(dpAsPixels(16),dpAsPixels(50),dpAsPixels(5),10);

        TextView headerExtra = new TextView(this);
        headerExtra.setText("Categoria");
        headerExtra.setTextSize(16);
        headerExtra.setPadding(dpAsPixels(16),dpAsPixels(50),dpAsPixels(5),10);

        headerRow.addView(headerControlo);
        headerRow.addView(headerExtra);
        headerRow.addView(headerHora);
        headerRow.addView(headerTipoSensor);
        headerRow.addView(headerValorMedicao);
        headerRow.addView(headerLimite);
        headerRow.addView(headerDescricao);

        table.addView(headerRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        while (cursorAlertasGlobais.moveToNext()) {
            contador++;
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView hora = new TextView(this);
            String horaDesformatado = cursorAlertasGlobais.getString(cursorAlertasGlobais.getColumnIndex("DataHoraMedicao"));
            String horaFormatado = horaDesformatado.split(" ")[1];
            hora.setText(horaFormatado);
            hora.setPadding(dpAsPixels(16), dpAsPixels(5), 0, 0);
            String data = horaDesformatado.split(" ")[0];

            TextView tipoSensor = new TextView(this);
            String valorTipoSensor = cursorAlertasGlobais.getString(cursorAlertasGlobais.getColumnIndex("TipoSensor"));
            if (valorTipoSensor == null || valorTipoSensor.equals("null")) valorTipoSensor = "";
            tipoSensor.setText(valorTipoSensor);
            tipoSensor.setPadding(dpAsPixels(16), dpAsPixels(5), dpAsPixels(5), 0);

            TextView valorMedicao = new TextView(this);
            String valor = Double.toString(cursorAlertasGlobais.getDouble(cursorAlertasGlobais.getColumnIndex("ValorMedicao")));
            if (valor.equals("-1000.0")) valor = "";
            valorMedicao.setText(valor);
            valorMedicao.setPadding(dpAsPixels(16), dpAsPixels(5), 0, 0);

            TextView limite = new TextView(this);
            String valorLimite = Double.toString(cursorAlertasGlobais.getDouble(cursorAlertasGlobais.getColumnIndex("Limite")));
            if (valorLimite.equals("-1000.0")) valorLimite = "";
            limite.setText(valorLimite);
            limite.setPadding(dpAsPixels(16), dpAsPixels(5), 0, 0);

            TextView descricao = new TextView(this);
            String valorDescricao = cursorAlertasGlobais.getString(cursorAlertasGlobais.getColumnIndex("Descricao"));
            if (valorDescricao == null || valorDescricao.equals("null")) valorDescricao = "";
            descricao.setText(valorDescricao);
            descricao.setPadding(dpAsPixels(16), dpAsPixels(5), dpAsPixels(5), 0);

            TextView controlo = new TextView(this);
            String valorControlo = Integer.toString(cursorAlertasGlobais.getInt(cursorAlertasGlobais.getColumnIndex("Controlo")));
            controlo.setText(valorControlo);
            controlo.setPadding(dpAsPixels(16), dpAsPixels(5), dpAsPixels(5), 0);

            TextView extra = new TextView(this);
            String valorExtra = cursorAlertasGlobais.getString(cursorAlertasGlobais.getColumnIndex("Extra"));
            if (valorExtra == null || valorExtra.equals("null")) valorExtra = "";
            extra.setText(valorExtra);
            extra.setPadding(dpAsPixels(16), dpAsPixels(5), dpAsPixels(5), 0);

            int controlo_value = Integer.valueOf(valorControlo);
            int id = cursorAlertasGlobais.getInt(cursorAlertasGlobais.getColumnIndex("ID"));
            if(id > mostRecentEntry) mostRecentEntry = id;

            String intHora = horaFormatado.replace(":", "");
            int newHora = Integer.parseInt(intHora);
            if (newHora > mostRecentData) mostRecentData = newHora;
            if (data.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date())) && contador <= tamanho - sp.getInt("lastSize", 0)) {
                hora.setTypeface(null, Typeface.BOLD_ITALIC);
                tipoSensor.setTypeface(null, Typeface.BOLD_ITALIC);
                valorMedicao.setTypeface(null, Typeface.BOLD_ITALIC);
                limite.setTypeface(null, Typeface.BOLD_ITALIC);
                descricao.setTypeface(null, Typeface.BOLD_ITALIC);
                controlo.setTypeface(null, Typeface.BOLD_ITALIC);
                extra.setTypeface(null, Typeface.BOLD_ITALIC);
            }

              if(id > sp.getInt("lastID", 0) && contador == 1){
                if (sp.getInt("refreshPref", 1) == 0) {
                    Bitmap levelImage;
                    switch (controlo_value) {
                        case 0:
                            levelImage = BitmapFactory.decodeResource(getResources(), R.drawable.warning_level0);
                            break;
                        case 1:
                            levelImage = BitmapFactory.decodeResource(getResources(), R.drawable.warning_level1);
                            break;
                        case 2:
                            levelImage = BitmapFactory.decodeResource(getResources(), R.drawable.warning_level2);
                            break;
                        case 3:
                            levelImage = BitmapFactory.decodeResource(getResources(), R.drawable.warning_level3);
                            break;
                        default:
                            levelImage = null;
                            break;
                    }
                    String title = valorExtra + " - Gravidade: " + valorControlo + "/3";
                    String desc="";
                    switch (valorTipoSensor){
                        case "tmp":
                            desc = "Descrição: " + valorDescricao + "\nTipo do Sensor: " + valorTipoSensor + "\nValor da Medição: " + valor + "ºC"
                                    + "\nLimite: " + valorLimite + "ºC";
                            break;
                        case "mov":
                            desc = "Descrição: " + valorDescricao + "\nTipo do Sensor: " + valorTipoSensor + "\nValor da Medição: " + valor;
                            break;
                        case "luz":
                            desc = "Descrição: " + valorDescricao + "\nTipo do Sensor: " + valorTipoSensor + "\nValor da Medição: " + valor + "lux"
                                    + "\nLimite: " + valorLimite + "lux";
                            break;
                        case "hum":
                            desc = "Descrição: " + valorDescricao + "\nTipo do Sensor: " + valorTipoSensor + "\nValor da Medição: " + valor + "%"
                                    + "\nLimite: " + valorLimite + "%";
                            break;
                        default:
                            break;
                    }

                    createNotification(title, desc, levelImage);
                }
            }
            switch (controlo_value) {
                case 0:
                    hora.setTextColor(Color.GREEN);
                    tipoSensor.setTextColor(Color.GREEN);
                    valorMedicao.setTextColor(Color.GREEN);
                    limite.setTextColor(Color.GREEN);
                    descricao.setTextColor(Color.GREEN);
                    controlo.setTextColor(Color.GREEN);
                    extra.setTextColor(Color.GREEN);
                    break;
                case 1:
                    hora.setTextColor(Color.YELLOW);
                    tipoSensor.setTextColor(Color.YELLOW);
                    valorMedicao.setTextColor(Color.YELLOW);
                    limite.setTextColor(Color.YELLOW);
                    descricao.setTextColor(Color.YELLOW);
                    controlo.setTextColor(Color.YELLOW);
                    extra.setTextColor(Color.YELLOW);
                    break;
                case 2:
                    hora.setTextColor(Color.rgb(255, 120, 0));
                    tipoSensor.setTextColor(Color.rgb(255, 120, 0));
                    valorMedicao.setTextColor(Color.rgb(255, 120, 0));
                    limite.setTextColor(Color.rgb(255, 120, 0));
                    descricao.setTextColor(Color.rgb(255, 120, 0));
                    controlo.setTextColor(Color.rgb(255, 120, 0));
                    extra.setTextColor(Color.rgb(255, 120, 0));
                    break;
                case 3:
                    hora.setTextColor(Color.RED);
                    tipoSensor.setTextColor(Color.RED);
                    valorMedicao.setTextColor(Color.RED);
                    limite.setTextColor(Color.RED);
                    descricao.setTextColor(Color.RED);
                    controlo.setTextColor(Color.RED);
                    extra.setTextColor(Color.RED);
                    break;
                default:
                    break;
            }

            row.addView(controlo);
            row.addView(extra);
            row.addView(hora);
            row.addView(tipoSensor);
            row.addView(valorMedicao);
            row.addView(limite);
            row.addView(descricao);

            table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            if(contador == tamanho)
                System.out.println("Tamanho: " + sp.getInt("tamanho", 0) + " | Contador: " + contador + " | LastSize: " + sp.getInt("lastSize", 0));

        }

        if (date.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
            SharedPreferences.Editor editor0 = sp.edit().putInt("lastID", mostRecentEntry);
            editor0.apply();
        }

        if (date.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
            SharedPreferences.Editor editor = sp.edit().putInt("timePref", mostRecentData);
            editor.apply();
        }

        SharedPreferences.Editor editor2 = sp.edit().putInt("refreshPref", 0);
        editor2.apply();
    }

    private int dpAsPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }

    public void refreshButton(View v) {
        getAlertas();
        listAlertas();
    }

    public void medicoesTemperatura(View v) {
        Intent i = new Intent(this, MedicoesTemperaturaActivity.class);
        startActivity(i);
    }

    public NotificationCompat.Builder createNotification(String title, String description, Bitmap levelImage){
        Intent i = new Intent(this, AlertasGlobaisActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alertas_notification")
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(levelImage)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
        return builder;
    }
}
