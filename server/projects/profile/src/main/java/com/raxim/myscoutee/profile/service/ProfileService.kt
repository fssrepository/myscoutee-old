package com.raxim.myscoutee.profile.service

import com.raxim.myscoutee.common.FileUtil
import com.raxim.myscoutee.common.save
import com.raxim.myscoutee.profile.controller.score
import com.raxim.myscoutee.profile.data.document.mongo.Profile
import com.raxim.myscoutee.profile.data.dto.rest.Car
import com.raxim.myscoutee.profile.data.dto.rest.ProfileStatus
import com.raxim.myscoutee.profile.data.dto.rest.School
import com.raxim.myscoutee.profile.repository.mongo.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val profileEventHandler: ProfileEventHandler,
    private val carRepository: CarRepository,
    private val carEventHandler: CarEventHandler,
    private val schoolRepository: SchoolRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {

    //val reqGroupId = UUID.fromString(groupId)
    //if (profile.group == reqGroupId) {
    /*val role = this.roleRepository.findRoleByProfileAndGroup(adminEmail, groupId = reqGroupId)
        .firstOrNull { role -> role.group.id == reqGroupId && role.role == "ROLE_ADMIN" && role.status == "A" }*/
    //}

    fun saveProfileStatus(
        profileId: String,
        profileStatus: ProfileStatus
    ): com.raxim.myscoutee.profile.data.dto.rest.Profile? {
        return profileRepository.findById(UUID.fromString(profileId)).map { profile ->
            val profileToSave = profile.copy(status = profileStatus.status)
            val profileSaved = profileRepository.save(profileToSave)

            val role = this.roleRepository.findRoleByProfile(profileSaved.id!!)

            val firstRole = role.first().copy(role = profileStatus.role!!)
            this.roleRepository.save(firstRole)

            com.raxim.myscoutee.profile.data.dto.rest.Profile(profile = profileSaved)
        }.get()
    }

    fun saveProfile(
        userId: UUID,
        profileId: UUID?,
        group: UUID,
        profile: Profile,
        voice: MultipartFile? = null
    ): com.raxim.myscoutee.profile.data.dto.rest.Profile? {
        val fileName: String = UUID.randomUUID().toString()

        val (separator, permDir) = FileUtil.uuidToPath(fileName, true)

        if (voice != null && !voice.isEmpty) {
            val voiceStream: InputStream = ByteArrayInputStream(voice.bytes)
            voiceStream.save("$permDir${separator}_${fileName}_voice")
        }

        val profileToSave = if (profileId != null) {
            val profileResult = this.profileRepository.findById(profileId)
            val profileEntity = profileResult.get()
            profile.copy(
                id = profileEntity.id,
                cars = profileEntity.cars,
                schools = profileEntity.schools,
                voice = fileName,
                group = group
            )
        } else {
            profile.copy(
                id = UUID.randomUUID(),
                voice = fileName,
                group = group
            )
        }

        val profileSaved = this.profileRepository.save(profileToSave.copy(score = profileToSave.score()))

        this.userRepository.findById(userId).map { user ->
            user.profile = profileSaved

            user.profiles!!.add(profileSaved)
            userRepository.save(user)
        }

        this.profileEventHandler.handleAfterCreate(profileSaved)
        return com.raxim.myscoutee.profile.data.dto.rest.Profile(profileSaved)
    }

    fun getCars(profileId: UUID, step: Int?, tOffset: Array<Any>): List<Car> {
        return this.profileRepository.findCarsByProfile(
            profileId, 20, step ?: 5,
            tOffset
        )
    }

    fun getCarByProfile(profileId: UUID, carId: UUID): Optional<com.raxim.myscoutee.profile.data.document.mongo.Car> {
        return this.profileRepository.findCarByProfile(
            profileId, carId
        )
    }

    fun getSchools(profileId: UUID, step: Int?, tOffset: Array<Any>): List<School> {
        return this.profileRepository.findSchoolsByProfile(
            profileId, 20, step ?: 5,
            tOffset
        )
    }

    fun getSchoolByProfile(
        profileId: UUID,
        schoolId: UUID
    ): Optional<com.raxim.myscoutee.profile.data.document.mongo.School> {
        return this.profileRepository.findSchoolByProfile(
            profileId, schoolId
        )
    }

    fun addCar(profileId: UUID, carId: UUID?, newCar: com.raxim.myscoutee.profile.data.document.mongo.Car): Car {
        return this.profileRepository.findById(profileId).map { profile ->

            val car = if (carId != null) {
                val dbCar = profile.cars!!.first { car -> car.id == carId }
                newCar.copy(id = dbCar.id)
            } else {
                newCar
            }

            val savedCar = carRepository.save(car)
            profile.cars!!.add(savedCar)
            profileRepository.save(profile)
            carEventHandler.handleAfterCreate(car)
            Car(savedCar)
        }.get()
    }

    fun saveSchools(
        profileId: UUID,
        schools: List<com.raxim.myscoutee.profile.data.document.mongo.School>
    ): List<School> {
        return this.profileRepository.findById(profileId).map { profile ->

            for (school in schools) {
                val savedSchool = this.schoolRepository.save(school)
                val hasSchool = profile.schools!!.any { school -> school.id == savedSchool.id }
                if (!hasSchool) {
                    profile.schools.add(savedSchool)
                }
            }
            this.profileRepository.save(profile)

            profile.schools!!.map { school ->
                School(school)
            }
        }.get()
    }
}