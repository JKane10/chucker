package com.chuckerteam.chucker.internal.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.api.Chucker.logger
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.combineLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

internal class TransactionViewModel(transactionId: Long) : ViewModel() {

    private val mutableEncodeUrl = MutableLiveData<Boolean>(false)

    val encodeUrl: LiveData<Boolean> = mutableEncodeUrl

    val transactionTitle: LiveData<String> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .combineLatest(encodeUrl) { transaction, encodeUrl ->
            if (transaction != null) "${transaction.method} ${transaction.getFormattedPath(encode = encodeUrl)}" else ""
        }

    val doesUrlRequireEncoding: LiveData<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .map { transaction ->
            if (transaction == null) {
                false
            } else {
                transaction.getFormattedPath(encode = true) != transaction.getFormattedPath(encode = false)
            }
        }

    val doesRequestBodyRequireEncoding: LiveData<Boolean> = RepositoryProvider.transaction()
        .getTransaction(transactionId)
        .map { transaction ->
            transaction?.requestContentType?.contains("x-www-form-urlencoded", ignoreCase = true) ?: false
        }

    val transaction: LiveData<HttpTransaction?> = RepositoryProvider.transaction().getTransaction(transactionId)

    val formatRequestBody: LiveData<Boolean> = doesRequestBodyRequireEncoding
        .combineLatest(encodeUrl) { requiresEncoding, encodeUrl ->
            !(requiresEncoding && encodeUrl)
        }

    fun switchUrlEncoding() = encodeUrl(!encodeUrl.value!!)

    fun encodeUrl(encode: Boolean) {
        mutableEncodeUrl.value = encode
    }

    /**
     * Writes mock response body and other meta info to the database.
     */
    fun writeMock(transaction: HttpTransaction, mockedBody: String, shouldUseMock: Boolean) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val mock = getMock(transaction)
                if (mock == null) {
                    transaction.responseBody = mockedBody
                    transaction.shouldUseMock = shouldUseMock
                    RepositoryProvider.mockTransaction().insertTransaction(transaction)
                } else {
                    mock.responseBody = mockedBody
                    mock.shouldUseMock = shouldUseMock
                    RepositoryProvider.mockTransaction().updateTransaction(mock)
                }
            }
        } catch (e: IllegalStateException) {
            // mocking not enabled
            logger.info("Mock Repository was not initialized: $e")
        }
    }

    suspend fun getMock(transaction: HttpTransaction): HttpTransaction? = coroutineScope {
        transaction.url?.let {
            RepositoryProvider.mockTransaction().getMockedTransactionByUrl(it)
        }
    }
}

internal class TransactionViewModelFactory(
    private val transactionId: Long = 0L
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return TransactionViewModel(transactionId) as T
    }
}
