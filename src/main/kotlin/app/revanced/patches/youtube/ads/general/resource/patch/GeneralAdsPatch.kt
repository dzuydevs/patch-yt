package app.revanced.patches.youtube.ads.general.resource.patch

import app.revanced.extensions.doRecursively
import app.revanced.extensions.startsWithAny
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.ads.general.bytecode.patch.GeneralAdsBytecodePatch
import app.revanced.patches.youtube.ads.getpremium.patch.HideGetPremiumPatch
import app.revanced.patches.youtube.utils.fix.doublebacktoclose.patch.DoubleBackToClosePatch
import app.revanced.patches.youtube.utils.fix.swiperefresh.patch.SwipeRefreshPatch
import app.revanced.patches.youtube.utils.litho.patch.LithoFilterPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.PATCHES_PATH
import app.revanced.util.resources.ResourceUtils.copyXmlNode
import org.w3c.dom.Element

@Patch(
    name = "Hide general ads",
    description = "Hides general ads.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.22.37",
                "18.23.36",
                "18.24.37",
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39"
            ]
        )
    ],
    dependencies = [
        DoubleBackToClosePatch::class,
        GeneralAdsBytecodePatch::class,
        HideGetPremiumPatch::class,
        LithoFilterPatch::class,
        SettingsPatch::class,
        SwipeRefreshPatch::class
    ]
)
@Suppress("unused")
object GeneralAdsPatch : ResourcePatch() {
    private val resourceFileNames = arrayOf(
        "promoted_",
        "promotion_",
        "compact_premium_",
        "compact_promoted_",
        "simple_text_section",
    )

    private val replacements = arrayOf(
        "height",
        "width",
        "marginTop"
    )

    private val additionalReplacements = arrayOf(
        "Bottom",
        "End",
        "Start",
        "Top"
    )

    override fun execute(context: ResourceContext) {
        LithoFilterPatch.addFilter("$PATCHES_PATH/ads/AdsFilter;")

        context.forEach {

            if (!it.name.startsWithAny(*resourceFileNames)) return@forEach

            // for each file in the "layouts" directory replace all necessary attributes content
            context.xmlEditor[it.absolutePath].use { editor ->
                editor.file.doRecursively {
                    replacements.forEach replacement@{ replacement ->
                        if (it !is Element) return@replacement

                        it.getAttributeNode("android:layout_$replacement")?.let { attribute ->
                            attribute.textContent = "0.0dip"
                        }
                    }
                }
            }
        }

        context.xmlEditor["res/layout/simple_text_section.xml"].use { editor ->
            editor.file.doRecursively {
                additionalReplacements.forEach replacement@{ replacement ->
                    if (it !is Element) return@replacement

                    it.getAttributeNode("android:padding_$replacement")?.let { attribute ->
                        attribute.textContent = "0.0dip"
                    }
                }
            }
        }

        /**
         * Copy arrays
         */
        context.copyXmlNode("youtube/doubleback/host", "values/arrays.xml", "resources")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: ADS_SETTINGS",
                "SETTINGS: HIDE_GENERAL_ADS",

                "SETTINGS: DOUBLE_BACK_TIMEOUT"
            )
        )

        SettingsPatch.updatePatchStatus("hide-general-ads")

    }
}