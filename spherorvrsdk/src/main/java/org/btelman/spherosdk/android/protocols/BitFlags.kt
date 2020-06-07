@file:Suppress("NOTHING_TO_INLINE", "unused")
package org.btelman.spherosdk.android.protocols

/*
 * https://gist.github.com/LouisCAD/04be811e22d45cbc7285ab247c6c12e7
 */

import kotlin.experimental.and // Used for Byte
import kotlin.experimental.inv // Used for Byte
import kotlin.experimental.or // Used for Byte

inline fun Int.hasFlag(flag: Int) = flag and this == flag
inline fun Int.withFlag(flag: Int) = this or flag
inline fun Int.minusFlag(flag: Int) = this and flag.inv()

inline fun Byte.hasFlag(flag: Byte) = flag and this == flag
inline fun Byte.withFlag(flag: Byte) = this or flag
inline fun Byte.minusFlag(flag: Byte) = this and flag.inv()