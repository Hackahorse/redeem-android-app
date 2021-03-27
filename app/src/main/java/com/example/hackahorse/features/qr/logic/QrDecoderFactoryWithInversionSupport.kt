package org.tokend.muna.features.qr.logic

import com.google.zxing.*
import com.google.zxing.qrcode.QRCodeReader
import com.journeyapps.barcodescanner.Decoder
import com.journeyapps.barcodescanner.DecoderFactory

/**
 * This decoder factory allows to decode QR codes with inverted colors
 * as well as the regular ones.
 */
class QrDecoderFactoryWithInversionSupport: DecoderFactory {
    override fun createDecoder(baseHints: MutableMap<DecodeHintType, *>?): Decoder {
        var invertedBitmapToDecode: BinaryBitmap? = null

        val reader = object : Reader {
            private val readers = listOf(
                QRCodeReader(),
                object : QRCodeReader() {
                    override fun decode(ignored: BinaryBitmap): Result {
                        return invertedBitmapToDecode
                            ?.let { super.decode(it) }
                            ?: throw NotFoundException.getNotFoundInstance()
                    }
                }
            )

            override fun reset() = readers.forEach(Reader::reset)

            override fun decode(image: BinaryBitmap,
                                hints: MutableMap<DecodeHintType, *>) = decode(image)

            override fun decode(image: BinaryBitmap): Result {
                for (reader in readers) {
                    try {
                        return reader.decode(image)
                    } catch (re: ReaderException) {
                        // continue
                    }
                }

                throw NotFoundException.getNotFoundInstance()
            }
        }

        return object : Decoder(reader) {
            override fun toBitmap(source: LuminanceSource): BinaryBitmap {
                invertedBitmapToDecode = super.toBitmap(InvertedLuminanceSource(source))
                return super.toBitmap(source)
            }
        }
    }
}