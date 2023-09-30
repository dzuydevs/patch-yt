package app.revanced.patches.youtube.utils.litho.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.patch.litho.ComponentParserPatch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch.generalHook
import app.revanced.patches.youtube.utils.litho.fingerprints.GeneralByteBufferFingerprint
import app.revanced.patches.youtube.utils.litho.fingerprints.LithoFilterFingerprint
import app.revanced.patches.youtube.utils.litho.fingerprints.LowLevelByteBufferFingerprint
import app.revanced.patches.youtube.utils.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.ADS_PATH
import java.io.Closeable

@Patch(
    dependencies = [
        ComponentParserPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class
    ]
)
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(
        GeneralByteBufferFingerprint,
        LithoFilterFingerprint,
        LowLevelByteBufferFingerprint
    )
), Closeable {
    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0

    override fun execute(context: BytecodeContext) {

        LowLevelByteBufferFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static { p0 }, $ADS_PATH/LowLevelFilter;->setProtoBuffer(Ljava/nio/ByteBuffer;)V"
        ) ?: throw LowLevelByteBufferFingerprint.exception

        GeneralByteBufferFingerprint.result?.let {
            (context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                .getMethod() as MutableMethod
                    ).apply {
                    addInstruction(
                        0,
                        "invoke-static { p2 }, $ADS_PATH/LithoFilterPatch;->setProtoBuffer(Ljava/nio/ByteBuffer;)V"
                    )
                }
        } ?: throw GeneralByteBufferFingerprint.exception

        generalHook("$ADS_PATH/LithoFilterPatch;->filters")

        LithoFilterFingerprint.result?.mutableMethod?.apply {
            removeInstructions(0, 6)

            addFilter = { classDescriptor ->
                addInstructions(
                    0, """
                        new-instance v0, $classDescriptor
                        invoke-direct {v0}, $classDescriptor-><init>()V
                        const/16 v3, ${filterCount++}
                        aput-object v0, v2, v3
                        """
                )
            }
        } ?: throw LithoFilterFingerprint.exception

    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.addInstructions(
            0, """
                const/16 v1, $filterCount
                new-array v2, v1, [Lapp/revanced/integrations/patches/ads/Filter;
                const/4 v1, 0x1
                """
        )
}
