package app.revanced.patches.youtube.utils.overridespeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object PlaybackSpeedPatchFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("F"),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/PlaybackSpeedPatch;") && methodDef.name == "overrideSpeed" }
)