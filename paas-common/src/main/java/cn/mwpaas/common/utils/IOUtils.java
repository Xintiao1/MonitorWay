package cn.mwpaas.common.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author phzhou
 * @ClassName IOUtils
 * @CreateDate 2019/3/1
 * @Description
 */
@Slf4j
public class IOUtils {

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * byte[] 转String
     *
     * @param bytes
     * @return
     */
    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    /**
     * inputStream 转 String
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String toString(InputStream inputStream) throws IOException {
        byte[] bytes;
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return toString(bytes);
    }

    /**
     * 拷贝文件流，使用NIO
     *
     * @param in  输入
     * @param out 输出
     * @return 拷贝的字节数
     * @throws IOException IO异常
     */
    public static long copy(FileInputStream in, FileOutputStream out) throws IOException {
        if (in == null) {
            throw new NullPointerException("FileInputStream is null!");
        }
        if (out == null) {
            throw new NullPointerException("FileOutputStream is null!");
        }
        final FileChannel inChannel = in.getChannel();
        final FileChannel outChannel = out.getChannel();

        return inChannel.transferTo(0, inChannel.size(), outChannel);
    }

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 文件转字节
     *
     * @param file
     * @return
     */
    public static byte[] fileToBytes(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            log.error("错误返回 :{}",e);
        } catch (IOException e) {
            log.error("错误返回 :{}",e);
        }
        return buffer;
    }

    /**
     * byte[]转成file
     * @param buffer
     * @param filePath
     * @throws IOException
     */
    public static void bytesToFile(byte[] buffer, final String filePath) throws IOException {
        File file = new File(filePath);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;
        try {
            output = new FileOutputStream(file);
            bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(buffer);
        } finally {
            if (null != bufferedOutput) {
                bufferedOutput.close();
            }
            if (null != output) {
                output.close();
            }
        }
    }

    /**
     * 文件压缩
     *
     * @param sourcePath 源文件
     */
    public static byte[] zip(String sourcePath) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        File sourceFile = new File(sourcePath);
        compress(sourceFile, zos, sourceFile.getName());
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    /**
     * 解压Zip
     *
     * @author: xuk
     * @date: 2018/2/2
     */
    public static byte[] unZip(byte[] data) throws Exception {
        byte[] b = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ZipInputStream zip = new ZipInputStream(bis);
        while (zip.getNextEntry() != null) {
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = zip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
        }
        zip.close();
        bis.close();
        return b;
    }

    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos zip输出流
     * @param name 压缩后的名称
     * @throws IOException
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws IOException {
        if (sourceFile.isFile()) {
            ZipEntry entry = new ZipEntry(name);
            byte[] data = fileToBytes(sourceFile);
            entry.setSize(data.length);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            zos.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
            } else {
                for (File file : listFiles) {
                    compress(file, zos, sourceFile.getName() + "/" + file.getName());
                }
            }
        }
    }

    /**
     * inputSteam 转 byte
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        // 280619
        ////System.out.println(zip("/Users/zhoupenghong/Documents/picture/44.png").length);
    }
}
