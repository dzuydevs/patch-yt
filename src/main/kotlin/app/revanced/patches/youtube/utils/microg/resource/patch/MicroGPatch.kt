package app.revanced.patches.youtube.utils.microg.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.shared.patch.packagename.PackageNamePatch
import app.revanced.patches.youtube.utils.microg.bytecode.patch.MicroGBytecodePatch
import app.revanced.patches.youtube.utils.microg.shared.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.utils.microg.shared.Constants.SPOOFED_PACKAGE_NAME
import app.revanced.patches.youtube.utils.microg.shared.Constants.SPOOFED_PACKAGE_SIGNATURE
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.microg.MicroGManifestHelper.addSpoofingMetadata
import app.revanced.util.microg.MicroGResourceHelper.patchManifest
import app.revanced.util.microg.MicroGResourceHelper.patchSetting
import app.revanced.util.resources.ResourceHelper.setMicroG

@Patch(
    name = "MicroG support",
    description = "Allows ReVanced to run without root and under a different package name with MicroG.",
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
        PackageNamePatch::class,
        SettingsPatch::class,
        MicroGBytecodePatch::class,
    ]
)
@Suppress("unused")
object MicroGPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {

        val packageName = PackageNamePatch.YouTubePackageName
            ?: throw PatchException("Invalid package name.")

        if (packageName == PACKAGE_NAME)
            throw PatchException("Original package name is not available as package name for MicroG build.")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: MICROG_SETTINGS"
            )
        )
        SettingsPatch.updatePatchStatus("microg-support")

        // update settings fragment
        context.patchSetting(
            PACKAGE_NAME,
            packageName
        )

        // update manifest
        context.patchManifest(
            PACKAGE_NAME,
            packageName
        )

        // add metadata to manifest
        context.addSpoofingMetadata(
            SPOOFED_PACKAGE_NAME,
            SPOOFED_PACKAGE_SIGNATURE
        )

        setMicroG(packageName)

    }
}