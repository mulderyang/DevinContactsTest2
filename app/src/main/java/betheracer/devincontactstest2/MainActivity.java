package betheracer.devincontactstest2;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    String host_number;
    String contacts;


    TextView textView;

    MyThread workThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        host_number = manager.getLine1Number();

        if(host_number.startsWith("+82")) {
            host_number=host_number.replace("+82", "0");
        }

        textView.setText(host_number);


        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = null;
                StringBuffer sb = new StringBuffer();
                String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);

                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            //message.append("\nNAME:" + name);
                            sb.append(name+"|");

                            //Cursor pCur = cr.query(
                            //        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            //        null,
                            //        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            //        new String[]{id}, null);
                            Cursor pCur = cr.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                                            + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                phoneNumber = pCur.getString(pCur
                                        .getColumnIndex(NUMBER));
                                //message.append("\nPHONE NUMBER:" + phoneNumber);
                                sb.append(phoneNumber+"|");
                            }
                            pCur.close();
                        }
                        //message.append("\n\n");
                        sb.append("||");

                    }
                }


                workThread = new MyThread(sb, host_number);
                workThread.start();


            }
        });


    }




    class MyThread extends Thread {

        StringBuffer buffer = new StringBuffer();
        String contacts;
        String host_number;

        public MyThread(StringBuffer sb, String hostNumber) {
            buffer = sb;
            host_number = hostNumber;
        }

        public void run() {

            contacts = buffer.toString();

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            HttpClient httpclient = new DefaultHttpClient();

            //HttpPost httppost = new HttpPost("http://192.168.0.16/shop/android/test8.php?start_debug=1&send_sess_end=1&debug_start_session=1&debug_session_id=12801&debug_port=10137&debug_host=192.168.109.1%2C127.0.0.1");
            //HttpPost httppost = new HttpPost("http://192.168.0.16/shop/android/test8.php");
            HttpPost httppost = new HttpPost("http://iblind2.godo.co.kr/shop/android/test8.php");
            HttpResponse response = null;

            try {
                nameValuePairs.add(new BasicNameValuePair("myNumber", host_number));
                nameValuePairs.add(new BasicNameValuePair("contacts", contacts));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "EUC-KR"));
                response = httpclient.execute(httppost);
            }catch (Exception e) {
                e.printStackTrace();
            }




        }
    }





}



