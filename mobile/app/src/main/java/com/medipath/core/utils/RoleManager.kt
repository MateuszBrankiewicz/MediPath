package com.medipath.core.utils

object RoleManager {
    private const val ROLE_CODE_DOCTOR = 2

    fun canBeDoctor(roleCode: Int): Boolean {
        return (roleCode and ROLE_CODE_DOCTOR) != 0
    }
}
