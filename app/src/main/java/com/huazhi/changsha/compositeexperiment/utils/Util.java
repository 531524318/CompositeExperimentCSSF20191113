package com.huazhi.changsha.compositeexperiment.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@SuppressWarnings("deprecation")
@SuppressLint("SimpleDateFormat")
public class Util {
    public static final String TAG = "Util";

    /**
     * 计算一个对象所占字节数
     *
     * @param o对象
     *            ,该对象必须继承Serializable接口即可序列化
     * @return
     * @throws IOException
     */
    public static int getObjectSize(final Object o) throws IOException {
        if (o == null) {
            return 0;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
        ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(o);
        out.flush();
        buf.close();

        return buf.size();
    }

    public static int occupyof(byte variable) {
        return 1;
    }

    public static int occupyof(byte variable[]) {
        return variable.length;
    }

    public static int getSize(Object f) {
        // int size = Util.getObjectSize(o);
        int size = 0;
        Field field[] = f.getClass().getDeclaredFields();
        for (int i = 0; i < field.length; i++) {
            String varName = field[i].getName();
            try {
                // 获取原来的访问控制权限
                boolean accessFlag = field[i].isAccessible();
                // 修改访问控制权限
                field[i].setAccessible(true);
                Object object = field[i].get(f);
                System.out.println(varName);
                System.out.println(object);
                size += occupyof((Byte) object);
                System.out.println(size);
                // 恢复访问控制权限
                field[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return size;
        // return (101); // 50+50+1
    }

    public static String getVersionName(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String version = packInfo.versionName;

        return version;
    }

    // 年月日时分秒：20140103083026
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd   hh:mm:ss");
        String date = sDateFormat.format(new Date());
        date = date.replace("-", "");
        date = date.replace(":", "");
        date = date.replace(" ", "");
        Log.d("hhx", "getCurrentTime: " + date);
        return date;
    }

    public static void writeFile(String path, String content) {
        File file;
        try {
            file = new File(path);
            if (file.exists() == false) {
                file.createNewFile();
                file = new File(path);
            }

            FileOutputStream fw = new FileOutputStream(file);
            fw.write(content.getBytes());
            fw.flush();
            fw.close();
        } catch (Exception e) {
        }
    }

    public static void writeFile(String path, byte[] content) {
        Log.d("hhx", "writeFile------" + content);
        File file = new File(path);
        ;
        try {
            if (file.exists() == false) {
                file.createNewFile();
                file = new File(path);
            }

            FileOutputStream fw = new FileOutputStream(file);
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (Exception e) {
        }
    }

    public static void appendFile(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendFile(String fileName, byte[] content) {
        Log.d("hhx", "appendFile------" + content);
        if (content != null) {
            try {
                // 打开一个随机访问文件流，按读写方式
                RandomAccessFile randomFile = new RandomAccessFile(fileName,
                        "rw");
                // 文件长度，字节数
                long fileLength = randomFile.length();
                // 将写文件指针移到文件尾。
                randomFile.seek(fileLength);
                randomFile.write(content);
                randomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] Interger2bytes(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static int bytes2Integer(byte[] byteVal) {
        int result = 0;
        for (int i = 0; i < byteVal.length; i++) {
            int tmpVal = (byteVal[i] << (8 * (3 - i)));
            switch (i) {
                case 0:
                    tmpVal = tmpVal & 0xFF000000;
                    break;
                case 1:
                    tmpVal = tmpVal & 0x00FF0000;
                    break;
                case 2:
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 3:
                    tmpVal = tmpVal & 0x000000FF;
                    break;
            }
            result = result | tmpVal;
        }
        return result;
    }

    public static InputStream String2InputStream(String str)
            throws UnsupportedEncodingException {
        ByteArrayInputStream stream = new ByteArrayInputStream(
                str.getBytes("GBK"));
        // ByteArrayInputStream stream = new ByteArrayInputStream(
        // str.getBytes("UTF-8"));
        return stream;
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    public static void setSP(Context context, String fileName, String key, Object value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        }
        editor.commit();
    }

    public static Object getSP(Context context, String fileName, String key, Class<?> clazz) {
        Object object = null;
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String name = clazz.getName().substring(10);
        if (name.equals("Boolean")) {
            object = sp.getBoolean(key, false);
        } else if (name.equals("Float")) {
            object = sp.getFloat(key, -1);
        } else if (name.equals("Integer")) {
            object = sp.getInt(key, -1);
        } else if (name.equals("Long")) {
            object = sp.getLong(key, -1);
        } else if (name.equals("String")) {
            object = sp.getString(key, "");
        }
        return object;
    }

    public static void clearSP(Context context, String fileName, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
    public static Object getSP(Context context, String fileName, String key,
                               Class<?> clazz, String defaultValue) {
        Object object = null;
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        String name = clazz.getName().substring(10);
        if (name.equals("Boolean")) {
            object = sp.getBoolean(key, false);
        } else if (name.equals("Float")) {
            object = sp.getFloat(key, -1);
        } else if (name.equals("Integer")) {
            object = sp.getInt(key, -1);
        } else if (name.equals("Long")) {
            object = sp.getLong(key, -1);
        } else if (name.equals("String")) {
            object = sp.getString(key, defaultValue);
        }
        return object;
    }

    /**
     * 按字节长度截取字符串
     *
     * @param str
     *            将要截取的字符串参数
     * @param toCount
     *            截取的字节长度
     * @param more
     *            字符串末尾补上的字符串
     * @return 返回截取后的字符串
     */
    public static String subString(String str, int toCount, String more) {
        int reInt = 0;
        String reStr = "";
        if (str == null)
            return "";
        char[] tempChar = str.toCharArray();
        for (int kk = 0; (kk < tempChar.length && toCount > reInt); kk++) {
            String s1 = String.valueOf(tempChar[kk]);
            byte[] b = s1.getBytes();
            reInt += b.length;
            reStr += tempChar[kk];
        }
        if (toCount == reInt || (toCount == reInt - 1))
            reStr += more;
        return reStr;
    }

    /**
     * 取数据的后toCount位
     **/
    public static String getScanText(String str_tmp,int toCount) {
        String strTmp = "";
        try {
            if (!TextUtils.isEmpty(str_tmp) && str_tmp.length() > toCount) {
                strTmp = str_tmp.substring(str_tmp.length() - toCount, str_tmp.length());
            }
        } catch (Exception e) {

        }
        return strTmp;
    }


    /**
     * 根据isChecked控制targetEt密码的显示/
     *
     * @param targetEt
     * @param isChecked
     */
    public static void handlerPWDCheck(EditText targetEt, boolean isChecked) {
        if (!isChecked) {
            // display password text, for example "123456"
            targetEt.setTransformationMethod(HideReturnsTransformationMethod
                    .getInstance());
        } else {
            // hide password, display "."
            targetEt.setTransformationMethod(PasswordTransformationMethod
                    .getInstance());
        }
        targetEt.setSelection(targetEt.getText().length());
    }

    // 将手机号码的中间四位转换成*号
    public static String toEncrypt(String str) {
        char[] b = str.toCharArray();
        char[] a = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            a[i] = b[i];
            if (i >= 3 && i <= 6) {
                a[i] = '*';
            }
        }
        String strEncrypt = new String(a);
        return strEncrypt;
    }

    // 这字符串中的指定位置插入字符串
    public static String insertStr(String srcStr, String insertStr, int pos) {
        if (srcStr.equals("")) {
            return "";
        }

        StringBuilder sb = new StringBuilder(srcStr);// 构造一个StringBuilder对象
        int length = srcStr.length();
        if (length > pos) {
            sb.insert(pos - 1, "\n");// 在指定的位置1，插入指定的字符串
        }
        return sb.toString();
    }

    // byte转long
    public static long getLong(byte[] bb) {
        return ((((long) bb[0] & 0xff) << 56) | (((long) bb[1] & 0xff) << 48)
                | (((long) bb[2] & 0xff) << 40) | (((long) bb[3] & 0xff) << 32)
                | (((long) bb[4] & 0xff) << 24) | (((long) bb[5] & 0xff) << 16)
                | (((long) bb[6] & 0xff) << 8) | (((long) bb[7] & 0xff) << 0));
    }

    /**
     * 检测输入是否为中文字符
     *
     * @param str
     * @return boolean
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static boolean isContainSpecial(String str) {

        char mid[] = str.toCharArray();
        boolean iscon = true;
        for (int i = 0; i < mid.length; i++) {
            if ((mid[i] >= 48 && mid[i] <= 57)
                    || (mid[i] >= 65 && mid[i] <= 90)
                    || (mid[i] > 97 && mid[i] <= 122)) {
                iscon = false;

            } else {
                iscon = true;
                return iscon;
            }
        }

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find() || str.contains(" ")) {
            iscon = true;
            return iscon;
        } else {
            iscon = false;
        }
        return iscon;
    }

    /**
     * 校验账号输入是否合法(字母开头，允许5-16字节，允许字母数字下划线)
     *
     * @param str
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean checkAccount(String str) {
        Pattern p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static void showToast(Context context, String info) {
        Toast toast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.e("px", (int) (dpValue * scale + 0.5f) + "");
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.e("px", (int) (pxValue / scale + 0.5f) + "");
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param fontScale
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * base64图片数据转Bitmap
     *
     **/
    public static Bitmap stringtoBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 检查是否为可用图片
     *
     * @param filePath
     *            图片路径
     * @return 图片可用true,其他false
     */
    public static boolean checkIsImage(String filePath) {
        try {
            Bitmap drawable2 = BitmapFactory.decodeFile(filePath);
            if (drawable2 == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean IsImage(String path) {
        boolean b = false;
        FileInputStream inputStream = null;
        try {
            // 从SDCARD下读取一个文件
            inputStream = new FileInputStream(path);
            byte[] buffer = new byte[2];
            // 文件类型代码
            String filecode = "";
            // 文件类型
            String fileType = "";
            // 通过读取出来的前两个字节来判断文件类型
            if (inputStream.read(buffer) != -1) {
                for (int i = 0; i < buffer.length; i++) {
                    // 获取每个字节与0xFF进行与运算来获取高位，这个读取出来的数据不是出现负数
                    // 并转换成字符串
                    filecode += Integer.toString((buffer[i] & 0xFF));
                }
                // 把字符串再转换成Integer进行类型判断
                switch (Integer.parseInt(filecode)) {
                    case 7790:
                        fileType = "exe";
                        break;
                    case 7784:
                        fileType = "midi";
                        break;
                    case 8297:
                        fileType = "rar";
                        break;
                    case 8075:
                        fileType = "zip";
                        break;
                    case 255216:
                        fileType = "jpg";
                        b = true;
                        break;
                    case 7173:
                        fileType = "gif";
                        break;
                    case 6677:
                        fileType = "bmp";
                        break;
                    case 13780:
                        fileType = "png";
                        break;
                    default:
                        fileType = "unknown type: " + filecode;
                }

            }
            System.out.println(fileType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return b;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }

    /**
     * 判断系统中是否存在可以启动的相机应用
     *
     * @return 存在返回true，不存在返回false
     */
    public static boolean hasCamera(Activity mActivity) {
        PackageManager packageManager = mActivity.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 检查网络是否可用
     *
     * @param ctx
     * @return
     */
//    public static boolean isNetworkAvailable(Context ctx) {
//        if (ctx == null)
//            return false;
//        ConnectivityManager conMan = (ConnectivityManager) ctx
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        // mobile Data Network
//        NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
//                .getState();
//        // wifi
//        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//                .getState();
//        // 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
//        if ((mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING)
//                || (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     *
     * 按照压缩比率获取bitmap,数字越大越清晰,压缩比越小
     *
     * @param localImgPath
     *            本地的图片地址
     * @param width
     *            压缩的宽度比率,越大越清晰
     * @return bitmap对象
     */
    @SuppressLint("UseValueOf")
    public static Bitmap getBitmap(String localImgPath, int width) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 可以编码
        BitmapFactory.decodeFile(localImgPath, options);

        // 根据固定宽度，计算高度
        float scaleWidth = ((float) width) / options.outWidth;
        float scaleHeight = scaleWidth * options.outHeight;
        Float f = new Float(scaleHeight);
        int height = f.intValue();

        if (options.outHeight > 0 && options.outWidth > 0) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2; // 长、宽缩小到原来的1/2，像素缩小到原来的1/4，减少内存的消耗
            while (options.outWidth / options.inSampleSize > width
                    && options.outHeight / options.inSampleSize > height) {
                options.inSampleSize++; // 增加压缩比

//                AppLog.i("按照压缩比率获取bitmap,数字越大越清晰,压缩比越小 : h-->"
//                        + options.outHeight + ",w---->" + options.outWidth
//                        + ",ss-->" + options.inSampleSize + ",压缩比-->"
//                        + options.outWidth / options.inSampleSize + "/"
//                        + options.outHeight / options.inSampleSize);
            }
            options.inSampleSize--;

            bitmap = BitmapFactory.decodeFile(localImgPath, options);
            if (bitmap != null) {
                bitmap = Bitmap
                        .createScaledBitmap(bitmap, width, height, false); // TODO
                // OOM
            }
        }
        return bitmap;
    }

    /**
     * 获取图片的旋转角度
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap
     *            需要旋转的图片
     * @param degree
     *            指定的旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        if (bitmap == null) {
            return null;
        }
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    /**
     * 添加图片到本地并规定压缩比例，100默认原图
     *
     * @param bitmap
     *            图片数据
     * @param savePath
     *            保存的图片路径
     * @param quality
     *            100默认原图
     * @return 处理后的图片文件
     */
    public static File saveBitmap(Bitmap bitmap, String savePath, int quality) {
        Log.d(TAG,"保存的图片路径====" + savePath);
        if (bitmap == null)
            return null;
        try {
            File f = new File(savePath);
            if (f.exists())
                f.delete();
            FileOutputStream fos = new FileOutputStream(f);
            f.createNewFile();
            // 把Bitmap对象解析成流
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            Log.d("bitmap", bitmap.getRowBytes() + "");
            fos.flush();
            fos.close();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return f;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return null;
        }

    }

    public static void saveBitmap(String picPath, Bitmap picBmp) {

        File f = new File(picPath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            picBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取旋转角度处理后的文件
     *
     * @param path
     *            图片文件路径
     * @param bit
     *            图片数据
     * @return
     */
    public static File getRotateFile(String path, Bitmap bit) {
        int degree = 0;
        degree = getBitmapDegree(path);
        bit = rotateBitmapByDegree(bit, degree);
        if (bit == null) {
            return null;
        }
        return saveBitmap(bit, path, 100);
    }

    /**
     * 刷新图库
     *
     * @param path
     * @param context
     */
    public static void scanFile(String path, Context context) {

        MediaScannerConnection.scanFile(context, new String[] {path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    // 导致TextView异常换行的原因：安卓默认数字、字母不能为第一行以后每行的开头字符，因为数字、字母为半角字符
    // 所以我们只需要将半角字符转换为全角字符即可，方法如下
    public static String toSBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 测试当前摄像头能否被使用
     */
    public static boolean isCanUseCamera() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }
        return canUse;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context
     *            Context
     * @return true 表示网络可用
     */
    public static boolean isCurNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 当前网络是连接的
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        // 当前所连接的网络可用
                        return true;
                    }
                }
            }
        } else {
            LogUtils.d("当前网络不可判断，context为空");
        }
        return false;
    }

    /**
     * 判断一个int有几位数
     *
     * @param x
     * @return
     */
    public static int sizeOfInt(int x) {
        int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999,
                999999999, Integer.MAX_VALUE };
        for (int i = 0;; i++)
            if (x <= sizeTable[i])
                return i + 1;
    }

    /** 检查设备是否提供摄像头 */

    public static boolean checkCameraHardware(Context context) {

        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // 摄像头存在
            return true;
        } else {
            // 摄像头不存在
            return false;
        }
    }

    /** 安全获取Camera对象实例的方法 */

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // 试图获取Camera实例
        } catch (Exception e) {
            // 摄像头不可用（正被占用或不存在）
            Log.d(TAG,"异常:" + e);
        }
        return c; // 不可用则返回null
    }

    private static float sDensity = 0;

    /**
     * DP转换为像素
     *
     * @param context
     * @param nDip
     * @return
     */
    public static int dipToPixel(Context context, int nDip) {
        if (sDensity == 0) {
            final WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            sDensity = dm.density;
        }
        return (int) (sDensity * nDip);
    }

    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * post上传文件
     *
     * @param urlStr
     * @param fileMap
     * @return
     */
    public synchronized static String formUpload(String urlStr,
                                                 Map<String, String> fileMap) {
        String res = "";
        HttpURLConnection conn = null;
        String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);

            OutputStream out = new DataOutputStream(conn.getOutputStream());

            // file
            if (fileMap != null) {
                Iterator<?> iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    @SuppressWarnings("rawtypes")
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();
                    /*
                     * String contentType = new MimetypesFileTypeMap()
                     * .getContentType(file);
                     */
                    String contentType = "application/octet-stream";
                    if (contentType == null || contentType.equals("")) {
                        contentType = "application/octet-stream";
                    }
                    try {
                        StringBuffer strBuf = new StringBuffer();
                        strBuf.append("\r\n").append("--").append(BOUNDARY)
                                .append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\""
                                + inputName + "\"; filename=\"" + filename
                                + "\"\r\n");
                        strBuf.append("Content-Type:" + contentType
                                + "\r\n\r\n");

                        out.write(strBuf.toString().getBytes());

                        DataInputStream in = new DataInputStream(
                                new FileInputStream(file));
                        int bytes = 0;
                        byte[] bufferOut = new byte[1024];

                        while ((bytes = in.read(bufferOut)) != -1) {
                            out.write(bufferOut, 0, bytes); // 容易崩
                        }
                        in.close();
                    } catch (Exception e) {
                        System.out.println("发送出错。" + urlStr);
                        e.printStackTrace();
                    }
                }
            }

            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();

            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            System.out.println("发送POST请求出错。" + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }

    public static Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Drawable loadImageFromNetwork(String imageUrl) {
        Drawable drawable = null;
        try {
            // 可以在这里通过文件名来判断，是否本地有此图片
            drawable = Drawable.createFromStream(
                    new URL(imageUrl).openStream(), "image.jpg");
        } catch (IOException e) {
            Log.d("test", e.getMessage());
        }
        if (drawable == null) {
            Log.d("test", "null drawable");
        } else {
            Log.d("test", "not null drawable");
        }

        return drawable;
    }

    /**
     * 获取流
     *
     * @param URLName
     * @return
     * @throws Exception
     */
    public static InputStream getUrlFile(String URLName) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int HttpResult = 0; // 服务器返回的状态
        URL url = new URL(URLName); // 创建URL
        URLConnection urlconn = url.openConnection(); // 试图连接并取得返回状态码urlconn.connect();
        HttpURLConnection httpconn = (HttpURLConnection) urlconn;
        HttpResult = httpconn.getResponseCode();
        System.out.println(HttpResult);
        if (HttpResult != HttpURLConnection.HTTP_OK) { // 不等于HTTP_OK说明连接不成功
            System.out.print("连接失败！");
        } else {
            int filesize = urlconn.getContentLength(); // 取数据长度
            System.out.println(filesize);
            BufferedInputStream bis = new BufferedInputStream(
                    urlconn.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(os);
            byte[] buffer = new byte[1024]; // 创建存放输入流的缓冲
            int num = -1; // 读入的字节数
            while (true) {
                num = bis.read(buffer); // 读入到缓冲区
                if (num == -1) {
                    bos.flush();
                    break; // 已经读完
                }
                bos.flush();
                bos.write(buffer, 0, num);
            }
            bos.close();
            bis.close();
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
        return bis;
    }

    public static void inputstreamtofile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            try {
                while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取网络视频的缩略图
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;

        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    /**
     * 随机生成压缩文件的名称
     *
     * @return
     */
    public static String generalFileName() {
        return UUID.randomUUID().toString() + ".zip";
    }

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getInputStreamFromUrl(String urlStr)
            throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public static File write2SDFromInput(String directory, String fileName,
                                         InputStream input) {
        File file = null;
        String SDPATH = Environment.getExternalStorageDirectory().toString();
        FileOutputStream output = null;
        File dir = new File(SDPATH + directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            file = new File(dir + File.separator + fileName);
            file.createNewFile();
            output = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            while ((input.read(buffer)) != -1) {
                output.write(buffer);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 解压缩一个文件
     *
     * @param zipFile
     *            压缩文件
     * @param folderPath
     *            解压缩的目标目录
     * @throws IOException
     *             当解压缩过程出错时抛出
     */
    public static void upZipFile(File zipFile, String folderPath)
            throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), "GB2312");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[1024 * 1024];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
        }
        zf.close();
    }




    /**
     * 将视屏Uri转换为绝对路径
     *
     * @param selectedVideoUri
     * @param contentResolver
     * @return
     */
    public static String contentUriToFilePath(Uri selectedVideoUri,
                                              ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = { MediaStore.MediaColumns.DATA };

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn,
                null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    /**
     * 下载文件
     *
     * @param videoPath
     * @param folderName
     * @param FileName
     * @return
     * @throws MalformedURLException
     */
//    public static File download(String videoPath, String folderName,
//                                String fileName) {
//        long currentTimeMillis = System.currentTimeMillis();
//        File fileDownload = null;
//        URL url;
//        try {
//            url = new URL(videoPath);
//            URLConnection conn = url.openConnection();
//            InputStream is = conn.getInputStream();
//            String dirName = Environment.getExternalStorageDirectory() + "/"
//                    + folderName;
//            File file = new File(dirName);
//            if (!file.exists()) {
//                file.mkdir();
//            }
//            fileDownload = new File(file, fileName);
//            if (fileDownload.exists()) {
//                fileDownload.delete();
//            } else {
//                fileDownload.createNewFile();
//            }
//            // 记录车辆视频是否下载成功
//            Constant.IsVideoDownloadIng.add(fileDownload.toString());
//            byte[] bs = new byte[1024];
//            int len;
//            OutputStream os = new FileOutputStream(fileDownload);
//            while (Constant.Interrupt[0] && (len = is.read(bs)) != -1) {
//                os.write(bs, 0, len);
//            }
//            os.close();
//            is.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (fileDownload != null && fileDownload.exists()) {
//                deleteFileOrFilePath(fileDownload);
//            }
//            LogUtils.e("---線程异常退出：" + e.toString() + "---");
//        }
//        if (fileDownload != null && fileDownload.exists())
//            Constant.IsVideoDownloadIng.remove(fileDownload.toString());
//        Log.i(TAG,"----下载时间为"
//                + (System.currentTimeMillis() - currentTimeMillis) / 1000 + "秒");
//        return fileDownload;
//    }

    /**
     * 下载文件
     *
     * @param videoPath
     * @param folderName
     * @param fileName
     * @return
     * @throws MalformedURLException
     */
    public static File downloadZZM(List<String> videoPath, String folderName,
                                   String fileName) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        File fileDownload = null;
        for (int i = 0; i < videoPath.size(); i++) {
            URL url = new URL(videoPath.get(i));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                String dirName = Environment.getExternalStorageDirectory()
                        + "/" + folderName;
                File file = new File(dirName);
                if (!file.exists()) {
                    file.mkdir();
                }
                fileDownload = new File(file, i + fileName);
                if (fileDownload.exists()) {
                    fileDownload.delete();
                }
                byte[] bs = new byte[1024];
                int len;
                OutputStream os = new FileOutputStream(fileDownload);
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                long currentTimeMillis2 = System.currentTimeMillis();
                System.out
                        .println("下载时间为"
                                + (currentTimeMillis2 - currentTimeMillis)
                                / 1000 + "秒");
                os.close();
                is.close();
            }
        }
        long currentTimeMillis1 = System.currentTimeMillis();
        System.out.println("下载时间是=" + (currentTimeMillis1 - currentTimeMillis)
                / 1000 + "秒");
        return fileDownload;
    }

    /**
     *
     * @param data
     * @return
     */
    public static String getPicDataFromAlum(ContentResolver contentResolver,
                                            Intent data) {
        String pic_path = "";
        if (data != null) {
            Uri selectedImage = data.getData();

            if (selectedImage == null) {
                return pic_path;
            }

            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = contentResolver.query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor == null) {
                pic_path = selectedImage.getPath();
                File file = new File(pic_path);
                if (file.isFile() && Util.IsImage(pic_path)) {
                    return pic_path;
                } else {
                    return "";
                }
            }
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            pic_path = cursor.getString(columnIndex);
            cursor.close();
        } else {
            return pic_path;
        }

        return pic_path;
    }

    /**
     * 图片路径返回bitmap
     *
     * @param filePath
     * @return
     */
//    public static Bitmap initBitmap(String filePath) {
//        Bitmap thumbnail = null;
//        if (filePath == null || filePath.equals("")) {
//            return thumbnail;
//        }
//
//        thumbnail = Util.getBitmap(filePath, Constant.IMG_COMPRESS_WIDTH);
//
//        if (thumbnail != null) {
//            int degree = Util.getBitmapDegree(filePath);
//            thumbnail = Util.rotateBitmapByDegree(thumbnail, degree);
//        }
//        return thumbnail;
//    }

    public static byte[] InputStream2Bytes(InputStream inStream) {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    public static InputStream Byte2InputStream(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return bais;
    }

    /**
     * 通过文件路径获取语音的时长
     *
     * @param string
     * @return
     */
    public static String gettime(String string) { // 使用此方法可以直接在后台获取音频文件的播放时间，而不会真的播放音频
        MediaPlayer player = new MediaPlayer(); // 首先你先定义一个mediaplayer
        try {
            player.setDataSource(string); // String是指音频文件的路径
            player.prepare(); // 这个是mediaplayer的播放准备 缓冲

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {// 监听准备

            @Override
            public void onPrepared(MediaPlayer player) {
                int size = player.getDuration();
                @SuppressWarnings("unused")
                String timelong = size / 1000 + "s";
            }
        });
        double size = player.getDuration();// 得到音频的时间
        String timelong1 = (int) Math.ceil((size / 1000)) + "s";// 转换为秒 单位为''
        player.stop();// 暂停播放
        player.release();// 释放资源
        return timelong1; // 返回音频时间
    }

    /**
     * 半角字符与全角字符混乱所致：这种情况一般就是汉字与数字、英文字母混用
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * String转Date
     *
     * @param dateString
     * @return
     */
//    public static Date stringToDate(String dateString) {
//        ParsePosition position = new ParsePosition(0);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//                GlobalDefs.TIME_FORMAT);
//        Date dateValue = simpleDateFormat.parse(dateString, position);
//        return dateValue;
//    }

    /**
     * long时间转换成String时间
     *
     * @param longDate
     * @return
     */
//    public static String longToStringDate(long longDate) {
//        Date date = new Date(longDate);
//        SimpleDateFormat df = new SimpleDateFormat(GlobalDefs.TIME_FORMAT);
//        return df.format(date);
//    }

    /**
     * 获取当前运行的activity名字
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getRunningActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity
                .getClassName();
        Log.d(TAG,"当前运行的activity名字====" + runningActivity);
        return runningActivity;
    }

    // 删除文件夹或文件
    @SuppressLint("SimpleDateFormat")
    private static void deleteFileOrFilePath(File mFileDir) {
        if (!mFileDir.exists()) {
            return;
        }
        /**
         * 重命名防止文件锁
         */
        String tmpPath = mFileDir.getParent() + File.separator
                + System.currentTimeMillis();
        File tmp = new File(tmpPath);
        mFileDir.renameTo(tmp);
        File[] files = tmp.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] filesDir = file.listFiles();
                    for (File file1 : filesDir) {
                        file1.delete();
                    }
                }
                file.delete();
            }
        }
        tmp.delete();
    }

