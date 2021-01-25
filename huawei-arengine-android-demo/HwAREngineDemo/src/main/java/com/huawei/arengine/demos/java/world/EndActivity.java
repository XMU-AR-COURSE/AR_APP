package com.huawei.arengine.demos.java.world;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.arengine.demos.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EndActivity extends AppCompatActivity {
    private TextView Title;
    private TextView table;
    private RecyclerView ranks;
    private RanksAdapter adapter;//声明适配器
    ScrollView scv;
    private Button Btn;
    private EditText id_input;
    String score ;
    private String url;
    List<Ranks> rank_list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        /*接收WorldActivity传入的分数*/
        Intent intent = getIntent();
        score = intent.getStringExtra("sc");
        /*关联UI组件*/
        Btn = findViewById(R.id.submit);
        id_input = findViewById(R.id.id_input);
        ranks = findViewById(R.id.ranks);
        scv = findViewById(R.id.scrollView);
        table = findViewById(R.id.tt);
        Title=findViewById(R.id.title);
        /*显示得分*/
        Title.setText("恭喜您\n您的得分为：" + score + "!");
        /*设置点击时间，即上传分数*/
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*用户输入的ID为空，提示用户重新输入*/
                if (id_input.getText().toString().equals(""))
                    Toast.makeText(EndActivity.this, "请输入您的ID", Toast.LENGTH_SHORT).show();
                else {
                    /*用户输入完成，隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(id_input.getWindowToken(), 0);//隐藏软键盘
                    /*生成url,传入Submit_score(string url)中，上传*/
                    String id = id_input.getText().toString();
                    //url前面部分替换成在3.3节中提及地址
                    url = "http://172.16.13.62:8080/upload_score?name=" + id + "&score=" + score;
                    Submit_score(url);
                }
            }
        });
    }
    /*
     *上传分数并接收10条信息, 采用GET, 接收Json
     */
    public void Submit_score(final String Urls) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Reader read;
                BufferedReader bufferReader;
                HttpURLConnection con = null;
                try {
                    URL url = new URL(Urls);
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(8000);
                    con.setRequestMethod("GET");
                    con.setReadTimeout(8000);
                    read = new InputStreamReader(con.getInputStream());
                    bufferReader = new BufferedReader(read);
                    //获取服务器返回的字符串
                    String str;//读取每一行数据
                    StringBuffer buffer = new StringBuffer();//接受全部数据
                    while ((str = bufferReader.readLine()) != null) {
                        buffer.append(str + "\n");
                    }
                    JSONArray rank_json = new JSONArray(buffer.toString());
                    for (int i = 0; i < rank_json.length(); i++) {
                        JSONObject object = rank_json.getJSONObject(i);
                        String rank = String.valueOf((i + 1)); //排名
                        String id = object.getJSONObject("fields").get("name").toString();//ID
                        String score = object.getJSONObject("fields").get("score").toString();//分数
                        String time = object.getJSONObject("fields").get("time").toString().replace("T", " ").substring(0, 19);//时间
                        Ranks r = new Ranks();
                        r.setRank(rank);
                        r.setId(id);
                        r.setScore(score);
                        r.setTime(time);
                        rank_list.add(r);
                    }
                    //关闭连接
                    read.close();
                    con.disconnect();
                    /*显示排行榜*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Btn.setVisibility(View.GONE);
                            id_input.setVisibility(View.GONE);
                            scv.setVisibility(View.VISIBLE);
                            table.setVisibility(View.VISIBLE);
                            Title.setText("排行榜");
                            adapter = new RanksAdapter(rank_list, EndActivity.this);
                            ranks.setLayoutManager(new LinearLayoutManager(EndActivity.this));
                            ranks.setLayoutManager(new LinearLayoutManager(EndActivity.this));
                            ranks.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }).start();
    }
}