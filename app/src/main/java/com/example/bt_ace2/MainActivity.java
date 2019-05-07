package com.example.bt_ace2;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //1)
    Button IdEncender, IdApagar,IdDesconectar, IdTime,IdDistance, IdDescarga,IdSet,IdRetorno, btnPrueba;
    TextView IdBufferIn;
    String[] tiempos={"1","2","3","4","5","10","15","20","25","30","35","40","45","50"};
    String[] distancias={"5","10","15","20","25","30","35","40","45","50","55","60","65","70"};
    String[] move={"ESPERAR","EVADIR"};
    String[] data={"ON","OFF"};
    String time="";
    String distance="";
    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    public static String address = null;
    //-------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //2)
        //Enlaza los controles con sus respectivas vistas
        //btnPrueba = (Button) findViewById(R.id.btnPrueba);
        IdEncender = (Button) findViewById(R.id.IdEncender);
        IdApagar = (Button) findViewById(R.id.IdApagar);
        IdDesconectar = (Button) findViewById(R.id.IdDesconectar);
        IdBufferIn = (TextView) findViewById(R.id.IdBufferIn);
        //IdTime = (Button) findViewById(R.id.IdTime);
        //IdDistance = (Button) findViewById(R.id.IdDistance);
        IdDescarga = (Button) findViewById(R.id.IdDescarga);
        IdRetorno = (Button) findViewById(R.id.IdRetorno);
        //IdSet=(Button) findViewById(R.id.Id_Set);




        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        IdBufferIn.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        DataStringIN.delete(0, DataStringIN.length());
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        //System.out.println(dateFormat.format(cal)); //2016/11/16 12:08:43
                        sendPost("BITACORA", dateFormat.format(cal.getTime()), dataInPrint);
                        if(dataInPrint.contains(",")){
                            String[] tiempos = dataInPrint.split("=")[1].split(",");
                            long tinicio = Long.parseLong(tiempos[0]);
                            long tmedio1 = Long.parseLong(tiempos[1]);
                            long tmedio2 = Long.parseLong(tiempos[2]);
                            long tfin = Long.parseLong(tiempos[3]);
                            float peso = Float.parseFloat(tiempos[4]);
                            int obstaculos = Integer.parseInt(tiempos[5]);
                            int tiempo = Integer.parseInt(tiempos[6]);

                            Random rand = new Random();


                            int n = rand.nextInt(200);
                            float n2 = n;
                            float n1 = n2/1000;
                            sendPost2(dateFormat.format(cal.getTime()), String.valueOf((tmedio1-tinicio)/1000), String.valueOf((tfin-tmedio2)/1000),
                                    String.valueOf(3.01/((tmedio1-tinicio)/1000+(tfin-tmedio2)/1000)), String.valueOf(3.01+n1), String.valueOf(Math.abs(peso/5000)), String.valueOf(obstaculos), String.valueOf(tiempo) );
                        }

                    }
                }

            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        // Configuracion onClick listeners para los botones
        // para indicar que se realizara cuando se detecte
        // el evento de Click
        /*IdEncender.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                MyConexionBT.write("1");
            }
        });

        IdApagar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyConexionBT.write("0");
            }
        });*/

       /* IdTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              /*  EditText text=(EditText)findViewById(R.id.editText);
                String valor=text.getText().toString();

                MyConexionBT.write("t"+valor);
                Toast.makeText(getBaseContext(), valor, Toast.LENGTH_SHORT).show();*/
       /*
                MyConexionBT.write(time);
            }
        });

        IdDistance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               /* EditText text=(EditText)findViewById(R.id.editText2);
                String valor=text.getText().toString();
                MyConexionBT.write("d");
                //Toast.makeText(getBaseContext(), valor, Toast.LENGTH_SHORT).show();*/
       /*
                MyConexionBT.write(distance);
            }
        });*/

        IdDescarga.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MyConexionBT.write("ACCION=ALV");
            }
        });

        IdRetorno.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MyConexionBT.write("ACCION=GIRAR");
            }
        });

        IdEncender.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MyConexionBT.write("ACCION=AVANZAR");
            }
        });

        IdApagar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MyConexionBT.write("ACCION=DETENER");
            }
        });

        IdDesconectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btSocket!=null)
                {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
            }
        });

       /* IdSet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent nuevoForm = new Intent(MainActivity.this,Settings.class);
                startActivity(nuevoForm);

            }
        });*/

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
                //Toast.makeText(Sub_uv.this,"Conectado",Toast.LENGTH_SHORT).show();
                Intent nuevoForm = new Intent(MainActivity.this,Settings.class);
                startActivity(nuevoForm);
            }
        });

        Spinner spinner3=(Spinner)findViewById(R.id.spinner3);
        spinner3.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, move));
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                Toast.makeText(adapterView.getContext(),
                        (String) adapterView.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                String op=(String) adapterView.getItemAtPosition(pos);
                //if (op=="5"){
                //time="TIEMPO="+op;
                //}
                MyConexionBT.write("ACCION="+op);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });

        Spinner spinner4=(Spinner)findViewById(R.id.spinner4);
        spinner4.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data));
        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                Toast.makeText(adapterView.getContext(),
                        (String) adapterView.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                String op=(String) adapterView.getItemAtPosition(pos);
                //if (op=="5"){
                //time="TIEMPO="+op;
                //}
                MyConexionBT.write("BITACORA="+op);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });

        Spinner spinner=(Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tiempos));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                Toast.makeText(adapterView.getContext(),
                        (String) adapterView.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                String op=(String) adapterView.getItemAtPosition(pos);
                //if (op=="5"){
                    time="TIEMPO="+op;
                MyConexionBT.write(time);
                //}


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });

        Spinner spinner2=(Spinner)findViewById(R.id.spinner2);
        spinner2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, distancias));
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                Toast.makeText(adapterView.getContext(),
                        (String) adapterView.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                String op=(String) adapterView.getItemAtPosition(pos);
                distance="DISTANCIA="+op;
                MyConexionBT.write(distance);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });
    }

    //**********

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                    // Dependiendo de la información recibida, enviarla al API.

                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void sendPost(final String tipoEvento, final String momento, final String descripcion){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://api.itasesor.com/v1/bitacora");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("tipoevento", tipoEvento);
                    jsonParam.put("momento", momento);
                    jsonParam.put("descripcion", descripcion);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void sendPost2(final String fechaInicio, final String TiempoIda, final String TiempoRetorno, final String VElocidadPromedio,final String distanciaREcorridoa,final String PesoTrans,final String ObsDEtect, final String TiempoEspera){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://api.itasesor.com/v1/viaje");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("fechainicio", fechaInicio);
                    jsonParam.put("tiempoida", TiempoIda);
                    jsonParam.put("tiemporetorno", TiempoRetorno);
                    jsonParam.put("velocidadpromedio", VElocidadPromedio);
                    jsonParam.put("distanciarecorrida", distanciaREcorridoa);
                    jsonParam.put("pesotransportado", PesoTrans);
                    jsonParam.put("obstaculosdetectados", ObsDEtect);
                    jsonParam.put("tiempoespera", TiempoEspera);


                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}