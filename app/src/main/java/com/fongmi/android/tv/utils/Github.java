package com.fongmi.android.tv.utils;

public class Github {

    // 1. 更新为你的 GitHub 仓库地址
    private static final String GITHUB_LATEST = "https://github.com/dyr1980/Silent1566webhtv/releases/latest/download";
    private static final String GITHUB_RELEASE = "https://github.com/dyr1980/Silent1566webhtv/releases/download";
    private static final String GITHUB_UPDATE_CHANNEL = GITHUB_RELEASE + "/update-channel";

    // 2. CNB 镜像地址（已暂时注释，以后恢复时取消注释，并确保填入你的CNB仓库路径）
    // private static final String CNB_MANIFEST = "https://cnb.cool/dyr1980/webhtv-release/-/git/raw/main/apk";

    // 3. 更新为你的 GitHub API 地址
    private static final String GITHUB_API = "https://api.github.com/repos/dyr1980/Silent1566webhtv/releases/tags";
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/dyr1980/Silent1566webhtv/releases";
    private static final String GITHUB_RELEASE_ASSETS_API = "https://api.github.com/repos/dyr1980/Silent1566webhtv/releases/assets";

    public static String getChannelAsset(String name) {
        return GITHUB_UPDATE_CHANNEL + "/" + name;
    }

    // [CNB 恢复] 恢复 CNB 时，取消下方代码注释，并注释或删除现在的 GitHub 回退逻辑
    public static String getCnbMirrorAsset(String name) {
        // return CNB_MANIFEST + "/" + name;
        return getGithubLatestAsset(name); // 暂时回退到 GitHub
    }

    public static String getCnbAsset(String name) {
        // 【临时禁用 CNB】直接返回 GitHub 地址
        return getGithubLatestAsset(name);
        
        // [CNB 恢复] 恢复 CNB 时，取消下方代码注释，并注释上方 return
        // return getCnbMirrorAsset(name);
    }

    public static String getGithubLatestAsset(String name) {
        return GITHUB_LATEST + "/" + name;
    }

    public static String getGithubReleaseAsset(String tag, String name) {
        return GITHUB_RELEASE + "/" + tag + "/" + name;
    }

    public static String getJson(String name) {
        return getCnbAsset(name + ".json");
    }

    public static String getJson(String name, String channel) {
        if ("beta".equals(channel)) return getCnbAsset(name + "-beta.json");
        return getJson(name);
    }

    public static String getApk(String name) {
        return getCnbAsset(name + ".apk");
    }

    public static String getApk(String name, String channel) {
        if ("beta".equals(channel)) return getCnbAsset(name + "-beta.apk");
        return getApk(name);
    }

    public static String getAsset(String name, String channel) {
        return getCnbAsset(name);
    }

    public static String getReleaseApi(String tag) {
        return GITHUB_API + "/" + tag;
    }

    public static String getReleasesApi() {
        return GITHUB_RELEASES_API + "?per_page=20";
    }

    public static String getLatestReleaseApi() {
        return GITHUB_RELEASES_API + "/latest";
    }

    public static String getReleaseAssetApi(long id) {
        return GITHUB_RELEASE_ASSETS_API + "/" + id;
    }
}
