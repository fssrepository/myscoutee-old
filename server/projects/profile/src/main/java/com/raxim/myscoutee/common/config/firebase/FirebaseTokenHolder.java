package com.raxim.myscoutee.common.config.firebase

import com.google.api.client.util.ArrayMap
import com.google.firebase.auth.FirebaseToken
import java.util.*


class FirebaseTokenHolder(private val token: FirebaseToken) {
    val email: String
        get() = token.email
    val issuer: String
        get() = token.issuer
    val name: String
        get() = token.name
    val uid: String
        get() = token.uid
    val googleId: String?
        get() = (((token.claims["firebase"] as ArrayMap<*, *>?)?.get("identities") as ArrayMap<*, *>?)!!["google.com"] as ArrayList<String?>?)!![0]

}