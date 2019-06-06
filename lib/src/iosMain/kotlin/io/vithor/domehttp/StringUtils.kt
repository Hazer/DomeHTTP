@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

import platform.Foundation.*


inline fun String.toNSData(): NSData? {
    return this.asNSString.dataUsingEncoding(NSUTF8StringEncoding)
}

inline fun NSData.string(): String? {
    return NSString.create(this, NSUTF8StringEncoding) as String?
}

inline val String.asNSString: NSString
    get() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return this as NSString
    }


inline fun percentEscapeString(value: String): String {
    val characterSet =
        NSMutableCharacterSet.characterSetWithCharactersInString("-._* ").also {
            it.formUnionWithCharacterSet(NSCharacterSet.alphanumericCharacterSet())
        }

    val nss = value.asNSString
    return nss.stringByAddingPercentEncodingWithAllowedCharacters(characterSet) ?: ""
        .replace(' ', '+')
}
