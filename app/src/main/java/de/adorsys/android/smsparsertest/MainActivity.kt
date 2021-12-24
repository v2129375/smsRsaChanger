package de.adorsys.android.smsparsertest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.adorsys.android.smsparser.SmsConfig
import de.adorsys.android.smsparser.SmsReceiver
import de.adorsys.android.smsparser.SmsTool


class MainActivity : AppCompatActivity() {
    private lateinit var smsSenderTextView: TextView
    private lateinit var smsMessageTextView: TextView
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var tv1:TextView
    private lateinit var tv2:TextView
    private lateinit var ed1:EditText
    private lateinit var ed2:EditText
    private lateinit var ed3:EditText

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsReceiver.INTENT_ACTION_SMS == intent.action) {
                val receivedSender = intent.getStringExtra(SmsReceiver.KEY_SMS_SENDER)
                val receivedMessage = intent.getStringExtra(SmsReceiver.KEY_SMS_MESSAGE)
                smsSenderTextView.text = getString(R.string.text_sms_sender_number,
                        receivedSender ?: "NO NUMBER")
                smsMessageTextView.text = getString(R.string.text_sms_message,
                        receivedMessage ?: "NO MESSAGE")
//                if(receivedMessage == "hello"){
//                    Log.v("TTT","hello")
//
//                }else{
//                    Log.v("TTT","fail")
//                }
                tv1.text = receivedMessage
                if(receivedMessage == ed1.text.toString()){
                    tv2.text = "認證成功"
                    tv2.setBackgroundColor(Color.GREEN)
                    val publicKey = JschSSHKeyGenerator.genkey(receivedMessage,this@MainActivity).toString()

                    //新建线程去操作网络
                    val thread = Thread {
                        try {
                            JschSSHKeyGenerator.uploadkey(this@MainActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    thread.start()



                    //val publicKey = TestKey.getPublicKey(receivedMessage).toString()
                    ed2.setText(publicKey)
                    //sendMsssage(publicKey)
                    sendMessageMuti(publicKey)

                }else{
                    tv2.text = "認證失敗"
                    tv2.setBackgroundColor(Color.RED)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == SmsTool.REQUEST_CODE_ASK_PERMISSIONS && (grantResults.size <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, getString(R.string.warning_permission_not_granted),
                    Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + applicationContext.packageName)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        SmsConfig.initializeSmsConfig(
                "key",
                "end",
                ed3.text.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SmsTool.requestSMSPermission(this)

        }
        val permissionCheck = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if(permissionCheck==PackageManager.PERMISSION_DENIED){
            val s=  arrayOfNulls<String>(1);
            s[0] = Manifest.permission.SEND_SMS
            ActivityCompat.requestPermissions(this,s, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();
                val s=  arrayOfNulls<String>(1);
                s[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
                ActivityCompat.requestPermissions(this, s, 100);
        }







    }

    override fun onPause() {
        unRegisterReceiver()
        super.onPause()
    }

    override fun onResume() {
        registerReceiver()
        super.onResume()
    }

    private fun initViews() {
        smsSenderTextView = findViewById(R.id.sms_sender_text_view)
        smsMessageTextView = findViewById(R.id.sms_message_text_view)
        tv1 = findViewById(R.id.tv1)
        ed1 = findViewById(R.id.ed1)
        tv2 = findViewById(R.id.tv2)
        ed2 = findViewById(R.id.ed2)
        ed3 = findViewById(R.id.ed3)
        smsSenderTextView.text = getString(R.string.text_sms_sender_number, "")
        smsMessageTextView.text = getString(R.string.text_sms_message, "")
    }

    private fun registerReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsReceiver.INTENT_ACTION_SMS)
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun unRegisterReceiver() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

//    private fun sendMsssage(s:String){
//        val sms = SmsManager.getDefault()
//        sms.sendTextMessage("0967314043", null, s, null, null)
////        var intent = Intent()
////        intent.action = Intent.ACTION_SENDTO//发短信的action
////        intent.data = Uri.parse("smsto:0967314043")//smsto:后面的是收信人，可以随便改
////        intent.putExtra("sms_body", s)//这里的第二个参数是短信内容
////        startActivity(intent)
//    }

    private fun sendMessageMuti(content: String) {
        val smsManager = SmsManager.getDefault()
        val divideContents: List<String> = smsManager.divideMessage(content)
        Log.e("txrjsms", "divide into " + divideContents.size + " parts.")
        for (text in divideContents) {
            Log.i("txrjsms", text + "(length:" + text.length + ")")
            smsManager.sendTextMessage(ed3.text.toString(), null, text, null,null);
        }
    }
}