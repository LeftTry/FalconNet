package com.example.falconnet;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AdapterView;

import com.example.falconnet.ui.login.LoginActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.xmlpull.v1.XmlPullParser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String server_name = "https://falconnetserver.000webhostapp.com";
    Spinner spinner_author, spinner_client;
    String author, client;
    Button open_chat_btn, open_chat_reverce_btn, delete_server_chat;
    private static final String TAG = "MyApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6915715635284813/8780276489");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        XmlPullParser xpp = getResources().getXml(R.xml.userinfo);
        UserResourceParser parser = new UserResourceParser();
        if(parser.parse(xpp)) {
            for (User user : parser.getUsers()) {
                if (user.username == null || user.password == null) {
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Log.i("chat", "+ MainActivity - запуск приложения");

        open_chat_btn = (Button) findViewById(R.id.open_chat_btn);
        open_chat_reverce_btn = (Button) findViewById(R.id.open_chat_reverce_btn);
        delete_server_chat = (Button) findViewById(R.id.delete_server_chat);

        // запустим FoneService
        this.startService(new Intent(this, FoneService.class));

        // заполним 2 выпадающих меню для выбора автора и получателя сообщения
        // 5 мужских и 5 женских имен
        // установим слушателей
        /*
        spinner_author.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] { "Петя",
                "Вася", "Коля", "Андрей", "Сергей", "Оля", "Лена",
                "Света", "Марина", "Наташа" }));
        spinner_client.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] { "Петя",
                "Вася", "Коля", "Андрей", "Сергей", "Оля", "Лена",
                "Света", "Марина", "Наташа" }));
        spinner_client.setSelection(5);

        open_chat_btn.setText("Открыть чат: "
                + spinner_author.getSelectedItem().toString() + " > "
                + spinner_client.getSelectedItem().toString());
        open_chat_reverce_btn.setText("Открыть чат: "
                + spinner_client.getSelectedItem().toString() + " > "
                + spinner_author.getSelectedItem().toString());

        spinner_author
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View itemSelected, int selectedItemPosition,
                                               long selectedId) {

                        author = spinner_author.getSelectedItem().toString();

                        open_chat_btn.setText("Открыть чат: "
                                + spinner_author.getSelectedItem().toString()
                                + " > "
                                + spinner_client.getSelectedItem().toString());
                        open_chat_reverce_btn.setText("Открыть чат: "
                                + spinner_client.getSelectedItem().toString()
                                + " > "
                                + spinner_author.getSelectedItem().toString());
                    }


        public void onNothingSelected(AdapterView<?> parent) {
        }
    });

		spinner_client
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                View itemSelected, int selectedItemPosition,
        long selectedId) {

            client = spinner_client.getSelectedItem().toString();

            open_chat_btn.setText("Открыть чат: "
                    + spinner_author.getSelectedItem().toString()
                    + " > "
                    + spinner_client.getSelectedItem().toString());
            open_chat_reverce_btn.setText("Открыть чат: "
                    + spinner_client.getSelectedItem().toString()
                    + " > "
                    + spinner_author.getSelectedItem().toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    });
    */
}

    // откроем чат с выбранным автором и получателем
    public void open_chat(View v) {
        // быстрая проверка
        if (author.equals(client)) {
            // если автор и получатель одинаковы
            // чат не открываем
            Toast.makeText(this, "author = client !", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // откроем нужный чат author > client
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            intent.putExtra("author", author);
            intent.putExtra("client", client);
            startActivity(intent);
        }
    }

    // откроем чат с выбранным автором и получателем, только наоборот
    public void open_chat_reverce(View v) {
        // быстрая проверка
        if (author.equals(client)) {
            // если автор и получатель одинаковы
            // чат не открываем
            Toast.makeText(this, "author = client !", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // откроем нужный чат client > author
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            intent.putExtra("author", client);
            intent.putExtra("client", author);
            startActivity(intent);
        }
    }

    // отправим запрос на сервер о удалении таблицы с чатами
    public void delete_server_chats(View v) {

        Log.i("chat", "+ MainActivity - запрос на удаление чата с сервера");

        delete_server_chat.setEnabled(false);
        delete_server_chat.setText("Запрос отправлен. Ожидайте...");

        DELETEfromChat delete_from_chat = new DELETEfromChat();
        delete_from_chat.execute();
    }

    // удалим локальную таблицу чатов
    // и создадим такуюже новую
    public void delete_local_chats(View v) {

        Log.i("chat", "+ MainActivity - удаление чата с этого устройства");

        SQLiteDatabase chatDBlocal;
        chatDBlocal = openOrCreateDatabase("chatDBlocal.db",
                Context.MODE_PRIVATE, null);
        chatDBlocal.execSQL("drop table chat");
        chatDBlocal
                .execSQL("CREATE TABLE IF NOT EXISTS chat (_id integer primary key autoincrement, author, client, data, text)");

        Toast.makeText(getApplicationContext(),
                "Чат на этом устройстве удален!", Toast.LENGTH_SHORT).show();
    }

// отправим запрос на сервер о удалении таблицы с чатами
// если он пройдет - таблица будет удалена
// если не пройдет (например нет интернета или сервер недоступен)
// - покажет сообщение
private class DELETEfromChat extends AsyncTask<Void, Void, Integer> {

    Integer res;
    HttpURLConnection conn;

    protected Integer doInBackground(Void... params) {

        try {
            URL url = new URL(server_name + "/chat.php?action=delete");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000); // ждем 10сек
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();
            res = conn.getResponseCode();
            Log.i("chat", "+ MainActivity - ответ сервера (200 = ОК): "
                    + res.toString());

        } catch (Exception e) {
            Log.i("chat",
                    "+ MainActivity - ответ сервера ОШИБКА: "
                            + e.getMessage());
        } finally {
            conn.disconnect();
        }

        return res;
    }

    protected void onPostExecute(Integer result) {

        try {
            if (result == 200) {
                Toast.makeText(getApplicationContext(),
                        "Чат на сервере удален!", Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Ошибка выполнения запроса.", Toast.LENGTH_SHORT)
                    .show();
        } finally {
            // сделаем кнопку активной
            delete_server_chat.setEnabled(true);
            delete_server_chat.setText("Удалить все чаты на сервере!");
        }
    }
}


    @Override
    public void onBackPressed() {
        Log.i("chat", "+ MainActivity - выход из приложения");
        finish();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent postIntent = new Intent(this, PostActivity.class);
            startActivity(postIntent);
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void Send(View view) {
        try {
            Intent messageIntent = new Intent(this, MessageActivity.class);
            messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(messageIntent);
            }
        catch (Exception ex)
        {
            Toast toast = Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
