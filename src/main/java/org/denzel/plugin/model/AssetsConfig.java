package org.denzel.plugin.model;

import java.util.List;

public class AssetsConfig {

    public String name;

    public String imageDirPath;

    public String dartFilePath;

    public boolean hasMakeDartFile;

    public String proPath;

    public List<AesstsInfo> assetsList;

    public AssetsConfig(String proPath, String name) {
        this.proPath = proPath;
        this.name = name;
    }

}