    /**
     * 获取视频第一帧图片
     *
     * @param string
     *            本地视频的绝对路径
     * @return 返回視頻的第一幀
     */
    public static Bitmap getVideoPic(String string, Context context) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(string);
        } catch (Exception e) {
            Toast.makeText(context, "视频加载失败", Toast.LENGTH_SHORT).show();
            return null;
        }
        Bitmap frameAtTime = mediaMetadataRetriever.getFrameAtTime(0);
        return frameAtTime;
    }

    /**
     * 获取本地图片
     *
     * @param path
     * @return
     */
    // public static Bitmap getBitmap(Context context, String path) {
    // try {
    // InputStream is = new FileInputStream(path);
    // BitmapFactory.Options opts = new BitmapFactory.Options();
    // opts.inTempStorage = new byte[100 * 1024];
    // opts.inPreferredConfig = Bitmap.Config.RGB_565;
    // opts.inPurgeable = true;
    // opts.inSampleSize = 4;
    // opts.inInputShareable = true;
    // return BitmapFactory.decodeStream(is, null, opts);
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // return BitmapFactory.decodeResource(context.getResources(),
    // R.drawable.icon_placeholder);
    // }
    // }

    /**
     * Drawable转bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 获取相册中的选择的图片的路径
     *
     * @param context
     * @param data
     * @return
     */
    public static String getPicDataFromAlum(Context context, Intent data) {
        String pic_path = "";
        if (data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor == null) {
                pic_path = selectedImage.getPath();
                File file = new File(pic_path);
                if (file.isFile()) {
                    return pic_path;
                } else {
                    return "";
                }
            }
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            pic_path = cursor.getString(columnIndex);
            cursor.close();
        }
        Log.d(TAG,"从相册中选择图片的路径===" + pic_path);
        return pic_path;
    }

    public static File saveBitmap(Bitmap bitmap, String filePath,
                                  String fileName, int quality) {

        if (bitmap == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(filePath, fileName);

        Log.d(TAG,"保存的图片路径====" + file.getPath());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            // 把Bitmap对象解析成流
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            Log.d("bitmap", bitmap.getRowBytes() + "");
            fos.flush();
            fos.close();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return null;
        }
    }

    /**
     * 创建文件夹（注意与创建文件不一样）
     *
     * @param filePath
     *            文件路径
     */
    public static File creatFile(String filePath) {
        File file = null;
        if (!TextUtils.isEmpty(filePath)) {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 创建文件
     *
     * @param filePath
     *            文件路径
     * @param fileName
     *            文件名
     * @return
     */
    public static File creatFile(String filePath, String fileName) {
        File file = null;
        if (!TextUtils.isEmpty(filePath)) {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(filePath, fileName);
        }
        return file;
    }

    /**
     * 创建文件,以时间命名
     *
     * @param filePath
     *            文件路径
     * @return
     */
    public static File createFileByTime(String filePath) {

        deleteFolderFile(filePath, false);

        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss")
                .format(new Date()) + ".jpg";
        file = new File(filePath, fileName);
        return file;
    }

    /**
     * 创建文件,以UUID命名
     *
     * @param filePath
     *            文件路径
     * @return
     */
    public static File createFileByUUID(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = UUID.randomUUID().toString() + ".jpg";
        file = new File(filePath, fileName);
        return file;
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @param filePath
     * @return
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateAndTime() {
        String date = "";
        date = new SimpleDateFormat("yyyy年MM月dd日hh时mm分ss秒").format(new Date());
        Log.d(TAG,"日期时间==" + date);
        return date;
    }

    public static String getTimeByFormat(String format) {
        String time = "";
        time = new SimpleDateFormat(format).format(new Date());
        return time;
    }

    // 压缩bitmap转文件
    public static File compressBitmapAsFile(Bitmap bitmap, File file) {
        if (file == null && bitmap == null) {
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Log.d(TAG,"压缩后文件大小：" + file.length() / 1024 + "K");
            if (file.length() == 0) {
                return null;
            }
            return file;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    // 压缩bitmap转文件
    public static File compressBitmapAsFile(Bitmap bitmap, String imgUrl) {
        File file = new File(imgUrl);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
//            LogUtils.d("压缩文件大小：" + file.length() / 1024);
            if (file.length() == 0) {
                return null;
            }
            return file;
        } catch (IOException e) {
//            LogUtils.d("" + e);
            Log.e(TAG, e.toString());
            return null;
        }
    }


    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }



    private static boolean isVarifyCode(String Ai, String IDStr) {
        String[] VarifyCode = { "1", "0", "X", "9", "8", "7", "6", "5", "4",
                "3", "2" };
        String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
                "9", "10", "5", "8", "4", "2" };
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(Ai.charAt(i)))
                    * Integer.parseInt(Wi[i]);
        }
        int modValue = sum % 11;
        String strVerifyCode = VarifyCode[modValue];
        Ai = Ai + strVerifyCode;
        if (IDStr.length() == 18) {
            if (Ai.equals(IDStr) == false) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 判断字符串是否为数字,0-9重复0次或者多次
     *
     * @param strnum
     * @return
     */
    private static boolean isNumeric(String strnum) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(strnum);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDate(String strDate) {
        Pattern pattern = Pattern
                .compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = ((Activity) context).managedQuery(contentUri, proj,
                    null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

    public static void setImageView(ImageView iv, String realPath) {
        try {
            InputStream is = new FileInputStream(realPath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inTempStorage = new byte[100 * 1024];
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inPurgeable = true;
            opts.inSampleSize = 4;
            opts.inInputShareable = true;
            Bitmap btp = BitmapFactory.decodeStream(is, null, opts);
            iv.setImageBitmap(btp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * * 获得最佳分辨率 * 注意:因为相机默认是横屏的，所以传参的时候要注意，width和height都是横屏下的 * * @param
     * parameters 相机参数对象 * @param width 期望宽度 * @param height 期望高度 * @return
     */
    public static int[] getBestResolution(Camera.Parameters parameters,
                                          int width, int height) {
        int[] bestResolution = new int[2];// int数组，用来存储最佳宽度和最佳高度
        int bestResolutionWidth = -1;// 最佳宽度
        int bestResolutionHeight = -1;// 最佳高度

        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();// 获得设备所支持的分辨率列表

        int difference = 99999;// 最小差值，初始化市需要设置成一个很大的数

        // 遍历sizeList，找出与期望分辨率差值最小的分辨率
        for (int i = 0; i < sizeList.size(); i++) {
            int differenceWidth = Math.abs(width - sizeList.get(i).width);// 求出宽的差值
            int differenceHeight = Math.abs(height - sizeList.get(i).height);// 求出高的差值
//            LogUtils.d("width:" + sizeList.get(i).width + ",height:"
//                    + sizeList.get(i).height);
            // 如果它们两的和，小于最小差值
            if ((differenceWidth + differenceHeight) < difference) {
                difference = (differenceWidth + differenceHeight);// 更新最小差值
                bestResolutionWidth = sizeList.get(i).width;// 赋值给最佳宽度
                bestResolutionHeight = sizeList.get(i).height;// 赋值给最佳高度
            }
        }
        // 最后将最佳宽度和最佳高度添加到数组中
        bestResolution[0] = bestResolutionWidth;
        bestResolution[1] = bestResolutionHeight;

//        LogUtils.d("最佳分辨率===" + "bestResolutionWidth:" + bestResolution[0]
//                + "bestResolutionHeight:" + bestResolution[1]);
        return bestResolution;// 返回最佳分辨率数组
    }


    /**
     * 获取屏幕宽高
     * @param activity
     * @return
     */
    public static int getWindowWidth(Activity activity) {
        int Width = 400;
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay()
                    .getMetrics(displayMetrics);
            Width = displayMetrics.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Width;
    }


    //----------------------------音频WAV------------------------------------------//

    // 这里得到可播放的音频文件
    public static void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        int sampleRateInHz = 8000;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 2;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        // 缓冲区字节大小
        int bufferSizeInBytes = 8000;

        byte[] data = new byte[bufferSizeInBytes];
        try {
            File file = new File(outFilename);
            if (!file.exists()) {
                file.createNewFile();
            }
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context
                        .getApplicationInfo().processName)) {
                    Log.d(TAG, "EntryActivity isRunningForeGround");
                    return true;
                }
            }
        }
        Log.d(TAG, "EntryActivity isRunningBackGround");
        return false;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.d(TAG, "EntryActivity isRunningBackGround");
                    return true;
                } else {
                    Log.d(TAG, "EntryActivity isRunningForeGround");
                    return false;
                }
            }
        }
        return false;
    }


    /**
     * 判断2个时间大小
     * yyyy-MM-dd 格式
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getTimeCompareSize(String startTime, String endTime){
        int i=0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//年-月-日
        try {
            Date date1 = dateFormat.parse(startTime);//开始时间
            Date date2 = dateFormat.parse(endTime);//结束时间
            // 1 结束时间小于开始时间 2 开始时间与结束时间相同 3 结束时间大于开始时间
            if (date2.getTime()<date1.getTime()){
                i= 1;
            }else if (date2.getTime()==date1.getTime()){
                i= 2;
            }else if (date2.getTime()>date1.getTime()){
                //正常情况下的逻辑操作.
                i= 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  i;
    }

    /**
     * 处理证件号码（需求要求是搜索后才显示有人员；证件号码显示二代身份证的前3位后4位，中间显示11位*）
     **/
    public static  String getPersonNo(String no) {
        String returnNo = "";//返回的数据
        String str = "";//星号

        if (!TextUtils.isEmpty(no)) {
            int length = no.length();

            if (length < 8) {
                returnNo = no;
            } else if (length > 7) {
                int hidLenght = length - 7;

                //星号处理
                StringBuilder hiddenSb = new StringBuilder(20);
                for (int i = 0; i < hidLenght; i++) {
                    hiddenSb.append("*");
                }
                str = hiddenSb.toString();

                //显示
                StringBuilder disSb = new StringBuilder(no);
                disSb.replace(3, 3 + hidLenght, str);
                returnNo = disSb.toString();
            }
        }

        return returnNo;
    }
}
