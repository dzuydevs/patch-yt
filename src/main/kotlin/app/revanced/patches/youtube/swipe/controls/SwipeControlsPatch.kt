package app.revanced.patches.youtube.swipe.controls

import app.revanced.extensions.exception
import app.revanced.extensions.transformMethods
import app.revanced.extensions.traverseClassHierarchy
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.swipe.controls.fingerprints.HDRBrightnessFingerprint
import app.revanced.patches.youtube.swipe.controls.fingerprints.SwipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.swipe.controls.fingerprints.WatchWhileActivityFingerprint
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.integrations.Constants.SWIPE_PATH
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Swipe controls",
    description = "Adds volume and brightness swipe controls.",
    dependencies = [
        PlayerTypeHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41"
            ]
        )
    ]
)
@Suppress("unused")
object SwipeControlsPatch : BytecodePatch(
    setOf(
        HDRBrightnessFingerprint,
        SwipeControlsHostActivityFingerprint,
        WatchWhileActivityFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val wrapperClass = SwipeControlsHostActivityFingerprint.result?.mutableClass
            ?: throw SwipeControlsHostActivityFingerprint.exception
        val targetClass = WatchWhileActivityFingerprint.result?.mutableClass
            ?: throw WatchWhileActivityFingerprint.exception

        // inject the wrapper class from integrations into the class hierarchy of WatchWhileActivity
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // ensure all classes and methods in the hierarchy are non-final, so we can override them in integrations
        context.traverseClassHierarchy(targetClass) {
            accessFlags = accessFlags and AccessFlags.FINAL.value.inv()
            transformMethods {
                ImmutableMethod(
                    definingClass,
                    name,
                    parameters,
                    returnType,
                    accessFlags and AccessFlags.FINAL.value.inv(),
                    annotations,
                    hiddenApiRestrictions,
                    implementation
                ).toMutable()
            }
        }

        HDRBrightnessFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $SWIPE_PATH/DisableHDRAutoBrightnessPatch;->disableHDRAutoBrightness()Z
                        move-result v0
                        if-eqz v0, :default
                        return-void
                        """, ExternalLabel("default", getInstruction(0))
                )
            }
        } ?: throw HDRBrightnessFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SWIPE_SETTINGS",
                "SETTINGS: SWIPE_CONTROLS"
            )
        )

        SettingsPatch.updatePatchStatus("Swipe controls")

        contexts.copyResources(
            "youtube/swipecontrols",
            ResourceUtils.ResourceGroup(
                "drawable",
                "ic_sc_brightness_auto.xml",
                "ic_sc_brightness_manual.xml",
                "ic_sc_volume_mute.xml",
                "ic_sc_volume_normal.xml"
            )
        )
    }
}