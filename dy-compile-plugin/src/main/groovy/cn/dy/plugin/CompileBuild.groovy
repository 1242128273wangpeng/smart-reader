package cn.dy.plugin

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.android.builder.model.ProductFlavor
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @desc 定制编译过程
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/22 16:20
 */
class CompileBuild implements Plugin<Project> {

    def ad_version = '1.5.8'

    @Override
    void apply(Project project) {

        String flavorsConfig = project.properties.get("flavorsConfig")
        JSONObject flavorsConfigJson = JSON.parse(new File(flavorsConfig).text)

        // 配置对应变体源码路径
        setFlavorSourcePath(project, flavorsConfigJson)

        // 编译变体
        compileFlavorVariant(project, flavorsConfigJson)
    }

    private void compileFlavorVariant(Project project, JSONObject flavorsConfigJson) {
        project.android {
            libraryVariants.all { variant ->
                ProductFlavor targetFlavor = null
                variant.getProductFlavors().forEach {
                    flavor ->
                        if (flavor.name.equalsIgnoreCase(variant.flavorName)) {
                            targetFlavor = flavor
                        }
                }
                addDependency(flavorsConfigJson, project, targetFlavor)
            }
        }
    }

    private void addDependency(JSONObject flavorsConfigJson, Project project, ProductFlavor targetFlavor) {
        if (targetFlavor == null) return

        JSONObject flavorConfig = flavorsConfigJson.getJSONObject(targetFlavor.name)
        if (flavorConfig != null && flavorConfig.getBoolean("hasAd")) {
            println "携带广告变体为： " + targetFlavor.name
            String configurationName = targetFlavor.name + "Api"
            project.dependencies.add(configurationName,
                    "net.iyouqu.android.common:dycm_ssp_sdk:$ad_version")
        }
    }

    private void setFlavorSourcePath(Project project, JSONObject flavorsConfigJson) {
        project.android {
            sourceSets {
                mfqbxssc {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                qbmfkdxs {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                qbzsydq {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                txtqbdzs {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                txtqbmfxs {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                txtqbmfyd {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }
                zsmfqbxs {
                    java.srcDirs = ['src/main/java', "src/${getName()}/java", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/java"]
                    res.srcDirs = ['src/main/res', "src/${getName()}/res", "src/${flavorsConfigJson.getJSONObject(getName()).getBoolean("hasAd") ? "hasad" : "noad"}/res"]
                }

            }
        }
    }
}