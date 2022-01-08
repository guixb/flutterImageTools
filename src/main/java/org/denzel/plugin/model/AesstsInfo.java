package org.denzel.plugin.model;

import org.denzel.plugin.CommonUtil;

public class AesstsInfo {
    public String refPath;

    public String varName;

    public AesstsInfo(String basePath, String fileName) {
        fileName = CommonUtil.filterXX(fileName);
        this.refPath = basePath + fileName;
        this.varName = buildName(fileName);
    }

    private String buildName(String fileName) {
        StringBuilder sb = new StringBuilder();
        int index = fileName.indexOf(".");
        if(index > 0) {
            fileName = fileName.substring(0, index);
        }
        String[] strs = fileName.split("_");
        for(int i = 0;i < strs.length; i++) {
            sb.append(i == 0?strs[i] : toFirstUpper(strs[i]));
        }
        return sb.toString();
    }

    private String toFirstUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean equals(Object obj) {
        if(this.refPath == null || this.varName == null || !(obj instanceof AesstsInfo)) {
            return false;
        } else {
            return this.refPath.equals(((AesstsInfo)obj).refPath) && this.varName.equals(((AesstsInfo)obj).varName);
        }
    }

    public String toDartCode() {
        return String.format("const String %s = '%s';\n\n", varName, refPath);
    }
}
