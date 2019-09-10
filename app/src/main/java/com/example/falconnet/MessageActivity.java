package com.example.falconnet;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import 	android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileOutputStream;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MyApp";
    final static String textViewTexKey = "TEXTVIEW_TEXT";
    String Message = null;
    String server_name = "https://falconnetserver.000webhostapp.com";

    ListView lv; // полоса сообщений
    EditText et;
    Button bt;
    SQLiteDatabase chatDBlocal;
    String author, client;
    INSERTtoChat insert_to_chat; // класс отправляет новое сообщение на сервер
    UpdateReceiver upd_res; // класс ждет сообщение от сервиса и получив его -
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        // получим 2 переменные по которым будем отбирать инфу из БД:
        // author - от чьего имени идет чат
        // client - с кем чатимся
        Intent intent = getIntent();
        author = intent.getStringExtra("author");
        client = intent.getStringExtra("client");

        Log.i("chat", "+ ChatActivity - открыт author = " + author
                + " | client = " + client);

        lv = (ListView) findViewById(R.id.message_list);
        et = (EditText) findViewById(R.id.edit_message);
        bt = (Button) findViewById(R.id.show_message);

        chatDBlocal = openOrCreateDatabase("chatDBlocal.db",
                Context.MODE_PRIVATE, null);
        chatDBlocal
                .execSQL("CREATE TABLE IF NOT EXISTS chat (_id integer primary key autoincrement, author, client, data, text)");

        // Создаём и регистрируем широковещательный приёмник

        upd_res = new UpdateReceiver();
        registerReceiver(upd_res, new IntentFilter(
                "by.andreidanilevich.action.UPDATE_ListView"));

        create_lv();
    }

    @SuppressLint("SimpleDateFormat")
    public void create_lv() {

        Cursor cursor = chatDBlocal.rawQuery(
                "SELECT * FROM chat WHERE author = '" + author
                        + "' OR author = '" + client + "' ORDER BY data", null);
        if (cursor.moveToFirst()) {
            // если в базе есть элементы соответствующие
            // нашим критериям отбора

            // создадим массив, создадим hashmap и заполним его результатом
            // cursor
            ArrayList<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> hm;

            do {
                // мое сообщение !!!
                // если автор сообщения = автор
                // и получатель сообщения = клиент
                if (cursor.getString(cursor.getColumnIndex("author")).equals(
                        author)
                        && cursor.getString(cursor.getColumnIndex("client"))
                        .equals(client)) {

                    hm = new HashMap<>();
                    hm.put("author", author);
                    hm.put("client", "");
                    hm.put("list_client", "");
                    hm.put("list_client_time", "");
                    hm.put("list_author",
                            cursor.getString(cursor.getColumnIndex("text")));
                    hm.put("list_author_time", new SimpleDateFormat(
                            "HH:mm - dd.MM.yyyy").format(new Date(cursor
                            .getLong(cursor.getColumnIndex("data")))));
                    mList.add(hm);

                }

                // сообщение мне !!!!!!!
                // если автор сообщения = клиент
                // и если получатель сообщения = автор
                if (cursor.getString(cursor.getColumnIndex("author")).equals(
                        client)
                        && cursor.getString(cursor.getColumnIndex("client"))
                        .equals(author)) {

                    hm = new HashMap<>();
                    hm.put("author", "");
                    hm.put("client", client);
                    hm.put("list_author", "");
                    hm.put("list_author_time", "");
                    hm.put("list_client",
                            cursor.getString(cursor.getColumnIndex("text")));
                    hm.put("list_client_time", new SimpleDateFormat(
                            "HH:mm - dd.MM.yyyy").format(new Date(cursor
                            .getLong(cursor.getColumnIndex("data")))));
                    mList.add(hm);

                }

            } while (cursor.moveToNext());

            // покажем lv
            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
                    mList, R.layout.list, new String[] { "list_author",
                    "list_author_time", "list_client",
                    "list_client_time", "author", "client" },
                    new int[] { R.id.list_author, R.id.list_author_time,
                            R.id.list_client, R.id.list_client_time,
                            R.id.author, R.id.client });

            lv.setAdapter(adapter);
            cursor.close();

        }

        Log.i("chat",
                "+ ChatActivity ======================== обновили поле чата");

    }

    public void onSaveInstanceState(Bundle outState) {
        TextView messageText = (TextView) findViewById(R.id.show_message);
        Message = messageText.getText().toString();
        outState.putString(textViewTexKey, Message);
        super.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle SavedInstanceState) {
        super.onRestoreInstanceState(SavedInstanceState);
        String Text = SavedInstanceState.getString(textViewTexKey);
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
        TextView textView1 = new TextView(this);
        //Добавляем его в RelativeLayout
        relativeLayout.addView(textView1);
        textView1.setId(R.id.show_message);
        //Добавляем параметры для отображения сообщений
        RelativeLayout.LayoutParams messageParams = new RelativeLayout.LayoutParams(700, RelativeLayout.LayoutParams.WRAP_CONTENT);
        messageParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        messageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        messageParams.rightMargin = 50;
        textView1.setLayoutParams(messageParams);
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        textView1.setSingleLine(false);
        textView1.setMinLines(1);
        textView1.setMaxLines(100);
        //Выставляем в поле отображения сообщения сообщение пользователя
        Text += "\n\n\n";
        textView1.setText(Text);
    }

    public void SendMessage(View view) {
        //Находим layout и поле отправки сообщений
        EditText editText =  findViewById(R.id.edit_message);
        //TextView TextView = (TextView) findViewById(R.id.show_message);
        // Получае текст данного текстового поля
        String message = editText.getText().toString();
        //Проверяем есть ли текст в сообщении
        if (message != null) {
            //Создаем новое текстовое поле
            TextView textView1 = new TextView(this);
            //Добавляем его в RelativeLayout
            //lv.addView(textView1);
            textView1.setId(R.id.show_message);
            //Добавляем параметры для отображения сообщений
            RelativeLayout.LayoutParams messageParams = new RelativeLayout.LayoutParams(700, RelativeLayout.LayoutParams.WRAP_CONTENT);
            messageParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            messageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            messageParams.rightMargin = 50;
            textView1.setLayoutParams(messageParams);
            textView1.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            textView1.setSingleLine(false);
            textView1.setMinLines(1);
            textView1.setMaxLines(100);
            //Выставляем в поле отображения сообщения сообщение пользователя
            message += "\n\n\n";
            textView1.setText(message);
            Message = message;
            String simpleFileName = "messeges.txt";
            try {
                FileOutputStream out = this.openFileOutput(simpleFileName, MODE_PRIVATE);
                out.write(Message.getBytes());
                out.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.getMessage());
            }
            //Выставляем значение поля отправки
            if (!et.getText().toString().trim().equals("")) {

                // кнопку сделаем неактивной
                bt.setEnabled(false);

                // если чтото есть - действуем!
                insert_to_chat = new INSERTtoChat();
                insert_to_chat.execute();
            } else {
                // если ничего нет - нечего и писать
                et.setText("");
            }
        }
    }
    private class INSERTtoChat extends AsyncTask<Void, Void, Integer> {

        HttpURLConnection conn;
        Integer res;

        protected Integer doInBackground(Void... params) {

            try {

                // соберем линк для передачи новой строки
                String post_url = server_name
                        + "/chat?action=insert&author="
                        + URLEncoder.encode(author, "UTF-8")
                        + "&client="
                        + URLEncoder.encode(client, "UTF-8")
                        + "&text="
                        + URLEncoder.encode(et.getText().toString().trim(),
                        "UTF-8");

                Log.i("chat",
                        "+ ChatActivity - отправляем на сервер новое сообщение: "
                                + et.getText().toString().trim());

                URL url = new URL(post_url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(1000); // ждем 1 сек
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();

                res = conn.getResponseCode();
                Log.i("chat", "+ ChatActivity - ответ сервера (200 - все ОК): "
                        + res.toString());

            } catch (Exception e) {
                Log.i("chat",
                        "+ ChatActivity - ошибка соединения: " + e.getMessage());

            } finally {
                // закроем соединение
                conn.disconnect();
            }
            return res;
        }

        protected void onPostExecute(Integer result) {

            try {
                if (result == 200) {
                    Log.i("chat", "+ ChatActivity - сообщение успешно ушло.");
                    // сбросим набранный текст
                    et.setText("");
                }
            } catch (Exception e) {
                Log.i("chat", "+ ChatActivity - ошибка передачи сообщения:\n"
                        + e.getMessage());
                Toast.makeText(getApplicationContext(),
                        "ошибка передачи сообщения", Toast.LENGTH_SHORT).show();
            } finally {
                // активируем кнопку
                bt.setEnabled(true);
            }
        }
    }

    // ресивер приёмник ждет сообщения от FoneService
    // если сообщение пришло, значит есть новая запись в БД - обновим ListView
    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("chat",
                    "+ ChatActivity - ресивер получил сообщение - обновим ListView");
            create_lv();
        }
    }
    public void MainBack(View view)
    {
        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(backIntent);
    }

}
