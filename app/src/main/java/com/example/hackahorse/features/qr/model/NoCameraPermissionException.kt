package org.tokend.muna.features.qr.model

class NoCameraPermissionException
    : IllegalStateException("Camera permission is required to perform this action")