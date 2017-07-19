package top.xututu.app.astroclockadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private String urlstring = "http://ftp.astron.ac.cn/v4/bin/astro.php?lon=#lon&lat=#lat&lang=zh-CN&ac=0&unit=metric&tzshift=0";
    private URL url;
    private Bitmap bitmap;

    private URL getUrl(double lon,double lat){
        String result = urlstring.
                replaceAll("#lon",String.format("%.3f",lon)).
                replaceAll("#lat",String.format("%.3f",lat));

        Log.e("urlstring",result);
        try {
            url = new URL(result);
        } catch (MalformedURLException e) {

        }
        return url;
    }

    private Location getLocation(){
        // 位置
        LocationManager locationManager;
        LocationListener locationListener;
        Location location;
        String contextService = Context.LOCATION_SERVICE;
        String provider;
//        double lat;
//        double lon;
        locationManager = (LocationManager) getSystemService(contextService);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
        criteria.setAltitudeRequired(false);// 不要求海拔
        criteria.setBearingRequired(false);// 不要求方位
        criteria.setCostAllowed(true);// 允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
        // 从可用的位置提供器中，匹配以上标准的最佳提供器
        provider = locationManager.getBestProvider(criteria, true);
        // 获得最后一次变化的位置
        location = locationManager.getLastKnownLocation(provider);
        return location;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != bitmap && null != imageView) {
                imageView.setImageBitmap(bitmap);

            }
        }
    };

    private Runnable load = new Runnable() {
        @Override
        public void run() {
            try {
                Location location = getLocation();
                getUrl(location.getLongitude(),location.getLatitude());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            } catch (IOException e) {
                System.out.println("io exception");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = (ImageView) findViewById(R.id.imageView);
                new Thread(load).start();
            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
