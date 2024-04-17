package com.example.contentapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> titles;
    private ArrayList<String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        titles = new ArrayList<>();
        links = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        // Xử lý sự kiện khi người dùng chọn một mục trong danh sách
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = links.get(position);
                openWebPage(url);
            }
        });

        // Tải RSS feed từ Google, Facebook hoặc Twitter
        new DownloadXmlTask().execute("https://news.google.com/rss", "https://www.facebook.com/feeds/page.php?id=YOUR_FACEBOOK_PAGE_ID&format=rss20", "https://api.twitter.com/2/tweets/search/recent?query=YOUR_SEARCH_QUERY&max_results=10");
    }

    // Phương thức mở liên kết trong trình duyệt web bên trong ứng dụng
    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // Lớp AsyncTask để tải và phân tích RSS feed từ URL
    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();

                // Phân tích RSS feed và lấy danh sách tiêu đề và liên kết
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputStream);
                doc.getDocumentElement().normalize();
                NodeList itemList = doc.getElementsByTagName("item");
                for (int i = 0; i < itemList.getLength(); i++) {
                    Element item = (Element) itemList.item(i);
                    String title = item.getElementsByTagName("title").item(0).getTextContent();
                    String link = item.getElementsByTagName("link").item(0).getTextContent();
                    titles.add(title);
                    links.add(link);
                }
                inputStream.close();
            } catch (IOException | ParserConfigurationException | SAXException e) {
                Log.e("Error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Cập nhật danh sách tiêu đề trong ListView sau khi đã tải và phân tích xong
            adapter.notifyDataSetChanged();
        }
    }
}
