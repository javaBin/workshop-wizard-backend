package no.javabin.util

import kotlinx.datetime.*

class TimeUtil {
    companion object {
        fun toGmtPlus2(zuluInstant: Instant): Instant {
            val gmtPlus2Offset = 2

            // Add the offset to the Zulu instant to get the time in GMT+2
            return zuluInstant.plus(2, DateTimeUnit.HOUR)
        }
    }
}
