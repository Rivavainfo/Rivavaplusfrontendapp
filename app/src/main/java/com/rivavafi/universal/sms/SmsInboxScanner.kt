package com.rivavafi.universal.sms

import android.content.Context
import android.provider.Telephony
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SmsScanResult(
    val messagesExamined: Int = 0,
    val transactionsDetected: Int = 0,
    val newTransactionsAdded: Int = 0,
    val duplicatesSkipped: Int = 0
)

class SmsInboxScanner @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend fun scanInbox(context: Context, mode: SmsTrackingMode, userId: String): SmsScanResult {
        if (mode == SmsTrackingMode.OFF) return SmsScanResult()

        return withContext(Dispatchers.IO) {
            val lastScanDate = preferencesRepository.lastSmsScanDateFlow.first()
            var latestScanDate = lastScanDate

            var examined = 0
            var detected = 0
            var added = 0
            var duplicates = 0

            val cursor = runCatching {
                context.contentResolver.query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE
                    ),
                    "${Telephony.Sms.DATE} > ?",
                    arrayOf(lastScanDate.toString()),
                    "${Telephony.Sms.DATE} DESC LIMIT 500"
                )
            }.getOrNull()

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    examined++
                    val smsId = it.getString(idIndex)
                    val sender = it.getString(addressIndex) ?: ""
                    val body = it.getString(bodyIndex) ?: ""
                    val timestamp = it.getLong(dateIndex)

                    if (timestamp > latestScanDate) {
                        latestScanDate = timestamp
                    }

                    val parsed = SmsTransactionParser.parseMessage(sender, body, timestamp, smsId, mode)
                    if (parsed != null) {
                        detected++
                        val isProcessed = transactionRepository.isSmsIdProcessed(smsId, userId)
                        if (isProcessed) {
                            duplicates++
                        } else {
                            val duplicate = transactionRepository.findDuplicate(
                                userId = userId,
                                transactionId = parsed.transactionId,
                                referenceId = parsed.referenceId,
                                date = parsed.date,
                                amount = parsed.amount,
                                type = parsed.type,
                                merchantName = parsed.merchantName,
                                smsSender = parsed.smsSender
                            )

                            if (duplicate != null) {
                                duplicates++
                            } else {
                                val entity = TransactionEntity(
                                    userId = userId,
                                    bankName = parsed.bankName,
                                    merchantName = parsed.merchantName,
                                    amount = parsed.amount,
                                    type = parsed.type,
                                    category = parsed.category,
                                    date = parsed.date,
                                    smsId = parsed.smsId,
                                    rawMessage = parsed.rawMessage,
                                    availableBalance = parsed.availableBalance,
                                    referenceId = parsed.referenceId,
                                    subcategory = parsed.subcategory,
                                    upiId = parsed.upiId,
                                    accountNumberLast4 = parsed.accountNumberLast4,
                                    transactionId = parsed.transactionId,
                                    smsSender = parsed.smsSender,
                                    source = parsed.source
                                )
                                transactionRepository.addTransaction(entity)
                                added++
                            }
                        }
                    }
                }
            }

            if (latestScanDate > lastScanDate) {
                preferencesRepository.setLastSmsScanDate(latestScanDate)
            }

            SmsScanResult(
                messagesExamined = examined,
                transactionsDetected = detected,
                newTransactionsAdded = added,
                duplicatesSkipped = duplicates
            )
        }
    }
}
