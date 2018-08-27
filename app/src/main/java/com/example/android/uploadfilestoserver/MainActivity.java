package com.example.android.uploadfilestoserver;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.provider.MediaStore.MediaColumns.DATA;

public class MainActivity extends AppCompatActivity {

    String serverUrl = "https://elsayedmohammed70.000webhostapp.com/uploadfile.php";
    FloatingActionButton fab;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        textView = findViewById( R.id.responseTextView );
        fabAction();

    }

    public void fabAction() {
        fab = findViewById( R.id.fab_upload );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText( "Click The Button To Upload Files To Server!!" );
                Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
                intent.setType( "*/*" );
                startActivityForResult( intent, 10 );
            }
        } );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filePath = RealPathUtil.getRealPath( this, uri );
            Log.d( "File Path : ", filePath );
            FileUpload( filePath );
        }
    }

    public void FileUpload(final String filePath) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                HttpURLConnection uploadConnection = null;
                DataOutputStream outputStream;
                String boundary = "********";
                String CRLF = "\r\n";
                String Hyphens = "--";
                int bytesRead, bytesAvailable, bufferSize;
                int maxBufferSize = 1024 * 1024;
                byte[] buffer;
                File ourFile = new File( filePath );
                try {
                    FileInputStream fileInputStream = new FileInputStream( ourFile );
                    URL url = new URL( serverUrl );
                    uploadConnection = (HttpURLConnection) url.openConnection();
                    uploadConnection.setDoInput( true );
                    uploadConnection.setDoOutput( true );
                    uploadConnection.setRequestMethod( "POST" );

                    uploadConnection.setRequestProperty( "Connection", "Keep-Alive" );
                    uploadConnection.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + boundary );
                    uploadConnection.setRequestProperty( "uploaded_file", filePath );

                    outputStream = new DataOutputStream( uploadConnection.getOutputStream() );

                    outputStream.writeBytes( Hyphens + boundary + CRLF );

                    outputStream.writeBytes( "Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + filePath + "\"" + CRLF );
                    outputStream.writeBytes( CRLF );

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min( bytesAvailable, maxBufferSize );
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read( buffer, 0, bufferSize );

                    while (bytesRead > 0) {
                        outputStream.write( buffer, 0, bufferSize );
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min( bytesAvailable, maxBufferSize );
                        bytesRead = fileInputStream.read( buffer, 0, bufferSize );
                    }

                    outputStream.writeBytes( CRLF );
                    outputStream.writeBytes( Hyphens + boundary + Hyphens + CRLF );

                    InputStreamReader resultReader = new InputStreamReader( uploadConnection.getInputStream() );
                    BufferedReader reader = new BufferedReader( resultReader );
                    ;
                    String line = "";
                    String response = "";
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }

                    final String finalResponse = response;
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            textView.setText( finalResponse );
                        }
                    } );

                    fileInputStream.close();
                    outputStream.flush();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        } ).start();


    }


}
