package app.revanced.patches.youtube.general.widesearchbar

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.general.widesearchbar.fingerprints.SetActionBarRingoFingerprint
import app.revanced.patches.youtube.general.widesearchbar.fingerprints.YouActionBarFingerprint
import app.revanced.patches.youtube.utils.fingerprints.SetToolBarPaddingFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.integrations.Constants.GENERAL
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable wide search bar",
    description = "Replaces the search icon with a wide search bar. This will hide the YouTube logo when active.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
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
object WideSearchBarPatch : BytecodePatch(
    setOf(
        SetActionBarRingoFingerprint,
        SetToolBarPaddingFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        arrayOf(
            SetActionBarRingoFingerprint,
            SetToolBarPaddingFingerprint
        ).forEach {
            it.injectHook(context)
        }

        YouActionBarFingerprint.also {
            it.resolve(
                context,
                SetActionBarRingoFingerprint.result!!.classDef
            )
        }.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $GENERAL->enableWideSearchBarInYouTab(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        } ?: throw YouActionBarFingerprint.exception

        /**
         * Set Wide SearchBar Start Margin
         */
        contexts.xmlEditor[TARGET_RESOURCE_PATH].use { editor ->
            val document = editor.file

            with(document.getElementsByTagName("RelativeLayout").item(0)) {
                if (attributes.getNamedItem(FLAG) != null) return@with

                document.createAttribute(FLAG)
                    .apply { value = "8.0dip" }
                    .let(attributes::setNamedItem)
            }
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: ENABLE_WIDE_SEARCH_BAR"
            )
        )

        SettingsPatch.updatePatchStatus("Enable wide search bar")

    }

    private const val FLAG = "android:paddingStart"
    private const val TARGET_RESOURCE_PATH = "res/layout/action_bar_ringo_background.xml"

    private fun MethodFingerprint.injectHook(context: BytecodeContext) {
        result?.let {
            (context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                .getMethod() as MutableMethod).apply {
                addInstructions(
                    implementation!!.instructions.size - 1, """
                            invoke-static {}, $GENERAL->enableWideSearchBar()Z
                            move-result p0
                            """
                )
            }
        } ?: throw exception
    }
}
