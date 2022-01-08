package org.denzel.plugin;

import org.denzel.plugin.model.AesstsInfo;
import org.denzel.plugin.model.AssetsConfig;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommonUtil {

    static final String regImg = "(.+)\\.(png|jpg|jpeg|bmp)$";
    static final String regXX = "^[23](\\.0|\\.)?x$";

    public static boolean isFile(String path, String fileName) {
        return new File(path, fileName).isFile();
    }

    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        File dir = file.getParentFile();
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        if (!file.isFile()) {
            file.createNewFile();
        }
        return file;
    }

    public static File createFile(String dirPath, String fileName) throws IOException {
        return createFile(dirPath + "/" + fileName);
    }

    /**
     * 搜索图片
     * @param dir
     * @param pPath
     * @return
     */
    public static List<AesstsInfo> findImages(File dir, String pPath) {
        final List<AesstsInfo> images = new ArrayList<>();
        final String cPath = pPath + dir.getName() + "/";
        File[] files = dir.listFiles();
        if (files != null) {
            for (File it : files) {
                if (it.isDirectory()) {
                    if (it.getName().matches(regXX)) {
                        findXChildImages(it, cPath, images);
                    } else {
                        images.addAll(findImages(it, cPath));
                    }
                } else if (it.isFile() && it.getName().matches(regImg)) {
                    final AesstsInfo info = new AesstsInfo(cPath, it.getName());
                    if(!images.contains(info)) {
                        images.add(info);
                    }
                }
            }
        }
        return images;
    }

    private static void findXChildImages(File dir, String pPath, List<AesstsInfo> images) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File it : files) {
                if (it.isFile() && it.getName().matches(regImg)) {
                    final AesstsInfo info = new AesstsInfo(pPath, it.getName());
                    if(!images.contains(info)) {
                        images.add(info);
                    }
                }
            }
        }
    }

    public static String filterXX(String fileName) {
        final Pattern pattern = Pattern.compile("@[23](\\.0|\\.)?x\\.");
        final Matcher matcher = pattern.matcher(fileName);
        if(matcher.find()) {
            return fileName.replace(matcher.group(), ".");
        } else {
            return fileName;
        }
    }

    public static AssetsConfig getAssetsConfig(String proPath) {
        AssetsConfig config = new AssetsConfig(proPath, "sync_assets.properties");
        final Properties properties = new Properties();
        try {
            boolean isNew = !CommonUtil.isFile(config.proPath, config.name);
            File propFile = CommonUtil.createFile(config.proPath, config.name);
            InputStream is = new FileInputStream(propFile);
            properties.load(is);

            if(isNew) {
                OutputStream os = new FileOutputStream(propFile);
                config.imageDirPath = "/images";
                properties.setProperty("imageDirPath", config.imageDirPath);
                config.dartFilePath = "/lib/constant/assets_constant.dart";
                properties.setProperty("dartFilePath", config.dartFilePath);
                config.hasMakeDartFile = false;
                properties.setProperty("hasMakeDartFile", "false");
                properties.store(os, "rootImageDir 图片检索的根目录文件\ndartFilePath 生成dart引用的文件路径\nhasMakeDartFile 是否生成dart引用文件");
            } else {
                config.imageDirPath = properties.getProperty("imageDirPath", "/images");
                config.dartFilePath = properties.getProperty("dartFilePath", "/lib/constant/assets_constant.dart");
                config.hasMakeDartFile = "true".equals(properties.getProperty("hasMakeDartFile"));
            }
            // 开始搜索图片
            config.assetsList = findImages(new File(config.proPath, config.imageDirPath), "");
            config.assetsList.sort((o1, o2) -> {
                if(o1.refPath == null || o2.refPath == null) {
                    return -1;
                } else {
                    int depth = o1.refPath.split("/").length - o2.refPath.split("/").length;
                    if(depth == 0) {
                        return o1.refPath.compareTo(o2.refPath);
                    } else {
                        return depth;
                    }
                }
            });
            /// 刷新IDE目录
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(propFile).refresh(true, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
