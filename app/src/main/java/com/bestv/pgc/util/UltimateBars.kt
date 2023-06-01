@file:Suppress("NOTHING_TO_INLINE")

package com.bestv.pgc.util

import android.app.Activity

inline fun Activity.ultimateBarBuilder(): UltimateBar.Builder = UltimateBar.with(this)
