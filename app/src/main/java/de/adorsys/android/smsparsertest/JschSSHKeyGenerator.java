package de.adorsys.android.smsparsertest;

import android.content.Context;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class JschSSHKeyGenerator {
    /* 拼接要傳入linux server的命令*/
    public static String getcmd(Context context) throws IOException {
        String cmd = "";
        //String filepath = "./id_rsa.pub";
        File file = new File(context.getCacheDir(),"id_rsa.pub");
        BufferedReader in = null;
        //in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8"));// 读取文件
        in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));// 读取文件
        String thisLine = null;
        while ((thisLine = in.readLine()) != null) {
            cmd += thisLine;
        }
        in.close();


        return cmd;
    }
    /* 生成keypair於當前目錄，回傳private key字串*/
    public static String genkey(String Uid_h,Context context) {
        String filename = "id_rsa";
        String comment = "";
        String privatekeystring = "";

        JSch jsch = new JSch();

        byte[] passphrase = Uid_h.getBytes(Charset.forName("UTF-8"));

        try {
            //Genkey
            KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);

            ByteArrayOutputStream S1 = new ByteArrayOutputStream();
            kpair.writePrivateKey(S1, passphrase);
            //kpair.writePrivateKey("id_rsa",passphrase);
            privatekeystring = S1.toString();
            //System.out.println(privatekeystring);
            //File file = new File("./id_rsa");
            File file = new File(context.getCacheDir(),"id_rsa");
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.println(privatekeystring);

            //kpair.writePublicKey(filename + ".pub", comment);
            kpair.writePublicKey(context.getCacheDir()+"/"+filename + ".pub", comment);

            System.out.println("Finger print: " + kpair.getFingerPrint());
            kpair.dispose();
            //File pvtKey = new File("keypair");
//            File pvtKey = new File(context.getCacheDir(),"keypair");
//            pvtKey.setWritable(false);

            /* 權限管理
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(pvtKey.toPath(), perms);
            */
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            return privatekeystring;
        }


    }
    /* 將生成的key通過ssh傳輸到Linux server*/
    public static void uploadkey(Context context) throws JSchException, InterruptedException, IOException {
        ChannelExec channelExec = null;
        BufferedReader inputStreamReader = null;
        BufferedReader errInputStreamReader = null;
        StringBuilder runLog = new StringBuilder("");
        StringBuilder errLog = new StringBuilder("");
        JSch jsch = new JSch();
        Session session = null;


        try {
            String USER = "labguest";
            String PASSWORD = "mir123";
            String HOST = "140.112.91.55";
            int DEFAULT_SSH_PORT = 10230;
            String cmd = ""; //命令

            //建構命令
            cmd = getcmd(context);
            //cmd = "echo " + cmd + " > /ssd/labguest/publickey.pub";
            cmd = "echo " + cmd + " >> /ssd/labguest/.ssh/authorized_keys";

            session = jsch.getSession(USER, HOST, DEFAULT_SSH_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();


            channelExec = (ChannelExec) session.openChannel("exec");

            //channelExec.setCommand("ifconfig;");
            channelExec.setCommand(cmd);
            channelExec.connect();

            //獲取標準輸入流
            inputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            //獲取標準錯誤輸入流
            errInputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));

            //記錄命令執行 log
            String line = null;
            while ((line = inputStreamReader.readLine()) != null) {
                runLog.append(line).append("\n");
            }

            //記錄命令執行錯誤 log
            String errLine = null;
            while ((errLine = errInputStreamReader.readLine()) != null) {
                errLog.append(errLine).append("\n");
            }

            //輸出 shell 命令執行紀錄檔
            System.out.println("exitStatus=" + channelExec.getExitStatus() + ", openChannel.isClosed="
                    + channelExec.isClosed());
            System.out.println("Run log:");
            System.out.println(runLog.toString());
            System.out.println("Error log:");
            System.out.println(errLog.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (errInputStreamReader != null) {
                    errInputStreamReader.close();
                }

                if (channelExec != null) {
                    channelExec.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static void main(String[] arg) throws IOException {
//        String S2 = genkey("z"); //传入User id
//        System.out.println(S2);
//
//        try {
//            uploadkey();
//        }
//        catch (Exception e){}


    }

}