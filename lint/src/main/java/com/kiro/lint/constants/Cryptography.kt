package com.kiro.lint.constants

class Cryptography {
    enum class JavaCryptoPackagesEnum(val packageName: String) {
        CIPHER("javax.crypto.Cipher"),
        MESSAGE_DIGEST("java.security.MessageDigest"),
        KEY_GENERATOR("javax.crypto.KeyGenerator"),
    }

    //https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher
    enum class CipherAlgorithmNamesEnum(val algorithmName: String) {
        AES("AES"),
        AES_WRAP("AESWrap"),
        ARCFOUR("ARCFOUR"),
        BLOWFISH("Blowfish"),
        CCM("CCM"),
        DES("DES"),
        DES_EDE("DESede"),
        DES_EDE_WRAP("DESedeWrap"),
        ECIES("ECIES"),
        GCM("GCM"),
        RC2("RC2"),
        RC4("RC4"),
        RC5("RC5"),
        RSA("RSA"),
    }

    // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest
    enum class MessageDigestAlgorithmNamesEnum(val algorithmName: String) {
        MD2("MD2"),
        MD5("MD5"),
        SHA_1("SHA-1"),
        SHA_256("SHA-256"),
        SHA_384("SHA-384"),
        SHA_512("SHA-512"),
    }

    //https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator
    enum class KeyGeneratorAlgorithmNamesEnum(val algorithmName: String) {
        AES("AES"),
        ARCFOUR("ARCFOUR"),
        BLOWFISH("Blowfish"),
        DES("DES"),
        DES_EDE("DESede"),
        HMAC_MD5("HmacMD5"),
        HMAC_SHA_1("HmacSHA1"),
        HMAC_SHA_256("HmacSHA256"),
        HMAC_SHA_384("HmacSHA384"),
        HMAC_SHA_512("HmacSHA512"),
        RC2("RC2"),
    }

    /*
    Confidentiality algorithms: AES-GCM-256 or ChaCha20-Poly1305
    Integrity algorithms: SHA-256, SHA-384, SHA-512, Blake2, the SHA-3 family
    Digital signature algorithms: RSA (3072 bits and higher), ECDSA with NIST P-384
    Key establishment algorithms: RSA (3072 bits and higher), DH (3072 bits or higher), ECDH with NIST P-384
     */
    companion object {
        val packageNameToInsufficientAlgorithmsMap = mapOf(
            JavaCryptoPackagesEnum.CIPHER to listOf(
                CipherAlgorithmNamesEnum.AES_WRAP.algorithmName,
                CipherAlgorithmNamesEnum.ARCFOUR.algorithmName,
                CipherAlgorithmNamesEnum.BLOWFISH.algorithmName,
                CipherAlgorithmNamesEnum.CCM.algorithmName,
                CipherAlgorithmNamesEnum.DES.algorithmName,
                CipherAlgorithmNamesEnum.DES_EDE.algorithmName,
                CipherAlgorithmNamesEnum.DES_EDE_WRAP.algorithmName,
                CipherAlgorithmNamesEnum.ECIES.algorithmName,
                CipherAlgorithmNamesEnum.GCM.algorithmName,
                CipherAlgorithmNamesEnum.RC2.algorithmName,
                CipherAlgorithmNamesEnum.RC4.algorithmName,
                CipherAlgorithmNamesEnum.RC5.algorithmName,
            ),
            JavaCryptoPackagesEnum.MESSAGE_DIGEST to listOf(
                MessageDigestAlgorithmNamesEnum.MD2.algorithmName,
                MessageDigestAlgorithmNamesEnum.MD5.algorithmName,
                MessageDigestAlgorithmNamesEnum.SHA_1.algorithmName
            ),
            JavaCryptoPackagesEnum.KEY_GENERATOR to listOf(
                KeyGeneratorAlgorithmNamesEnum.ARCFOUR.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.BLOWFISH.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.DES.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.DES_EDE.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.HMAC_MD5.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.HMAC_SHA_1.algorithmName,
                KeyGeneratorAlgorithmNamesEnum.RC2.algorithmName
            ),
        )
    }
}