package com.save.protect.data

class DocIdManagement {
    companion object {

        private var receivedId: String? = ""

        fun resetReceivedId() {
            receivedId = ""
        }

        fun setReceivedId(value: String? = "") {
            receivedId = value
        }

        fun getReceivedId(): String? {
            return receivedId
        }

    }
}