package com.raxim.myscoutee.common.config.firebase

import com.google.firebase.auth.FirebaseAuth
import com.raxim.myscoutee.common.config.ConfigProperties
import com.raxim.myscoutee.profile.data.document.mongo.Profile
import com.raxim.myscoutee.profile.data.document.mongo.Role
import com.raxim.myscoutee.profile.data.document.mongo.User
import com.raxim.myscoutee.profile.repository.mongo.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class FirebaseService(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val groupRepository: GroupRepository,
    private val config: ConfigProperties,
    private val linkRepository: LinkRepository
) {
    fun parseToken(firebaseToken: String?): FirebaseTokenHolder {
        require(firebaseToken!!.isNotBlank()) { "FirebaseTokenBlank" }
        return try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken)
            FirebaseTokenHolder(decodedToken)
        } catch (e: Exception) {
            throw FirebaseTokenInvalidException(e.message)
        }
    }

    fun loadUserByUsername(username: String, xLink: String?): UserDetails {
        var user = this.userRepository.findUserByEmail(username)
        val profileSaved: List<Profile>

        if (user == null) {
            val group = if (config.adminUser == username) {
                val groups = this.groupRepository.findSystemGroups()
                val profiles = groups.map { group ->
                    Profile(group = group.id)
                }

                profileSaved = this.profileRepository.saveAll(profiles)

                val roles = profileSaved.map { profile ->
                    Role(UUID.randomUUID(), profile.id!!, "ROLE_ADMIN")
                }

                this.roleRepository.saveAll(roles)
                groups.first { group -> group.type == "b" }
            } else {

                val groups =
                    this.groupRepository.findSystemGroups().toMutableList()

                val profiles = groups.map { group ->
                    Profile(group = group.id)
                }

                profileSaved = this.profileRepository.saveAll(profiles)

                val roles = profileSaved.map { profile ->
                    Role(UUID.randomUUID(), profile.id!!, "ROLE_USER")
                }
                this.roleRepository.saveAll(roles)

                groups.first { group -> group.type == "d" }
            }

            val profile = profileSaved.find { profile -> profile.group == group.id }
            val userToSave =
                User(UUID.randomUUID(), username, Date(), group.id, profile, profiles = profileSaved.toMutableSet())
            user = this.userRepository.save(userToSave)
        }

        if (xLink != null) {
            this.linkRepository.findByKey(UUID.fromString(xLink)).map { link ->
                val usedBy = link.usedBys.filter { usedBy -> usedBy == username }
                if (usedBy.isEmpty()) {
                    when (link.type) {
                        "g" -> {
                            val group = this.groupRepository.findById(link.refId!!)
                            if (group.isPresent) {
                                val profile = Profile(group = group.get().id)
                                val profileSaved = this.profileRepository.save(profile)

                                val role = Role(UUID.randomUUID(), profileSaved.id!!, "ROLE_USER")
                                this.roleRepository.save(role)

                                user.profiles?.add(profile)
                                val userNew = user.copy(group = group.get().id, profile = profile)
                                this.userRepository.save(userNew)
                            }
                        }
                        else -> {

                        }
                    }

                    link.usedBys.add(username)
                    this.linkRepository.save(link)
                }
            }
        }

        val roles = user.profile?.let { profile ->
            this.roleRepository.findRoleByProfile(
                profile.id!!
            )
        } ?: emptyList()

        return FirebasePrincipal(user, roles);
    }
}